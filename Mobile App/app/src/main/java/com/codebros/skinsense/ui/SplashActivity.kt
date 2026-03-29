package com.codebros.skinsense.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.codebros.skinsense.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_splash)

            val logoIcon = findViewById<ImageView>(R.id.splashLogo)
            val appName = findViewById<TextView>(R.id.splashAppName)
            val tagline = findViewById<TextView>(R.id.splashTagline)

            // Animate logo
            val scaleAnimation = ScaleAnimation(
                0.5f, 1.0f, 0.5f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 800
                fillAfter = true
            }

            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 800
                fillAfter = true
            }

            val animationSet = AnimationSet(true).apply {
                addAnimation(scaleAnimation)
                addAnimation(fadeIn)
            }

            logoIcon.startAnimation(animationSet)

            // Delayed fade in for text
            val textFadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 600
                startOffset = 400
                fillAfter = true
            }
            appName.startAnimation(textFadeIn)

            val taglineFadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 600
                startOffset = 700
                fillAfter = true
            }
            tagline.startAnimation(taglineFadeIn)
        } catch (t: Throwable) {
            Log.e(TAG, "Splash animation error", t)
        }

        // Always navigate to main after delay, even if animation fails
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start MainActivity", t)
                finish()
            }
        }, 2500)
    }
}
