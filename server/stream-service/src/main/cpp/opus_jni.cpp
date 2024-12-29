#include <jni.h>
#include <opus.h>
#include <cstring>

// 错误处理宏
#define HANDLE_ERROR(env, error) \
    if (error < 0) { \
        jclass exception = env->FindClass("java/lang/RuntimeException"); \
        env->ThrowNew(exception, opus_strerror(error)); \
        return; \
    }

// 保存Opus解码器实例的结构体
typedef struct {
    OpusDecoder* decoder;
    int channels;
    int sampleRate;
} OpusDecoderWrapper;

// JNI方法实现
extern "C" {

JNIEXPORT jlong JNICALL
Java_com_speech_stream_opus_OpusJNI_createDecoder(
    JNIEnv* env, jclass, jint sampleRate, jint channels) {
    
    int error;
    OpusDecoder* decoder = opus_decoder_create(sampleRate, channels, &error);
    
    if (error != OPUS_OK || decoder == NULL) {
        jclass exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exception, opus_strerror(error));
        return 0;
    }

    OpusDecoderWrapper* wrapper = new OpusDecoderWrapper();
    wrapper->decoder = decoder;
    wrapper->channels = channels;
    wrapper->sampleRate = sampleRate;

    return reinterpret_cast<jlong>(wrapper);
}

JNIEXPORT void JNICALL
Java_com_speech_stream_opus_OpusJNI_destroyDecoder(
    JNIEnv* env, jclass, jlong handle) {
    
    if (handle != 0) {
        OpusDecoderWrapper* wrapper = reinterpret_cast<OpusDecoderWrapper*>(handle);
        opus_decoder_destroy(wrapper->decoder);
        delete wrapper;
    }
}

JNIEXPORT jint JNICALL
Java_com_speech_stream_opus_OpusJNI_decode(
    JNIEnv* env, jclass, jlong handle, jbyteArray input, jshortArray output, jint frameSize) {
    
    OpusDecoderWrapper* wrapper = reinterpret_cast<OpusDecoderWrapper*>(handle);
    
    // 获取输入数据
    jbyte* inputBuffer = env->GetByteArrayElements(input, NULL);
    jsize inputLength = env->GetArrayLength(input);
    
    // 获取输出缓冲区
    jshort* outputBuffer = env->GetShortArrayElements(output, NULL);
    
    // 解码
    int samplesDecoded = opus_decode(
        wrapper->decoder,
        reinterpret_cast<const unsigned char*>(inputBuffer),
        inputLength,
        outputBuffer,
        frameSize,
        0
    );
    
    // 释放缓冲区
    env->ReleaseByteArrayElements(input, inputBuffer, JNI_ABORT);
    env->ReleaseShortArrayElements(output, outputBuffer, 0);
    
    if (samplesDecoded < 0) {
        jclass exception = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exception, opus_strerror(samplesDecoded));
        return -1;
    }
    
    return samplesDecoded;
}

} 