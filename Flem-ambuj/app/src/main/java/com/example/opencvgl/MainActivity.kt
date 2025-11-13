package com.example.opencvgl

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.opengl.GLSurfaceView
import androidx.core.app.ActivityCompat
import kotlin.concurrent.thread
import android.widget.FrameLayout
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView
    private lateinit var glView: GLSurfaceView
    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private lateinit var renderer: GLRenderer

    private val CAMERA_REQUEST = 101
    private val width = 640
    private val height = 480

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // layout: TextureView behind GLSurfaceView
        val layout = FrameLayout(this)
        textureView = TextureView(this)
        glView = GLSurfaceView(this)
        glView.setEGLContextClientVersion(2)
        renderer = GLRenderer()
        glView.setRenderer(renderer)
        layout.addView(textureView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        layout.addView(glView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(layout)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
            return
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            finish()
        }
    }

    private fun startCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            val chosenSize = Size(width, height)
            imageReader = ImageReader.newInstance(chosenSize.width, chosenSize.height, android.graphics.ImageFormat.YUV_420_888, 2)
            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                val nv21 = imageToNV21(image)
                image.close()
                thread {
                    val processed = NativeLib.processFrameNV21(nv21, chosenSize.width, chosenSize.height, 80, 160)
                    // queue update on GL thread
                    glView.queueEvent {
                        renderer.updateTextureFromRGBA(processed, chosenSize.width, chosenSize.height)
                    }
                    glView.requestRender()
                }
            }, Handler(Looper.getMainLooper()))

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    val surfaces = listOf(imageReader!!.surface)
                    val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequestBuilder.addTarget(imageReader!!.surface)
                    camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            captureSession = session
                            val request = captureRequestBuilder.build()
                            session.setRepeatingRequest(request, null, Handler(Looper.getMainLooper()))
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }, Handler(Looper.getMainLooper()))
                }
                override fun onDisconnected(camera: CameraDevice) { camera.close() }
                override fun onError(camera: CameraDevice, error: Int) { camera.close() }
            }, Handler(Looper.getMainLooper()))
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun imageToNV21(image: Image): ByteArray {
        val width = image.width
        val height = image.height
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.buffer.get(nv21, 0, ySize)
        val chromaPixelStride = uPlane.pixelStride
        val vBuffer = ByteArray(vSize)
        vPlane.buffer.get(vBuffer)
        val uBuffer = ByteArray(uSize)
        uPlane.buffer.rewind(); uPlane.buffer.get(uBuffer)
        var pos = ySize
        for (i in 0 until uSize step chromaPixelStride) {
            nv21[pos++] = vBuffer[i]
            nv21[pos++] = uBuffer[i]
        }
        return nv21
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice?.close()
        imageReader?.close()
    }
}
