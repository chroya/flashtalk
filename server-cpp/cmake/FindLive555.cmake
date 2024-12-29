# FindLive555.cmake

# 设置 Live555 的路径
set(LIVE555_ROOT_DIR ${THIRD_PARTY_DIR}/live555)

# 查找必要的库
find_library(LIVE555_BASIC_USAGE_ENVIRONMENT_LIBRARY
    NAMES BasicUsageEnvironment
    PATHS ${LIVE555_ROOT_DIR}/BasicUsageEnvironment
)

find_library(LIVE555_USAGE_ENVIRONMENT_LIBRARY
    NAMES UsageEnvironment
    PATHS ${LIVE555_ROOT_DIR}/UsageEnvironment
)

find_library(LIVE555_GROUPSOCK_LIBRARY
    NAMES groupsock
    PATHS ${LIVE555_ROOT_DIR}/groupsock
)

find_library(LIVE555_MEDIA_LIBRARY
    NAMES liveMedia
    PATHS ${LIVE555_ROOT_DIR}/liveMedia
)

# 设置包含目录
set(LIVE555_INCLUDE_DIRS
    ${LIVE555_ROOT_DIR}/BasicUsageEnvironment/include
    ${LIVE555_ROOT_DIR}/UsageEnvironment/include
    ${LIVE555_ROOT_DIR}/groupsock/include
    ${LIVE555_ROOT_DIR}/liveMedia/include
)

# 设置所有库
set(LIVE555_LIBRARIES
    ${LIVE555_BASIC_USAGE_ENVIRONMENT_LIBRARY}
    ${LIVE555_USAGE_ENVIRONMENT_LIBRARY}
    ${LIVE555_GROUPSOCK_LIBRARY}
    ${LIVE555_MEDIA_LIBRARY}
)

# 处理标准的 FIND_PACKAGE 参数
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Live555 DEFAULT_MSG
    LIVE555_LIBRARIES
    LIVE555_INCLUDE_DIRS
)

mark_as_advanced(
    LIVE555_INCLUDE_DIRS
    LIVE555_LIBRARIES
) 