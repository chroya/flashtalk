class AudioRecorder {
    constructor() {
        this.ws = null;
        this.mediaRecorder = null;
        this.audioContext = null;
        this.analyser = null;
        this.dataArray = null;
        this.isRecording = false;

        // DOM元素
        this.startButton = document.getElementById('startButton');
        this.stopButton = document.getElementById('stopButton');
        this.statusDiv = document.getElementById('status');
        this.audioLevelBar = document.getElementById('audioLevelBar');

        // 检查浏览器支持
        this.checkBrowserSupport();

        // 绑定事件处理器
        this.startButton.onclick = () => this.initAudioAndStart();
        this.stopButton.onclick = () => this.stopRecording();

        // 初始化WebSocket
        this.initWebSocket();
    }

    checkBrowserSupport() {
        // 检查必要的API是否可用
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            this.showError('您的浏览器不支持音频录制。请使用最新版本的Chrome、Firefox或Safari。');
            this.startButton.disabled = true;
            return false;
        }

        // 检查MediaRecorder API
        if (typeof MediaRecorder === 'undefined') {
            this.showError('您的浏览器不支持MediaRecorder API。请使用最新版本的Chrome、Firefox或Safari。');
            this.startButton.disabled = true;
            return false;
        }

        // 检查WebSocket支持
        if (!window.WebSocket) {
            this.showError('您的浏览器不支持WebSocket。请使用最新版本的浏览器。');
            this.startButton.disabled = true;
            return false;
        }

        return true;
    }

    showError(message) {
        this.statusDiv.textContent = message;
        this.statusDiv.className = 'disconnected';
        console.error(message);
    }

    initWebSocket() {
        try {
            this.ws = new WebSocket('ws://localhost:8080/audio');
            
            this.ws.onopen = () => {
                this.statusDiv.textContent = '已连接到服务器';
                this.statusDiv.className = 'connected';
                this.startButton.disabled = false;
            };
            
            this.ws.onclose = () => {
                this.statusDiv.textContent = '与服务器断开连接';
                this.statusDiv.className = 'disconnected';
                this.startButton.disabled = true;
                this.stopButton.disabled = true;
                
                // 5秒后尝试重连
                setTimeout(() => this.initWebSocket(), 5000);
            };
            
            this.ws.onerror = (error) => {
                console.error('WebSocket错误:', error);
                this.showError('WebSocket连接错误');
            };
        } catch (error) {
            this.showError('WebSocket初始化失败: ' + error.message);
        }
    }

    async initAudioAndStart() {
        if (!this.checkBrowserSupport()) return;

        try {
            // 首先请求音频权限
            const stream = await navigator.mediaDevices.getUserMedia({ 
                audio: {
                    channelCount: 1,
                    sampleRate: 48000,
                    echoCancellation: true,
                    noiseSuppression: true
                }
            });

            // 在用户交互后初始化AudioContext
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            this.audioContext = new AudioContext();

            // 设置音频处理管道
            await this.setupAudioPipeline(stream);

            // 开始录音
            this.startRecording(stream);
        } catch (error) {
            this.showError('初始化音频失败: ' + error.message);
        }
    }

    async setupAudioPipeline(stream) {
        // 创建音频源
        const source = this.audioContext.createMediaStreamSource(stream);
        
        // 创建分析器
        this.analyser = this.audioContext.createAnalyser();
        this.analyser.fftSize = 256;
        
        // 连接节点
        source.connect(this.analyser);
        
        // 创建数据数组
        this.dataArray = new Uint8Array(this.analyser.frequencyBinCount);
    }

    startRecording(stream) {
        try {
            // 创建MediaRecorder
            const options = {
                mimeType: 'audio/webm;codecs=opus'
            };

            this.mediaRecorder = new MediaRecorder(stream, options);

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0 && this.ws && this.ws.readyState === WebSocket.OPEN) {
                    this.ws.send(event.data);
                }
            };

            // 开始录音
            this.mediaRecorder.start(100); // 每100ms发送一次数据
            this.isRecording = true;
            
            // 更新UI
            this.startButton.disabled = true;
            this.stopButton.disabled = false;
            this.statusDiv.textContent = '正在录音...';
            
            // 开始音量可视化
            this.visualize();
        } catch (error) {
            this.showError('启动录音失败: ' + error.message);
        }
    }

    stopRecording() {
        if (this.mediaRecorder && this.isRecording) {
            this.mediaRecorder.stop();
            this.isRecording = false;
            
            // 停止所有音轨
            this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
            
            // 关闭音频上下文
            if (this.audioContext) {
                this.audioContext.close();
                this.audioContext = null;
            }
            
            // 更新UI
            this.startButton.disabled = false;
            this.stopButton.disabled = true;
            this.statusDiv.textContent = '已停止录音';
            this.audioLevelBar.style.width = '0%';
        }
    }

    visualize() {
        if (!this.isRecording || !this.analyser || !this.dataArray) return;

        this.analyser.getByteFrequencyData(this.dataArray);
        const average = this.dataArray.reduce((a, b) => a + b) / this.dataArray.length;
        const volume = (average / 255) * 100;
        this.audioLevelBar.style.width = volume + '%';

        requestAnimationFrame(() => this.visualize());
    }
}

// 等待用户交互后再创建AudioRecorder实例
document.addEventListener('DOMContentLoaded', () => {
    const startButton = document.getElementById('startButton');
    const statusDiv = document.getElementById('status');
    
    // 初始状态
    startButton.disabled = true;
    statusDiv.textContent = '点击"开始录音"按钮开始';
    
    // 用户点击开始按钮时才创建AudioRecorder实例
    startButton.onclick = () => {
        startButton.onclick = null; // 移除这个临时的点击处理器
        const recorder = new AudioRecorder();
        // 立即触发录音开始
        recorder.initAudioAndStart();
    };
    
    // 启用开始按钮
    startButton.disabled = false;
}); 