cmake_minimum_required(VERSION 3.10.2)
project("opencvgl")

# If you have OpenCV Android SDK, set OpenCV_DIR to its sdk/native/jni folder.
# Example:
# set(OpenCV_DIR "/path/to/OpenCV-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED COMPONENTS core imgproc)

add_library(native-lib SHARED src/main/cpp/native-lib.cpp)

include_directories(${OpenCV_INCLUDE_DIRS})
target_link_libraries(native-lib ${OpenCV_LIBS} log)
