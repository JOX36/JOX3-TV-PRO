package com.streamvault.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splash_logo)
        val title = findViewById<TextView>(R.id.splash_title)
        val subtitle = findViewById<TextView>(R.id.splash_subtitle)
        val glowLine = findViewById<View>(R.id.splash_glow_line)
        val particle1 = findViewById<View>(R.id.particle1)
        val particle2 = findViewById<View>(R.id.particle2)
        val particle3 = findViewById<View>(R.id.particle3)

        // Initial state
        logo.alpha = 0f
        logo.scaleX = 0.3f
        logo.scaleY = 0.3f
        title.alpha = 0f
        title.translationY = 50f
        subtitle.alpha = 0f
        subtitle.translationY = 30f
        glowLine.scaleX = 0f
        listOf(particle1, particle2, particle3).forEach {
            it.alpha = 0f
            it.scaleX = 0f
            it.scaleY = 0f
        }

        // Animation sequence
        val handler = Handler(Looper.getMainLooper())

        // 1. Particles burst
        handler.postDelayed({
            animateParticle(particle1, -100f, -200f, 0)
            animateParticle(particle2, 150f, -150f, 100)
            animateParticle(particle3, -50f, -250f, 200)
        }, 200)

        // 2. Logo entrance with glow
        handler.postDelayed({
            val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.3f, 1.1f, 1f)
            val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.3f, 1.1f, 1f)
            val alpha = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)
            val rotation = ObjectAnimator.ofFloat(logo, "rotation", -15f, 0f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alpha, rotation)
                duration = 800
                interpolator = OvershootInterpolator(1.5f)
                start()
            }
        }, 400)

        // 3. Glow line sweep
        handler.postDelayed({
            glowLine.animate()
                .scaleX(1f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }, 800)

        // 4. Title slide in
        handler.postDelayed({
            title.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }, 1200)

        // 5. Subtitle fade in
        handler.postDelayed({
            subtitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .start()
        }, 1600)

        // 6. Navigate to main
        handler.postDelayed({
            // Fade out everything
            val fadeOut = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(title, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(subtitle, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(glowLine, "alpha", 1f, 0f)
                )
                duration = 400
            }
            fadeOut.start()

            handler.postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }, 400)
        }, 3000)
    }

    private fun animateParticle(view: View, transX: Float, transY: Float, delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            view.alpha = 1f
            val moveX = ObjectAnimator.ofFloat(view, "translationX", 0f, transX)
            val moveY = ObjectAnimator.ofFloat(view, "translationY", 0f, transY)
            val scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f, 0f)
            val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f, 0f)
            val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)

            AnimatorSet().apply {
                playTogether(moveX, moveY, scaleUp, scaleUpY, fadeOut)
                duration = 1200
                start()
            }
        }, delay)
    }
}
