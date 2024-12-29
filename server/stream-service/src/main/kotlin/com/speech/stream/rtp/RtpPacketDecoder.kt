package com.speech.stream.rtp

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import org.slf4j.LoggerFactory

/**
 * RTP包解码器
 * 将UDP数据包解析为RTP包
 */
class RtpPacketDecoder : MessageToMessageDecoder<DatagramPacket>() {
    private val logger = LoggerFactory.getLogger(RtpPacketDecoder::class.java)

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, out: MutableList<Any>) {
        try {
            val content = msg.content()
            
            // 检查RTP包最小长度(12字节头部)
            if (content.readableBytes() < RTP_HEADER_SIZE) {
                logger.warn("Received packet too small to be RTP: ${content.readableBytes()} bytes")
                return
            }

            // 解析RTP头部
            val firstByte = content.readByte()
            val version = (firstByte.toInt() and 0xC0) ushr 6
            
            // 验证RTP版本
            if (version != 2) {
                logger.warn("Invalid RTP version: $version")
                return
            }

            val secondByte = content.readByte()
            val payloadType = secondByte.toInt() and 0x7F
            val marker = (secondByte.toInt() and 0x80) != 0

            val sequenceNumber = content.readUnsignedShort()
            val timestamp = content.readUnsignedInt()
            val ssrc = content.readInt()

            // 读取负载数据
            val payloadLength = content.readableBytes()
            val payload = ByteArray(payloadLength)
            content.readBytes(payload)

            // 创建RTP包对象
            val rtpPacket = RtpPacket(
                payload = payload,
                timestamp = timestamp,
                sequenceNumber = sequenceNumber,
                ssrc = ssrc,
                payloadType = payloadType,
                marker = marker
            )

            out.add(rtpPacket)
            
            logger.debug("Decoded RTP packet: seq=$sequenceNumber, ts=$timestamp, pt=$payloadType")
        } catch (e: Exception) {
            logger.error("Failed to decode RTP packet", e)
        }
    }

    companion object {
        const val RTP_HEADER_SIZE = 12
    }
} 