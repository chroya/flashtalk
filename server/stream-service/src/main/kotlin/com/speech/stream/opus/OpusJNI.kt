package com.speech.stream.opus

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

/**
 * Opus JNI接口
 */
object OpusJNI {
    private val logger = LoggerFactory.getLogger(OpusJNI::class.java)
    
    init {
        loadNativeLibrary()
    }

    private fun loadNativeLibrary() {
        try {
            // 确定操作系统类型
            val osName = System.getProperty("os.name").lowercase()
            val (osDir, libPrefix, libSuffix) = when {
                osName.contains("mac") -> Triple("macos", "lib", ".dylib")
                osName.contains("linux") -> Triple("linux", "lib", ".so")
                else -> throw UnsupportedOperationException("Unsupported operating system: $osName")
            }

            // 构建库文件名
            val libraryName = "${libPrefix}opus_jni$libSuffix"
            
            // 从resources中复制库文件到临时目录
            val libraryPath = "/native/$osDir/$libraryName"
            val libraryStream = OpusJNI::class.java.getResourceAsStream(libraryPath)
                ?: throw IllegalStateException("Native library not found: $libraryPath")

            // 创建临时文件
            val tempFile = Files.createTempFile("opus_jni", libSuffix).toFile()
            tempFile.deleteOnExit()

            // 复制库文件到临时文件
            FileOutputStream(tempFile).use { output ->
                libraryStream.copyTo(output)
            }

            // 加载库文件
            System.load(tempFile.absolutePath)
            logger.info("Successfully loaded native library: $libraryPath")
        } catch (e: Exception) {
            logger.error("Failed to load native library", e)
            throw RuntimeException("Failed to load native library", e)
        }
    }

    /**
     * 创建Opus解码器
     * @param sampleRate 采样率
     * @param channels 通道数
     * @return 解码器句柄
     */
    @JvmStatic
    external fun createDecoder(sampleRate: Int, channels: Int): Long

    /**
     * 销毁Opus解码器
     * @param handle 解码器句柄
     */
    @JvmStatic
    external fun destroyDecoder(handle: Long)

    /**
     * 解码Opus音频数据
     * @param handle 解码器句柄
     * @param input Opus编码数据
     * @param output PCM输出缓冲区
     * @param frameSize 帧大小
     * @return 解码的采样点数
     */
    @JvmStatic
    external fun decode(handle: Long, input: ByteArray, output: ShortArray, frameSize: Int): Int
} 