/*
 * Copyright © Marc Auberer 2020. All rights reserved
 */

package com.chillibits.splashscreen

import android.app.Activity
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class SplashActivity : AppCompatActivity() {

    // Variables as objects
    private lateinit var textureView: TextureView
    private lateinit var player: MediaPlayer
    private lateinit var appIcon: ImageView
    private lateinit var appName: TextView
    private lateinit var poweredBy: TextView
    private lateinit var handler: Handler
    private lateinit var fadeIn: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize calling intent
        val i = intent

        // Initialize handler
        handler = Handler()

        // Initialize MediaPlayer
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val videoUri = Uri.parse("android.resource://$packageName/" + if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) i.getIntExtra(SplashScreenBuilder.VIDEO_ID, 0) else i.getIntExtra(SplashScreenBuilder.VIDEO_ID_DARK, 0))
        player = MediaPlayer.create(this, videoUri)

        // Initialize components
        val container = findViewById<ConstraintLayout>(R.id.container)
        if (i.hasExtra(SplashScreenBuilder.SKIP_ON_TAP) && i.getBooleanExtra(SplashScreenBuilder.SKIP_ON_TAP, true)) {
            container.setOnClickListener {
                player.stop()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        // Initialize TextureView
        textureView = findViewById(R.id.app_icon_animation)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                player.setSurface(Surface(surface))
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
        appIcon = findViewById(R.id.app_icon)
        appIcon.setImageResource(i.getIntExtra(SplashScreenBuilder.IMAGE_ID, 0))
        appName = findViewById(R.id.app_title)
        if (i.hasExtra(SplashScreenBuilder.TITLE) && !i.getStringExtra(SplashScreenBuilder.TITLE)!!.isEmpty()) appName.text = i.getStringExtra(SplashScreenBuilder.TITLE)
        poweredBy = findViewById(R.id.app_powered)
        if (i.hasExtra(SplashScreenBuilder.SUBTITLE) && !i.getStringExtra(SplashScreenBuilder.SUBTITLE)!!.isEmpty()) poweredBy.text = i.getStringExtra(SplashScreenBuilder.SUBTITLE)

        // Initialize fade-in textureView
        fadeIn = AnimationUtils.loadAnimation(this@SplashActivity, android.R.anim.fade_in)

        textureView.bringToFront()
        player.setOnPreparedListener { mediaPlayer ->
            player.seekTo(0)
            player.start()
            mediaPlayer.setOnInfoListener { _, what, extra ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) appIcon.visibility = View.VISIBLE
                false
            }
        }
        player.setOnCompletionListener {
            textureView.visibility = View.GONE
            player.stop()
            if (i.hasExtra(SplashScreenBuilder.SKIP_IMAGE) && i.getBooleanExtra(SplashScreenBuilder.SKIP_IMAGE, false)) {
                // If image skipping was set, finish the activity
                setResult(Activity.RESULT_OK)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else {
                // Fade in the text slowly
                fadeIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        handler.postDelayed({
                            setResult(Activity.RESULT_OK)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }, (if (i.hasExtra(SplashScreenBuilder.TEXT_FADE_IN_DURATION)) i.getIntExtra(SplashScreenBuilder.TEXT_FADE_IN_DURATION, 1000) else 1000).toLong())
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                appName.startAnimation(fadeIn)
                poweredBy.startAnimation(fadeIn)
                appName.visibility = View.VISIBLE
                poweredBy.visibility = View.VISIBLE
            }
        }
        textureView.requestFocus()
    }
}