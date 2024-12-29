cmake_minimum_required(VERSION 3.10)
project(opus_jni)

# 查找JNI
find_package(JNI REQUIRED)

# 在macOS上查找brew安装的opus
if(APPLE)
    # 使用pkg-config查找opus
    find_package(PkgConfig REQUIRED)
    pkg_check_modules(OPUS REQUIRED opus)
    
    # 设置opus库的具体路径
    set(OPUS_LIBRARY_PATH "/opt/homebrew/Cellar/opus/1.5.2/lib/libopus.dylib")
    
    if(NOT EXISTS ${OPUS_LIBRARY_PATH})
        # 如果找不到上面的路径，尝试intel mac的路径
        set(OPUS_LIBRARY_PATH "/usr/local/Cellar/opus/1.5.2/lib/libopus.dylib")
    endif()
    
    if(NOT EXISTS ${OPUS_LIBRARY_PATH})
        message(FATAL_ERROR "Could not find opus library. Please install it with: brew install opus")
    endif()
else()
    # Linux上的查找方式
    find_package(PkgConfig REQUIRED)
    pkg_check_modules(OPUS REQUIRED opus)
endif()

# 添加库
add_library(opus_jni SHARED
    opus_jni.cpp
)

# 包含目录
target_include_directories(opus_jni PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${OPUS_INCLUDE_DIRS}
)

# 链接目录
if(APPLE)
    # 在macOS上直接使用完整的库路径
    target_link_libraries(opus_jni PRIVATE
        ${OPUS_LIBRARY_PATH}
    )
else()
    link_directories(${OPUS_LIBRARY_DIRS})
    target_link_libraries(opus_jni PRIVATE
        ${OPUS_LIBRARIES}
    )
endif()

# 设置输出目录
set_target_properties(opus_jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
)

# 设置rpath以便在macOS上能找到opus库
if(APPLE)
    set_target_properties(opus_jni PROPERTIES
        BUILD_WITH_INSTALL_RPATH TRUE
        INSTALL_RPATH "@loader_path;@loader_path/../lib;${OPUS_LIBRARY_DIRS}"
    )
endif()

# 打印调试信息
message(STATUS "JNI include dirs: ${JNI_INCLUDE_DIRS}")
message(STATUS "Opus include dirs: ${OPUS_INCLUDE_DIRS}")
message(STATUS "Opus library dirs: ${OPUS_LIBRARY_DIRS}")
message(STATUS "Opus libraries: ${OPUS_LIBRARIES}")
if(APPLE)
    message(STATUS "Opus library path: ${OPUS_LIBRARY_PATH}")
endif() 