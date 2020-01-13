package org.commonvoice.saverio

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.schedule

class RestartActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val UI_LANGUAGE_CHANGED = "UI_LANGUAGE_CHANGED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restart)

        val img: ImageView = this.findViewById(R.id.imgIconStart)
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.start)
        img.startAnimation(animation)

        val sharedPref: SharedPreferences = getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE)
        var restart: Boolean = sharedPref.getBoolean(UI_LANGUAGE_CHANGED, true)
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
        val sharedPref: SharedPreferences = getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putBoolean(UI_LANGUAGE_CHANGED, false)
        editor.apply()

        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun start() {
        //finish()
        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
}