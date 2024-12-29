#include "stream_service.h"
#include <iostream>

namespace speech {

StreamService::StreamService() {
    opus_decoder_ = std::make_unique<OpusDecoder>();
    rtp_server_ = std::make_unique<RTPServer>();
    
    // 启动RTP服务器
    if (!rtp_server_->start()) {
        std::cerr << "Failed to start RTP server" << std::endl;
        return;
    }
    
    // 启动音频处理线程
    running_ = true;
    audio_thread_ = std::thread(&StreamService::processAudio, this);
}

StreamService::~StreamService() {
    running_ = false;
    if (audio_thread_.joinable()) {
        audio_thread_.join();
    }
}

void StreamService::processAudio() {
    while (running_) {
        // 从RTP服务器获取最新的音频数据
        auto [audio_data, timestamp] = rtp_server_->getLatestAudioData();
        
        if (audio_data.empty()) {
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
            continue;
        }
        
        // 解码Opus音频
        auto pcm_data = opus_decoder_->decode(audio_data);
        
        // TODO: 发送到ASR服务进行处理
    }
}

grpc::Status StreamService::ProcessAudioStream(
    grpc::ServerContext* context,
    grpc::ServerReaderWriter<speech::v1::AudioResponse, 
                            speech::v1::AudioChunk>* stream) {
    
    speech::v1::AudioChunk chunk;
    while (stream->Read(&chunk)) {
        // 发送处理响应
        speech::v1::AudioResponse response;
        response.set_success(true);
        response.set_message("Audio chunk received");
        stream->Write(response);
    }
    
    return grpc::Status::OK;
}

} // namespace speech 