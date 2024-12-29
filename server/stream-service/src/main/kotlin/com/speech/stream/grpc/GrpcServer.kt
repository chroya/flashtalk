package com.speech.stream.grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
class GrpcServer(
    @Value("\${grpc.server.port:50051}")
    private val port: Int
) {
    private var server: Server? = null
    private val logger = LoggerFactory.getLogger(GrpcServer::class.java)

    fun start() {
        server = ServerBuilder.forPort(port)
            // .addService(streamService) // TODO: 添加gRPC服务实现
            .build()
            .start()
        logger.info("gRPC Server started on port $port")
    }

    @PreDestroy
    fun stop() {
        server?.shutdown()
        logger.info("gRPC Server stopped")
    }
} 