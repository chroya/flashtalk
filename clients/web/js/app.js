class AudioStreamer {
    constructor() {
        this.recorder = null;
        this.socket = null;
        this.sequence = 0;
    }

    async init() {
        try {
            // 配置Opus录音机
            this.recorder = new Recorder({
                encoderPath: "https://cdn.jsdelivr.net/npm/opus-recorder@8.0.5/dist/encoderWorker.min.js",
                encoderApplication: 2048,
                streamPages: true,
                numberOfChannels: 1,
                encoderSampleRate: 16000,
                originalSampleRate: 16000
            });

            // 处理音频数据
            this.recorder.ondataavailable = (typedArray) => {
                if (this.socket && this.socket.readyState === WebSocket.OPEN) {
                    // 创建RTP包
                    const rtpPacket = this.createRTPPacket(typedArray);
                    this.socket.send(rtpPacket);
                }
            };

            await this.connectWebSocket();
            this.updateStatus('已就绪');
        } catch (error) {
            this.updateStatus('初始化失败: ' + error.message, true);
        }
    }

    async connectWebSocket() {
        return new Promise((resolve, reject) => {
            this.socket = new WebSocket('ws://localhost:8080/audio');
            
            this.socket.onopen = () => {
                this.updateStatus('WebSocket已连接');
                resolve();
            };
            
            this.socket.onerror = (error) => {
                this.updateStatus('WebSocket错误: ' + error.message, true);
                reject(error);
            };
            
            this.socket.onclose = () => {
                this.updateStatus('WebSocket已断开');
            };
            
            this.socket.onmessage = (event) => {
                const response = JSON.parse(event.data);
                this.updateStatus('收到响应: ' + response.message);
            };
        });
    }

    createRTPPacket(opusData) {
        // RTP头部 (12字节)
        const buffer = new ArrayBuffer(12 + opusData.length);
        const view = new DataView(buffer);
        
        // 1. 版本(2位)=2, 填充(1位)=0, 扩展(1位)=0, CSRC计数(4位)=0
        view.setUint8(0, 0x80);
        
        // 2. 标记(1位)=0, 负载类型(7位)=111(Opus)
        view.setUint8(1, 111);
        
        // 3-4. 序列号(16位)
        view.setUint16(2, this.sequence++);
        
        // 5-8. 时间戳(32位)
        view.setUint32(4, Date.now());
        
        // 9-12. SSRC(32位)
        view.setUint32(8, 0x12345678);
        
        // 添加Opus数据
        new Uint8Array(buffer, 12).set(opusData);
        
        return buffer;
    }

    async start() {
        try {
            await this.recorder.start();
            this.updateStatus('录音已开始');
            document.getElementById('startButton').disabled = true;
            document.getElementById('stopButton').disabled = false;
        } catch (error) {
            this.updateStatus('启动录音失败: ' + error.message, true);
        }
    }

    async stop() {
        try {
            await this.recorder.stop();
            this.updateStatus('录音已停止');
            document.getElementById('startButton').disabled = false;
            document.getElementById('stopButton').disabled = true;
        } catch (error) {
            this.updateStatus('停止录音失败: ' + error.message, true);
        }
    }

    updateStatus(message, isError = false) {
        const statusDiv = document.getElementById('status');
        statusDiv.textContent = message;
        statusDiv.className = 'status ' + (isError ? 'error' : 'success');
    }
}

// 初始化应用
const streamer = new AudioStreamer();

document.addEventListener('DOMContentLoaded', () => {
    streamer.init();
    
    document.getElementById('startButton').onclick = () => streamer.start();
    document.getElementById('stopButton').onclick = () => streamer.stop();
}); 