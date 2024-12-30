package com.speech.stream.config

import com.speech.stream.service.AudioProcessor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import java.util.concurrent.ConcurrentHashMap

@Configuration
@EnableWebSocket
open class WebSocketConfig @Autowired constructor(
    private val audioProcessor: AudioProcessor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(AudioWebSocketHandler(audioProcessor), "/audio")
            .setAllowedOrigins("*")  // 允许所有来源，生产环境应该限制
    }
}

class AudioWebSocketHandler(
    private val audioProcessor: AudioProcessor
) : BinaryWebSocketHandler() {
    
    private val logger = LoggerFactory.getLogger(AudioWebSocketHandler::class.java)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("WebSocket connection established: ${session.id}")
        sessions[session.id] = session
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        try {
            val audioData = message.payload.array()
            val timestamp = System.currentTimeMillis()
            
            // 处理音频数据
            audioProcessor.processAudio(audioData, timestamp)
            
            // 可以在这里发送处理结果给客户端
            // session.sendMessage(TextMessage("Processed audio data"))
        } catch (e: Exception) {
            logger.error("Error processing audio data from session ${session.id}", e)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("WebSocket connection closed: ${session.id}, status: $status")
        sessions.remove(session.id)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error for session ${session.id}", exception)
        sessions.remove(session.id)
        try {
            session.close(CloseStatus.SERVER_ERROR)
        } catch (e: Exception) {
            logger.error("Error closing session", e)
        }
    }
} 