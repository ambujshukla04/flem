\
#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>

using namespace cv;
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "native-lib", __VA_ARGS__))

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_opencvgl_NativeLib_initNative(JNIEnv *env, jclass clazz) {
    LOGI("native init called");
    return JNI_TRUE;
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_opencvgl_NativeLib_processFrameNV21(JNIEnv *env, jclass clazz,
                                                     jbyteArray nv21Data,
                                                     jint width, jint height,
                                                     jint lowThreshold, jint highThreshold) {
    jbyte *nv21 = env->GetByteArrayElements(nv21Data, 0);
    Mat yuv(height + height/2, width, CV_8UC1, (unsigned char *)nv21);
    Mat bgr;
    cvtColor(yuv, bgr, COLOR_YUV2BGR_NV21);
    Mat gray;
    cvtColor(bgr, gray, COLOR_BGR2GRAY);
    Mat edges;
    Canny(gray, edges, lowThreshold, highThreshold);
    Mat rgba;
    cvtColor(edges, rgba, COLOR_GRAY2RGBA);
    int outSize = rgba.total() * rgba.elemSize();
    jbyteArray outArray = env->NewByteArray(outSize);
    env->SetByteArrayRegion(outArray, 0, outSize, (jbyte*)rgba.data);
    env->ReleaseByteArrayElements(nv21Data, nv21, 0);
    return outArray;
}
}
