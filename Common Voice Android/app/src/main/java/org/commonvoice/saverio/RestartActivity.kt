package org.commonvoice.saverio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RestartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }
}