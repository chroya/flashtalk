package com.speech.stream.rtp

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue

/**
 * RTP服务器处理器
 * 处理解码后的RTP包
 */
@Component
class RtpServerHandler(
    private val packetQueue: BlockingQueue<RtpPacket>
) : SimpleChannelInboundHandler<RtpPacket>() {
    
    private val logger = LoggerFactory.getLogger(RtpServerHandler::class.java)
    
    override fun channelRead0(ctx: ChannelHandlerContext, packet: RtpPacket) {
        try {
            // 检查是否为Opus音频包
            if (packet.payloadType != OPUS_PAYLOAD_TYPE) {
                logger.warn("Received non-Opus packet: type=${packet.payloadType}")
                return
            }

            // 将数据包放入队列
            if (!packetQueue.offer(packet)) {
                logger.warn("Packet queue full, dropping packet")
            }

            logger.debug("Received RTP packet: seq=${packet.sequenceNumber}, ts=${packet.timestamp}")
        } catch (e: Exception) {
            logger.error("Error processing RTP packet", e)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Channel error", cause)
        ctx.close()
    }

    companion object {
        const val OPUS_PAYLOAD_TYPE = 111  // Opus音频的RTP负载类型
    }
} 