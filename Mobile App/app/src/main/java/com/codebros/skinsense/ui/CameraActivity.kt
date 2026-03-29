package com.codebros.skinsense.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.codebros.skinsense.databinding.ActivityCameraBinding
import com.codebros.skinsense.ml.SkinClassifier
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var classifier: SkinClassifier? = null
    private var isProcessing = false

    companion object {
        private const val TAG = "CameraActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityCameraBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (t: Throwable) {
            Log.e(TAG, "Layout inflation failed", t)
            Toast.makeText(this, "UI Error: ${t.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        try {
            classifier = SkinClassifier(this)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load model", t)
            Toast.makeText(this, "Failed to load AI model", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        startCamera()
        setupUI()
    }

    private fun setupUI() {
        binding.btnCapture.setOnClickListener {
            if (!isProcessing) {
                captureAndAnalyze()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }
    }

    private fun startCamera() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    // Preview
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                        }

                    // Image capture with JPEG output
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(windowManager.defaultDisplay.rotation)
                        .build()

                    // Select back camera
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                    Log.d(TAG, "Camera started successfully")
                } catch (t: Throwable) {
                    Log.e(TAG, "Camera binding failed", t)
                    runOnUiThread {
                        Toast.makeText(this, "Failed to start camera: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }, ContextCompat.getMainExecutor(this))
        } catch (t: Throwable) {
            Log.e(TAG, "Camera provider failed", t)
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureAndAnalyze() {
        val imageCapture = imageCapture ?: return
        isProcessing = true

        binding.btnCapture.isEnabled = false
        binding.analysisOverlay.visibility = android.view.View.VISIBLE
        binding.tvAnalyzing.text = "Analyzing skin..."

        // Save to a temp file first, then decode — more reliable than ImageProxy
        val tempFile = java.io.File(cacheDir, "temp_capture.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        // Decode the saved JPEG
                        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)

                        if (bitmap != null && classifier != null) {
                            val result = classifier!!.classify(bitmap)

                            // Save a smaller version for the result screen
                            val scaled = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
                            val resultFile = java.io.File(cacheDir, "captured_image.jpg")
                            val fos = java.io.FileOutputStream(resultFile)
                            scaled.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                            fos.close()

                            val intent = Intent(this@CameraActivity, ResultActivity::class.java).apply {
                                putExtra("label", result.label)
                                putExtra("displayName", result.displayName)
                                putExtra("confidence", result.confidence)
                                putExtra("imagePath", resultFile.absolutePath)

                                val allLabels = result.allResults.map { it.first }.toTypedArray()
                                val allConfidences = result.allResults.map { it.second }.toFloatArray()
                                putExtra("allLabels", allLabels)
                                putExtra("allConfidences", allConfidences)
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@CameraActivity, "Failed to process captured image", Toast.LENGTH_SHORT).show()
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Analysis failed", t)
                        Toast.makeText(this@CameraActivity, "Analysis failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isProcessing = false
                        binding.btnCapture.isEnabled = true
                        binding.analysisOverlay.visibility = android.view.View.GONE
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed", exception)
                    isProcessing = false
                    binding.btnCapture.isEnabled = true
                    binding.analysisOverlay.visibility = android.view.View.GONE
                    Toast.makeText(this@CameraActivity, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun toggleFlash() {
        try {
            imageCapture?.let {
                val currentFlash = it.flashMode
                it.flashMode = if (currentFlash == ImageCapture.FLASH_MODE_ON) {
                    ImageCapture.FLASH_MODE_OFF
                } else {
                    ImageCapture.FLASH_MODE_ON
                }
                binding.btnFlash.alpha = if (it.flashMode == ImageCapture.FLASH_MODE_ON) 1.0f else 0.5f
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Flash toggle failed", t)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            cameraExecutor.shutdown()
            classifier?.close()
        } catch (t: Throwable) {
            Log.e(TAG, "Cleanup error", t)
        }
    }
}
