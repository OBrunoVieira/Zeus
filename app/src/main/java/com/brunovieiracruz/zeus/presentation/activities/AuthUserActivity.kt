package com.brunovieiracruz.zeus.presentation.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewAnimationUtils.createCircularReveal
import android.view.animation.AccelerateDecelerateInterpolator
import com.brunovieiracruz.zeus.R

class AuthUserActivity : BaseActivity() {

    private lateinit var rootContent: View
    private var revealX: Int = 0
    private var revealY: Int = 0

    companion object {
        private const val DURATION = 600L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_user)
        rootContent = findViewById(R.id.activity_auth_user_content_root)

        setupRootContentPresentation()
    }


    private fun setupRootContentPresentation() {
        if (SDK_INT >= LOLLIPOP) {
            revealActivity()
        } else {
            rootContent.visibility = VISIBLE
        }
    }

    @TargetApi(LOLLIPOP)
    private fun revealActivity() {
        rootContent.post {
            revealX = ((rootContent.x + rootContent.width) / 2).toInt()
            revealY = ((rootContent.y + rootContent.height)).toInt()

            val finalRadius = Math.max(rootContent.width, rootContent.height) * 1.1f
            val circularReveal = createCircularReveal(rootContent, revealX, revealY, 0f, finalRadius)
            circularReveal.duration = DURATION
            circularReveal.interpolator = AccelerateDecelerateInterpolator()

            rootContent.visibility = VISIBLE
            circularReveal.start()
        }
    }

    @TargetApi(LOLLIPOP)
    private fun unRevealActivity() {
        val finalRadius = Math.max(rootContent.width, rootContent.height) * 1.1f
        val circularReveal = createCircularReveal(rootContent, revealX, revealY, finalRadius, 0f)

        circularReveal.duration = DURATION
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                rootContent.visibility = INVISIBLE
                finish()
            }
        })
        circularReveal.start()
    }
}