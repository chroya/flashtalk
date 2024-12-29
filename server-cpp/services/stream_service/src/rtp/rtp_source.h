#pragma once

#include <functional>
#include <vector>
#include "liveMedia.hh"

namespace speech {

// RTP数据源类，继承自live555的FramedSource
class RTPSource : public FramedSource {
public:
    explicit RTPSource(UsageEnvironment& env);
    ~RTPSource() override;

    // 处理接收到的RTP包的回调函数类型
    using PacketCallback = std::function<void(const uint8_t*, size_t, uint64_t)>;
    void setPacketCallback(PacketCallback callback);

protected:
    // live555要求实现的虚函数，用于获取下一帧数据
    void doGetNextFrame() override;

private:
    PacketCallback packet_callback_;
};

} // namespace speech 