package com.speech.stream.rtp

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * RTP服务器
 * 负责接收和处理RTP音频流
 */
@Component
class RtpServer {
    @Value("\${rtp.port:5004}")
    private var port: Int = 5004

    @Value("\${rtp.buffer-size:2048}")
    private var bufferSize: Int = 2048

    private val group = NioEventLoopGroup()
    private var channel: Channel? = null
    private val packetQueue = LinkedBlockingQueue<RtpPacket>(bufferSize)
    private val logger = LoggerFactory.getLogger(RtpServer::class.java)

    @PostConstruct
    fun start() {
        try {
            val b = Bootstrap()
            b.group(group)
                .channel(NioDatagramChannel::class.java)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, 
                       io.netty.channel.FixedRecvByteBufAllocator(65535))
                .handler(object : ChannelInitializer<NioDatagramChannel>() {
                    override fun initChannel(ch: NioDatagramChannel) {
                        ch.pipeline().apply {
                            addLast(RtpPacketDecoder())
                            addLast(RtpServerHandler(packetQueue))
                        }
                    }
                })

            channel = b.bind(port).sync().channel()
            logger.info("RTP server started on port $port")
        } catch (e: Exception) {
            logger.error("Failed to start RTP server", e)
            throw e
        }
    }

    @PreDestroy
    fun stop() {
        channel?.close()
        group.shutdownGracefully()
        logger.info("RTP server stopped")
    }

    /**
     * 获取下一个音频包
     * @return RTP包，如果队列为空则等待
     */
    fun getNextPacket(): RtpPacket = packetQueue.take()

    /**
     * 尝试获取下一个音频包，不阻塞
     * @return RTP包，如果队列为空则返回null
     */
    fun tryGetNextPacket(): RtpPacket? = packetQueue.poll()
} 