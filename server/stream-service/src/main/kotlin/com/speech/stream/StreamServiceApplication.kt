package com.speech.stream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * 音频流处理服务的主启动类
 * 
 * 该服务负责：
 * 1. 接收RTP音频流
 * 2. 解码Opus音频
 * 3. 将音频转发给ASR服务
 */
@SpringBootApplication
@EnableAsync  // 启用异步处理
class StreamServiceApplication

/**
 * 主函数，服务入口点
 */
fun main(args: Array<String>) {
    runApplication<StreamServiceApplication>(*args)
}

/**
 * 线程池配置类
 */
@Configuration
class AsyncConfig {
    
    /**
     * 配置异步任务执行器
     * 用于处理音频流和转码等耗时操作
     */
    @Bean(name = ["audioProcessingExecutor"])
    fun audioProcessingExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 4  // 核心线程数
            maxPoolSize = 8   // 最大线程数
            queueCapacity = 100  // 队列容量
            setThreadNamePrefix("Audio-")  // 线程名前缀
            initialize()
        }
    }
} 