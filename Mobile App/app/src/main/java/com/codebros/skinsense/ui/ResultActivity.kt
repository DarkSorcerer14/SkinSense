package com.codebros.skinsense.ui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codebros.skinsense.R
import com.codebros.skinsense.data.SkinConditionInfo
import com.codebros.skinsense.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    companion object {
        private const val TAG = "ResultActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityResultBinding.inflate(layoutInflater)
            setContentView(binding.root)

            loadResults()
            setupUI()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load results", t)
            Toast.makeText(this, "Error displaying results: ${t.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnScanAgain.setOnClickListener {
            finish()
        }
    }

    private fun loadResults() {
        val label = intent.getStringExtra("label") ?: "unknown"
        val displayName = intent.getStringExtra("displayName") ?: "Unknown"
        val confidence = intent.getFloatExtra("confidence", 0f)
        val imageUri = intent.getStringExtra("imageUri")
        val imagePath = intent.getStringExtra("imagePath")
        val allLabels = intent.getStringArrayExtra("allLabels") ?: emptyArray()
        val allConfidences = intent.getFloatArrayExtra("allConfidences") ?: floatArrayOf()

        // Load image
        try {
            when {
                imagePath != null -> {
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    binding.ivSkinImage.setImageBitmap(bitmap)
                }
                imageUri != null -> {
                    binding.ivSkinImage.setImageURI(Uri.parse(imageUri))
                }
            }
        } catch (e: Exception) {
            // Use placeholder
        }

        // Set main result
        binding.tvConditionName.text = displayName
        binding.tvConfidence.text = "${(confidence * 100).toInt()}% confidence"
        binding.progressConfidence.progress = (confidence * 100).toInt()

        // Set color based on severity
        val conditionInfo = SkinConditionInfo.getConditionInfo(label)
        val severityColor = Color.parseColor(conditionInfo.severity.color)
        binding.progressConfidence.progressTintList = android.content.res.ColorStateList.valueOf(severityColor)
        binding.tvSeverity.text = "Severity: ${conditionInfo.severity.displayName}"
        binding.tvSeverity.setTextColor(severityColor)

        // Description
        binding.tvDescription.text = conditionInfo.description

        // Doctor advice
        binding.tvDoctorAdvice.text = conditionInfo.doctorAdvice

        // Symptoms
        binding.layoutSymptoms.removeAllViews()
        for (symptom in conditionInfo.symptoms) {
            val symptomView = TextView(this).apply {
                text = "• $symptom"
                setTextColor(Color.parseColor("#E0E0E0"))
                textSize = 14f
                setPadding(0, 4, 0, 4)
            }
            binding.layoutSymptoms.addView(symptomView)
        }

        // Remedies
        binding.layoutRemedies.removeAllViews()
        for (remedy in conditionInfo.remedies) {
            val remedyLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_remedy_card)
                setPadding(32, 24, 32, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 16)
                layoutParams = params
            }

            val nameView = TextView(this).apply {
                text = remedy.name
                setTextColor(Color.WHITE)
                textSize = 15f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val descView = TextView(this).apply {
                text = remedy.description
                setTextColor(Color.parseColor("#B0B0B0"))
                textSize = 13f
                setPadding(0, 4, 0, 4)
            }

            val costView = TextView(this).apply {
                text = "Est. Cost: ${remedy.estimatedCost}"
                setTextColor(Color.parseColor("#81C784"))
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            remedyLayout.addView(nameView)
            remedyLayout.addView(descView)
            remedyLayout.addView(costView)
            binding.layoutRemedies.addView(remedyLayout)
        }

        // All predictions
        binding.layoutAllPredictions.removeAllViews()
        for (i in allLabels.indices) {
            val predLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val labelView = TextView(this).apply {
                text = allLabels[i]
                setTextColor(Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val percentView = TextView(this).apply {
                val pct = if (i < allConfidences.size) (allConfidences[i] * 100).toInt() else 0
                text = "$pct%"
                setTextColor(Color.parseColor("#B0B0B0"))
                textSize = 14f
                setPadding(16, 0, 16, 0)
            }

            val progressView = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT)
                max = 100
                progress = if (i < allConfidences.size) (allConfidences[i] * 100).toInt() else 0
                progressTintList = android.content.res.ColorStateList.valueOf(
                    if (i == 0) severityColor else Color.parseColor("#616161")
                )
            }

            predLayout.addView(labelView)
            predLayout.addView(percentView)
            predLayout.addView(progressView)
            binding.layoutAllPredictions.addView(predLayout)
        }

        // Disclaimer
        binding.tvDisclaimer.text = "⚠️ DISCLAIMER: This is a screening tool only. It does NOT provide medical diagnosis. Always consult a qualified dermatologist for proper diagnosis and treatment."
    }
}
