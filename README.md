# 语音指令系统

一个基于Opus编解码和WebSocket的实时语音处理系统。

## 系统要求

### Windows
- JDK 11+
- Maven 3.6+
- CMake 3.10+
- Visual Studio 2019+ (带C++支持)
- vcpkg

### macOS
- JDK 11+
- Maven 3.6+
- CMake 3.10+
- Xcode Command Line Tools
- Homebrew

### Linux
- JDK 11+
- Maven 3.6+
- CMake 3.10+
- GCC/G++ 7.0+
- pkg-config

## 安装步骤

### 1. 安装依赖

#### Windows
```bash
# 安装vcpkg
git clone https://github.com/Microsoft/vcpkg.git
cd vcpkg
.\bootstrap-vcpkg.bat
.\vcpkg integrate install

# 安装opus
.\vcpkg install opus:x64-windows
```

#### macOS
```bash
# 安装必要工具
brew install cmake opus pkg-config
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y build-essential cmake pkg-config libopus-dev
```

### 2. 编译JNI库

#### Windows
```bash
cd server/stream-service/src/main/cpp
mkdir build && cd build
cmake -DCMAKE_TOOLCHAIN_FILE=[vcpkg root]/scripts/buildsystems/vcpkg.cmake ..
cmake --build . --config Release
mkdir -p ../../resources/native/windows
copy Release\opus_jni.dll ..\..\resources\native\windows\
```

#### macOS
```bash
cd server/stream-service/src/main/cpp
mkdir build && cd build
cmake ..
make
mkdir -p ../../resources/native/macos
cp lib/libopus_jni.dylib ../../resources/native/macos/
```

#### Linux
```bash
cd server/stream-service/src/main/cpp
mkdir build && cd build
cmake ..
make
mkdir -p ../../resources/native/linux
cp lib/libopus_jni.so ../../resources/native/linux/
```

### 3. 构建Java项目

所有平台:
```bash
cd server
mvn clean install
```

### 4. 运行服务

所有平台:
```bash
cd server/stream-service
mvn spring-boot:run
```

### 5. 测试Web客户端

所有平台:
```bash
cd clients/web
python3 -m http.server 8000
```

访问 [http://localhost:8000](http://localhost:8000)

## 常见问题

### Windows
- **找不到opus.h**: 检查vcpkg安装路径和CMake工具链文件设置
- **链接错误**: 确保使用x64构建配置

### macOS
- **找不到opus.h**: 运行 `brew doctor`
- **链接错误**: 检查 `otool -L lib/libopus_jni.dylib`

### Linux
- **找不到opus.h**: 确保libopus-dev已安装
- **链接错误**: 检查 `ldd lib/libopus_jni.so`

## 项目结构

```
speech-command-system/
├── clients/          # 客户端实现
│   └── web/         # Web客户端
└── server/          # 服务端实现
    ├── common/      # 公共模块
    └── stream-service/ # 流处理服务
```

## 验证部署

1. 启动服务后，检查日志确认无错误
2. 打开Web客户端，点击"开始录音"
3. 说话测试
4. 检查服务端日志确认收到音频数据

如遇问题，请查看服务日志或提交issue。

## 开发说明

### 环境配置
- IDE推荐：IntelliJ IDEA、Visual Studio Code
- JDK版本：11或更高
- Maven版本：3.6或更高

### 调试技巧
- 使用JNI调试：`-Xcheck:jni`
- 查看详细日志：设置日志级别为DEBUG
- 使用Wireshark抓包分析RTP流

## 贡献指南

1. Fork本仓库
2. 创建特性分支
3. 提交更改
4. 发起Pull Request

## 许可证

[MIT License](LICENSE)