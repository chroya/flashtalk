#!/bin/bash
# server/scripts/build_native.sh

# 设置变量
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/.."
CPP_DIR="$PROJECT_ROOT/stream-service/src/main/cpp"
RESOURCES_DIR="$PROJECT_ROOT/stream-service/src/main/resources/native"

# 创建构建目录
cd "$CPP_DIR"
rm -rf build
mkdir build
cd build

# 构建
cmake ..
make

# 复制库文件到resources目录
if [[ "$OSTYPE" == "darwin"* ]]; then
    mkdir -p "$RESOURCES_DIR/macos"
    cp lib/libopus_jni.dylib "$RESOURCES_DIR/macos/"
elif [[ "$OSTYPE" == "linux"* ]]; then
    mkdir -p "$RESOURCES_DIR/linux"
    cp lib/libopus_jni.so "$RESOURCES_DIR/linux/"
fi