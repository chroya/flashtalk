#pragma once

#include <memory>
#include <grpcpp/grpcpp.h>
#include "speech_service.grpc.pb.h"
#include "opus_decoder.h"
#include "rtp/rtp_server.h"

namespace speech {

class StreamService final : public speech::v1::StreamService::Service {
public:
    StreamService();
    ~StreamService() override;

    grpc::Status ProcessAudioStream(
        grpc::ServerContext* context,
        grpc::ServerReaderWriter<speech::v1::AudioResponse, 
                                speech::v1::AudioChunk>* stream) override;

private:
    std::unique_ptr<OpusDecoder> opus_decoder_;
    std::unique_ptr<RTPServer> rtp_server_;
    
    void processAudio();
    std::thread audio_thread_;
    bool running_{false};
};

} // namespace speech 