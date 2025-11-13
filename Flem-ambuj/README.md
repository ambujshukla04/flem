# OpenCV-GL Android Assignment (scaffold)

This scaffold contains:
- Android app (Kotlin) with Camera2 -> JNI -> OpenCV (Canny) -> OpenGL ES renderer skeleton
- Native C++ code (CMake) for OpenCV processing
- Simple TypeScript web viewer (compiled JS included)

**Notes**
- You must install Android NDK and provide OpenCV Android SDK or prebuilt libs.
- Update `app/CMakeLists.txt` with your OpenCV path if needed.
- This is a scaffold focused on integration. Some device-specific adjustments (YUV handling) may be required.

Build:
1. Open `opencv_gl_project` in Android Studio.
2. Ensure NDK is installed and `local.properties` points to sdk/ndk if needed.
3. Build and run on a device (minSdk 21).

