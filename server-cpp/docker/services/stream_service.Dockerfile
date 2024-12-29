FROM ubuntu:22.04

# 安装依赖
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    git \
    pkg-config \
    libgrpc++-dev \
    libprotobuf-dev \
    protobuf-compiler \
    protobuf-compiler-grpc \
    libopus-dev \
    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 复制源代码
COPY . .

# 创建构建目录
RUN mkdir -p build

# 构建项目
WORKDIR /app/build
RUN cmake .. && \
    make -j$(nproc)

# 暴露端口
EXPOSE 50051
EXPOSE 5004/udp

# 运行服务
CMD ["./services/stream_service/stream_service"] 