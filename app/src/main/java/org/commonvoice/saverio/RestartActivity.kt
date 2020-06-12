package org.commonvoice.saverio

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import org.commonvoice.saverio.ui.VariableLanguageActivity
import java.util.*
import kotlin.concurrent.schedule

class RestartActivity : VariableLanguageActivity(R.layout.activity_restart) {

    private var PRIVATE_MODE = 0
    private val UI_LANGUAGE_CHANGED = "UI_LANGUAGE_CHANGED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val img: ImageView = this.findViewById(R.id.imgIconStart)
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.start)
        if (mainPrefManager.areAnimationsEnabled) {
            img.startAnimation(animation)
        }

        var restart: Boolean = getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE).getBoolean(
            UI_LANGUAGE_CHANGED,
            true
        )
        if (restart) {
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

    fun restart() {
        getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE).edit()
            .putBoolean(UI_LANGUAGE_CHANGED, false).apply()

        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun start() {
        finish()
    }
}