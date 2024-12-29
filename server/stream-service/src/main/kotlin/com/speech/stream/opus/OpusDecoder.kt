package com.speech.stream.opus

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Opus音频解码器
 * 使用JNI调用本地libopus实现
 */
@Component
class OpusDecoder {
    @Value("\${audio.opus.sample-rate}")
    private var sampleRate: Int = 16000

    @Value("\${audio.opus.channels}")
    private var channels: Int = 1

    @Value("\${audio.opus.frame-size}")
    private var frameSize: Int = 480  // 10ms at 48kHz

    private var handle: Long = 0
    private val logger = LoggerFactory.getLogger(OpusDecoder::class.java)

    /**
     * 初始化Opus解码器
     */
    @PostConstruct
    fun init() {
        try {
            handle = OpusJNI.createDecoder(sampleRate, channels)
            logger.info("Opus decoder initialized with sample rate: $sampleRate, channels: $channels")
        } catch (e: Exception) {
            logger.error("Failed to initialize Opus decoder", e)
            throw RuntimeException("Failed to initialize Opus decoder", e)
        }
    }

    /**
     * 解码Opus音频数据
     * @param opusData Opus编码的音频数据
     * @return 解码后的PCM音频数据
     */
    fun decode(opusData: ByteArray): ByteArray {
        return try {
            // 创建输出缓冲区
            val pcmShorts = ShortArray(frameSize * channels)
            
            // 解码
            val decodedSamples = OpusJNI.decode(handle, opusData, pcmShorts, frameSize)
            if (decodedSamples < 0) {
                throw RuntimeException("Failed to decode Opus data")
            }

            // 转换为字节数组
            val pcmBytes = ByteArray(decodedSamples * channels * 2)  // 2 bytes per sample
            val buffer = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
            
            for (i in 0 until decodedSamples * channels) {
                buffer.putShort(pcmShorts[i])
            }

            logger.debug("Decoded ${opusData.size} bytes of Opus data to ${pcmBytes.size} bytes of PCM")
            pcmBytes
        } catch (e: Exception) {
            logger.error("Failed to decode Opus data", e)
            ByteArray(0)
        }
    }

    /**
     * 清理资源
     */
    @PreDestroy
    fun destroy() {
        if (handle != 0L) {
            try {
                OpusJNI.destroyDecoder(handle)
                handle = 0
                logger.info("Opus decoder destroyed")
            } catch (e: Exception) {
                logger.error("Error closing Opus decoder", e)
            }
        }
    }

    /**
     * 获取解码后的PCM音频格式信息
     */
    fun getPcmFormat(): PcmAudioFormat = PcmAudioFormat(
        sampleRate = sampleRate,
        channels = channels,
        bitsPerSample = 16,  // Opus解码后为16位PCM
        signed = true,
        bigEndian = false
    )
}

/**
 * PCM音频格式描述
 */
data class PcmAudioFormat(
    val sampleRate: Int,      // 采样率
    val channels: Int,        // 通道数
    val bitsPerSample: Int,   // 采样位数
    val signed: Boolean,      // 是否有符号
    val bigEndian: Boolean    // 是否大端序
) {
    /**
     * 计算指定时长的音频数据大小（字节）
     * @param durationMs 时长（毫秒）
     * @return 字节数
     */
    fun calculateBufferSize(durationMs: Int): Int {
        val bytesPerSample = bitsPerSample / 8
        val samplesPerMs = sampleRate / 1000
        return durationMs * samplesPerMs * channels * bytesPerSample
    }
} 