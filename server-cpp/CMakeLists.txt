cmake_minimum_required(VERSION 3.15)
project(speech_command_system)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 添加 cmake 模块路径
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/cmake)

# 设置编译选项
add_compile_options(-Wall -Wextra)

# 设置第三方库的路径
set(THIRD_PARTY_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party)

# 添加第三方库的 CMake 配置
include(FetchContent)
# gRPC
FetchContent_Declare(
    grpc
    GIT_REPOSITORY https://github.com/grpc/grpc.git
    GIT_TAG v1.54.0
)
FetchContent_MakeAvailable(grpc)

# Opus
FetchContent_Declare(
    opus
    GIT_REPOSITORY https://github.com/xiph/opus.git
    GIT_TAG v1.3.1
)
FetchContent_MakeAvailable(opus)

# Live555
# 注意：Live555需要特殊处理，创建一个cmake模块文件

# 添加子目录
add_subdirectory(services)
add_subdirectory(common) 