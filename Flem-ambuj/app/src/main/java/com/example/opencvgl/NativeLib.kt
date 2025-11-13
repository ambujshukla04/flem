package com.example.opencvgl

object NativeLib {
    init { System.loadLibrary("native-lib") }
    external fun initNative(): Boolean
    external fun processFrameNV21(nv21: ByteArray, width: Int, height: Int, lowThreshold: Int, highThreshold: Int): ByteArray
}
