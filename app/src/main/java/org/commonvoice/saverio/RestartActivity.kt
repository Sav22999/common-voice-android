package org.commonvoice.saverio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import org.commonvoice.saverio.databinding.ActivityRestartBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.schedule

class RestartActivity : ViewBoundActivity<ActivityRestartBinding>(
    ActivityRestartBinding::inflate
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.start)
        if (mainPrefManager.areAnimationsEnabled) {
            binding.imgIconStart.startAnimation(animation)
        }

        if (mainPrefManager.hasLanguageChanged) {
            Timer("Restart", false).schedule(1000) {
                restart()
            }
        } else {
            Timer("Start", false).schedule(500) {
                start()
            }
        }
    }

    override fun onBackPressed() {
        //
    }

    private fun restart() {
        mainPrefManager.hasLanguageChanged = false

        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun start() {
        finish()
    }
}