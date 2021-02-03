package com.fakhry.opencv

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fakhry.opencv.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var baseLoaderCallback: BaseLoaderCallback
    private lateinit var binding: ActivityMainBinding
    private var counter = 0
    private var startCanny = false
    private var startYolo = false
    private var firstTimeYolo = false
    private lateinit var tinyYolo: Net
//    private lateinit var cascFile: File

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(binding.root)

        checkCameraPermission()
        openCameraWithOpenCV()

        binding.btnEdgeDetection.setOnClickListener {
            startCanny = if (!startCanny) !startCanny else !startCanny
        }

        binding.btnYolo.setOnClickListener {
            if (!startYolo) {
                startYolo = !startYolo

                if (!firstTimeYolo) {
                    firstTimeYolo = !firstTimeYolo

                    //IT DOESN'T WORK BECAUSE IDK HOW THE FUCK I CAN ACCESS THE FILE
                    val tinyYoloCfg = "src/main/assets/yolov3-tiny.cfg"
                    val tinyYoloWeights = "src/main/assets/yolov3-tiny.weights"
                    Log.d("asfldas", tinyYoloCfg)
                    Log.d("asfldas", tinyYoloWeights)

                    tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights)
                }
            }
        }
    }

    private fun openCameraWithOpenCV() {
        cameraBridgeViewBase = binding.ocvCamera
        cameraBridgeViewBase.setCvCameraViewListener(object : CvCameraViewListener2 {
            // when you turn on camera
            override fun onCameraViewStarted(width: Int, height: Int) {

            }

            // when camera stopped
            override fun onCameraViewStopped() {
            }

            // when view launch
            override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
                val frame: Mat = inputFrame.rgba()
                // FACE DETECTION
                faceDetection(frame)

                // CANNY - EDGE DETECITON
                edgeDetection(frame)


                /* Flip or mirroring
                if (counter % 2 == 0) {
                    Core.flip(frame, frame, 1)
                }*/
                counter += 1

                return frame
            }
        })

        baseLoaderCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> {
                        cameraBridgeViewBase.enableView()
                    }
                }
                super.onManagerConnected(status)
            }

            private fun read(buffer: ByteArray): Int {
                return 1
            }
        }
    }

    private fun faceDetection(frame: Mat) {
        if (startYolo) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB)
            val imageBlob =
                Dnn.blobFromImage(frame, 0.00392, Size(416.0, 416.0), Scalar(0.0, 0.0, 0.0), false)

            tinyYolo.setInput(imageBlob)
            tinyYolo.forward()
        }
    }

    private fun edgeDetection(frame: Mat) {
        if (startCanny) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY)
            Imgproc.Canny(frame, frame, 150.0, 80.0)
        }
    }

    private fun checkCameraPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    openCameraWithOpenCV()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@MainActivity,
                        "Tidak bisa mengakses kamera dan storage, silakan mengubahnya di setting.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                }
            })
            .check();
    }


    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "There's a problem", Toast.LENGTH_SHORT).show()
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        }
    }

    override fun onPause() {
        super.onPause()
        cameraBridgeViewBase.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraBridgeViewBase.disableView()
    }
/*
    private fun trash() {
        val inputStream =
            resources.openRawResource(R.raw.haarcascade_frontalface_alt2)
        val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
        cascFile = File(cascadeDir, "haarcascade_frontalface_alt2.xml")

        val fileOutputStream = FileOutputStream(cascFile)

//                        val buffer = byteArrayOf(4096.toByte())
//
//                        var bytesRead: Int = readBy(buffer)
//
//                        while ((bytesRead = is.read(buffer)) != -1)

        val fileName = "src/resources/thermopylae.txt"
        val file = File(fileOutputStream.toString())

        val bytes: ByteArray = file.readBytes()

    }

 */
}