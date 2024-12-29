#pragma once

#include <memory>
#include <string>
#include <thread>
#include <queue>
#include <mutex>
#include "liveMedia.hh"
#include "BasicUsageEnvironment.hh"

namespace speech {

// 前向声明
class RTPSource;

class RTPServer {
public:
    // 构造函数，参数为RTP端口号
    explicit RTPServer(unsigned short port = 5004);
    ~RTPServer();

    // 启动RTP服务器
    bool start();
    // 停止RTP服务器
    void stop();
    
    // 获取最新的音频数据
    // 返回值: pair<音频数据, 时间戳>
    std::pair<std::vector<uint8_t>, uint64_t> getLatestAudioData();

private:
    // RTP事件循环线程函数
    void eventLoop();
    
    // Live555环境相关成员
    UsageEnvironment* env_{nullptr};           // Live555运行环境
    TaskScheduler* scheduler_{nullptr};        // 事件调度器
    RTCPInstance* rtcp_{nullptr};             // RTCP实例
    std::unique_ptr<RTPSource> rtp_source_;   // RTP数据源
    
    // 线程相关成员
    std::thread event_thread_;                 // 事件循环线程
    bool running_{false};                      // 服务器运行状态
    
    // 音频数据缓冲相关
    static constexpr size_t MAX_QUEUE_SIZE = 100;  // 最大缓冲区大小
    std::queue<std::pair<std::vector<uint8_t>, uint64_t>> audio_queue_;  // 音频数据队列
    std::mutex queue_mutex_;                   // 队列互斥锁
    
    unsigned short port_;                      // RTP端口号
};

} // namespace speech 