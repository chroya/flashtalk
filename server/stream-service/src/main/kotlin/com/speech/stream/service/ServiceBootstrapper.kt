package com.speech.stream.service

import com.speech.stream.rtp.RtpServer
import com.speech.stream.grpc.GrpcServer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * 服务启动器
 * 负责协调各个组件的启动顺序和依赖关系
 */
@Component
class ServiceBootstrapper @Autowired constructor(
    private val rtpServer: RtpServer,
    private val audioProcessor: AudioProcessor,
    private val grpcServer: GrpcServer
) {
    private val logger = LoggerFactory.getLogger(ServiceBootstrapper::class.java)

    /**
     * 在Spring应用程序完全启动后执行
     * 按照正确的顺序启动各个组件
     */
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        try {
            logger.info("Starting Stream Service components...")
            
            // 1. 启动音频处理器
            audioProcessor.start()
            logger.info("Audio processor started successfully")
            
            // 2. 启动RTP服务器
            rtpServer.start()
            logger.info("RTP server started successfully")
            
            // 3. 启动gRPC服务器
            grpcServer.start()
            logger.info("gRPC server started successfully")
            
            logger.info("All components started successfully")
        } catch (e: Exception) {
            logger.error("Failed to start service components", e)
            throw RuntimeException("Service startup failed", e)
        }
    }
} 