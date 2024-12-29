#!/bin/bash

# 创建第三方库目录
mkdir -p third_party
cd third_party

# 下载并构建 Live555
git clone https://github.com/rgaufman/live555.git
cd live555
./genMakefiles linux-64bit
make -j$(nproc)
cd ..

# 下载 Opus（CMake 会处理构建）
git clone https://github.com/xiph/opus.git

# 返回根目录
cd .. 