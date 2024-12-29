#include "rtp/rtp_server.h"
#include "rtp_source.h"
#include <GroupsockHelper.hh>

namespace speech {

RTPServer::RTPServer(unsigned short port) : port_(port) {
    // 创建Live555基础环境
    scheduler_ = BasicTaskScheduler::createNew();
    env_ = BasicUsageEnvironment::createNew(*scheduler_);
}

RTPServer::~RTPServer() {
    stop();
    
    // 清理Live555环境
    env_->reclaim();
    delete scheduler_;
}

bool RTPServer::start() {
    if (running_) {
        return false;
    }

    // 创建RTP会话
    struct in_addr dest_addr;
    dest_addr.s_addr = INADDR_ANY;
    
    // 创建socket和端口
    Groupsock* rtpGroupsock = new Groupsock(*env_, dest_addr, port_, 255);
    
    // 创建RTP数据源
    rtp_source_ = std::make_unique<RTPSource>(*env_);
    
    // 设置接收到RTP包时的回调
    rtp_source_->setPacketCallback(
        [this](const uint8_t* data, size_t size, uint64_t timestamp) {
            std::lock_guard<std::mutex> lock(queue_mutex_);
            
            // 将接收到的音频数据放入队列
            std::vector<uint8_t> audio_data(data, data + size);
            audio_queue_.push({std::move(audio_data), timestamp});
            
            // 如果队列超出最大大小，移除最旧的数据
            if (audio_queue_.size() > MAX_QUEUE_SIZE) {
                audio_queue_.pop();
            }
        }
    );
    
    // 创建RTCP实例
    unsigned const rtcpPortNum = port_ + 1;
    unsigned const estimatedSessionBandwidth = 10000; // 带宽估计：10Mbps
    rtcp_ = RTCPInstance::createNew(*env_, rtpGroupsock,
                                  estimatedSessionBandwidth, nullptr,
                                  nullptr, false);

    // 启动事件循环线程
    running_ = true;
    event_thread_ = std::thread(&RTPServer::eventLoop, this);
    
    return true;
}

void RTPServer::stop() {
    if (!running_) {
        return;
    }
    
    running_ = false;
    
    // 等待事件循环线程结束
    if (event_thread_.joinable()) {
        event_thread_.join();
    }
    
    // 清理资源
    rtcp_ = nullptr;
    rtp_source_.reset();
}

void RTPServer::eventLoop() {
    // Live555事件循环
    while (running_) {
        env_->taskScheduler().doEventLoop();
    }
}

std::pair<std::vector<uint8_t>, uint64_t> RTPServer::getLatestAudioData() {
    std::lock_guard<std::mutex> lock(queue_mutex_);
    
    if (audio_queue_.empty()) {
        return {std::vector<uint8_t>(), 0};
    }
    
    auto data = std::move(audio_queue_.front());
    audio_queue_.pop();
    return data;
}

} // namespace speech 