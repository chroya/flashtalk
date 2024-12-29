package com.speech.stream.rtp

/**
 * RTP数据包
 * 包含RTP头部信息和负载数据
 */
data class RtpPacket(
    val payload: ByteArray,          // 音频数据负载
    val timestamp: Long,             // RTP时间戳
    val sequenceNumber: Int,         // 序列号
    val ssrc: Int,                   // 同步源标识符
    val payloadType: Int,            // 负载类型(Opus = 111)
    val marker: Boolean = false      // 标记位
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RtpPacket

        if (!payload.contentEquals(other.payload)) return false
        if (timestamp != other.timestamp) return false
        if (sequenceNumber != other.sequenceNumber) return false
        if (ssrc != other.ssrc) return false
        if (payloadType != other.payloadType) return false
        if (marker != other.marker) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payload.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + sequenceNumber
        result = 31 * result + ssrc
        result = 31 * result + payloadType
        result = 31 * result + marker.hashCode()
        return result
    }
} 