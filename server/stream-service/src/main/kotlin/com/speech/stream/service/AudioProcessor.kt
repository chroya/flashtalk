package com.speech.stream.service

import com.speech.stream.opus.OpusDecoder
import com.speech.stream.rtp.RtpPacket
import com.speech.stream.rtp.RtpServer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 音频处理器接口
 * 定义音频处理的基本操作
 */
interface AudioProcessor {
    /**
     * 启动音频处理
     */
    fun start()
    
    /**
     * 停止音频处理
     */
    fun stop()
    
    /**
     * 处理音频数据
     * @param audioData 音频数据字节数组
     * @param timestamp 时间戳
     */
    fun processAudio(audioData: ByteArray, timestamp: Long)
}

/**
 * 音频处理器实现
 */
@Service
class AudioProcessorImpl @Autowired constructor(
    private val rtpServer: RtpServer,
    private val opusDecoder: OpusDecoder
) : AudioProcessor {
    private val running = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger(AudioProcessorImpl::class.java)

    override fun start() {
        if (running.compareAndSet(false, true)) {
            startProcessing()
            logger.info("Audio processor started")
        }
    }

    override fun stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Audio processor stopped")
        }
    }

    override fun processAudio(audioData: ByteArray, timestamp: Long) {
        try {
            // 解码Opus音频
            val pcmData = opusDecoder.decode(audioData)
            if (pcmData.isEmpty()) {
                logger.warn("Failed to decode audio data")
                return
            }

            // TODO: 发送到ASR服务
            logger.debug("Processed audio data: ${pcmData.size} bytes at timestamp $timestamp")
        } catch (e: Exception) {
            logger.error("Error processing audio data", e)
        }
    }

    @Async("audioProcessingExecutor")
    protected fun startProcessing() {
        logger.info("Starting audio processing loop")
        
        while (running.get()) {
            try {
                // 获取RTP包
                val packet = rtpServer.getNextPacket()
                
                // 处理音频数据
                processAudio(packet.payload, packet.timestamp)
            } catch (e: InterruptedException) {
                logger.info("Audio processing interrupted")
                break
            } catch (e: Exception) {
                logger.error("Error in audio processing loop", e)
                Thread.sleep(100)  // 避免在错误情况下过度循环
            }
        }
        
        logger.info("Audio processing loop stopped")
    }
} 