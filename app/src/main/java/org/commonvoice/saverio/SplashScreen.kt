package org.commonvoice.saverio

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import org.commonvoice.saverio.databinding.SplashScreenBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity

class SplashScreen : ViewBoundActivity<SplashScreenBinding>(
    SplashScreenBinding::inflate
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            delay(SPLASH_DELAY)

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {

        private const val SPLASH_DELAY = 2500L //Lowered from 3000L because this method is lagging a bit, I guess

    }

}