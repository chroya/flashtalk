# 生成 protobuf 文件
get_target_property(grpc_cpp_plugin_location grpc_cpp_plugin LOCATION)
get_target_property(protoc_location protoc LOCATION)

# 生成 proto 文件
add_custom_command(
    OUTPUT 
        ${CMAKE_CURRENT_BINARY_DIR}/speech_service.pb.cc
        ${CMAKE_CURRENT_BINARY_DIR}/speech_service.pb.h
        ${CMAKE_CURRENT_BINARY_DIR}/speech_service.grpc.pb.cc
        ${CMAKE_CURRENT_BINARY_DIR}/speech_service.grpc.pb.h
    COMMAND ${protoc_location}
        --grpc_out=${CMAKE_CURRENT_BINARY_DIR}
        --cpp_out=${CMAKE_CURRENT_BINARY_DIR}
        --plugin=protoc-gen-grpc=${grpc_cpp_plugin_location}
        -I${CMAKE_SOURCE_DIR}/common/proto
        ${CMAKE_SOURCE_DIR}/common/proto/speech_service.proto
)

# 查找 Live555 库
find_package(Live555 REQUIRED)

# 添加可执行文件
add_executable(stream_service
    src/main.cpp
    src/stream_service.cpp
    src/rtp/rtp_server.cpp
    src/rtp/rtp_source.cpp
    ${CMAKE_CURRENT_BINARY_DIR}/speech_service.pb.cc
    ${CMAKE_CURRENT_BINARY_DIR}/speech_service.grpc.pb.cc
)

# 添加包含目录
target_include_directories(stream_service PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_BINARY_DIR}
    ${LIVE555_INCLUDE_DIRS}
)

# 链接库
target_link_libraries(stream_service PRIVATE
    ${LIVE555_LIBRARIES}
    opus
    grpc++
    protobuf
) 