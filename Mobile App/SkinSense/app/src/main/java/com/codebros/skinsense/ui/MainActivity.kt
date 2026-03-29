package com.codebros.skinsense.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codebros.skinsense.R
import com.codebros.skinsense.databinding.ActivityMainBinding
import com.codebros.skinsense.ml.SkinClassifier
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var classifier: SkinClassifier? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    // Gallery picker
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImageUri(it) }
    }

    // Camera permission
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to scan skin", Toast.LENGTH_LONG).show()
        }
    }

    // Storage permission (for older Android)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "Storage permission is required to select images", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to inflate layout", t)
            Toast.makeText(this, "UI Error: ${t.message}", Toast.LENGTH_LONG).show()
            return
        }

        initializeClassifier()
        setupUI()
    }

    private fun initializeClassifier() {
        try {
            classifier = SkinClassifier(this)
            Log.d(TAG, "Classifier initialized successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load AI model", t)
            Toast.makeText(this, "Failed to load AI model: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        // Scan with camera button
        binding.btnScanCamera.setOnClickListener {
            checkCameraPermission()
        }

        // Upload from gallery button
        binding.btnUploadGallery.setOnClickListener {
            checkStoragePermission()
        }

        // Info / About button
        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openGallery()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openGallery()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                else -> {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun launchCamera() {
        try {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to launch camera", t)
            Toast.makeText(this, "Failed to open camera: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        try {
            galleryLauncher.launch("image/*")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to open gallery", t)
            Toast.makeText(this, "Failed to open gallery: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImageUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null && classifier != null) {
                val result = classifier!!.classify(bitmap)

                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra("label", result.label)
                    putExtra("displayName", result.displayName)
                    putExtra("confidence", result.confidence)
                    putExtra("imageUri", uri.toString())

                    val allLabels = result.allResults.map { it.first }.toTypedArray()
                    val allConfidences = result.allResults.map { it.second }.toFloatArray()
                    putExtra("allLabels", allLabels)
                    putExtra("allConfidences", allConfidences)
                }
                startActivity(intent)
            } else if (classifier == null) {
                Toast.makeText(this, "AI model not loaded yet. Please restart app.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error processing image", t)
            Toast.makeText(this, "Error processing image: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        try {
            val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("About Skin Sense")
                .setMessage(
                    "Skin Sense is a privacy-first skin health monitor designed for diverse Indian skin tones.\n\n" +
                    "• 100% on-device processing\n" +
                    "• No cloud uploads\n" +
                    "• Trained on Indian skin tone datasets\n" +
                    "• Powered by MobileNetV3 AI\n\n" +
                    "DISCLAIMER: This app is a screening tool only and does NOT provide medical diagnosis. " +
                    "Always consult a qualified dermatologist for proper diagnosis and treatment.\n\n" +
                    "Built by Code Bros"
                )
                .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
                .create()
            dialog.show()
        } catch (t: Throwable) {
            Log.e(TAG, "Error showing about dialog", t)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            classifier?.close()
        } catch (t: Throwable) {
            Log.e(TAG, "Error closing classifier", t)
        }
    }
}
