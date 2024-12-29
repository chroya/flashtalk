package com.speech.stream.rtp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class RtpServer {
    @Value("${rtp.port:5004}")
    private int port;

    private final EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;
    private final BlockingQueue<RtpPacket> packetQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void start() {
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RtpPacketDecoder());
                        pipeline.addLast(new RtpServerHandler(packetQueue));
                    }
                });

            channel = b.bind(port).sync().channel();
            log.info("RTP server started on port {}", port);
        } catch (Exception e) {
            log.error("Failed to start RTP server", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
        log.info("RTP server stopped");
    }

    public RtpPacket getNextPacket() throws InterruptedException {
        return packetQueue.take();
    }
} 