package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import org.commonvoice.saverio.databinding.FirstRunSpeakBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.koin.android.ext.android.inject

class FirstRunSpeak : ViewBoundActivity<FirstRunSpeakBinding>(
    FirstRunSpeakBinding::inflate
) {

    private val firstRunPrefManager: FirstRunPrefManager by inject()

    private var status: Int = 0
    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.seekBarFirstRunSpeak.isEnabled = false
        binding.seekBarFirstRunSpeak.progress = 0

        goNextOrBack()
        binding.btnNextSpeak.onClick {
            goNextOrBack()
        }

        binding.layoutFirstRunSpeak.setOnTouchListener(object :
            OnSwipeTouchListener(this@FirstRunSpeak) {
            override fun onSwipeLeft() {
                goNextOrBack(true)
            }

            override fun onSwipeRight() {
                goNextOrBack(false)
            }
        })

        setTheme()
    }

    fun setTheme() = withBinding {
        theme.setElements(this@FirstRunSpeak, firstRunSpeakSectionBottom)
        theme.setElement(this@FirstRunSpeak, 1, firstRunSpeakSectionBottom)
        theme.setElement(layoutFirstRunSpeak)
        theme.setElement(this@FirstRunSpeak, btnNextSpeak)
        theme.setElement(
            this@FirstRunSpeak,
            seekBarFirstRunSpeak,
            R.color.colorBackground,
            R.color.colorBackgroundDT
        )
    }

    override fun onBackPressed() {
        finish()
    }

    fun goNextOrBack(next: Boolean = true) = withBinding {
        if (status in 0..(binding.seekBarFirstRunSpeak.max)) {
            btnNumberBottomSpeak.isGone = true
            txtTutorialMessageBottomSpeak.isGone = true
            btnNumberTopSpeak.isGone = true
            txtTutorialMessageTopSpeak.isGone = true
            btnOneSpeak.isGone = true
            btnTwoSpeak.isGone = true
            btnFourSpeak.isGone = true
            btnThreeSpeak.isGone = true
            btnEightSpeak.isGone = true
            btnNineSpeak.isGone = true
            imgBtnRecordSpeak.setImageResource(R.drawable.speak_cv)
            imgBtnListenAgainSpeak.isGone = true
            imgBtnSendSpeak.isGone = true
        }

        if (next) seekBarFirstRunSpeak.progress = status
        else if (!next && status > 1) this.seekBarFirstRunSpeak.progress = status - 2

        if (status == 0 || status == 2 && !next || status == 1 && !next) {
            status = 1
            btnNextSpeak.setText(R.string.btn_tutorial1)
            btnNumberBottomSpeak.text = "1"
            txtTutorialMessageBottomSpeak.setText(R.string.txt1_tutorial_speak)
            btnNumberBottomSpeak.isGone = false
            txtTutorialMessageBottomSpeak.isGone = false
            btnOneSpeak.isGone = false
            stopAnimation(btnTwoSpeak)
            startAnimation(btnOneSpeak, R.anim.zoom_in)
        } else if (status == 1 || status == 3 && !next) {
            status = 2
            btnNextSpeak.setText(R.string.btn_tutorial3)
            btnNumberTopSpeak.text = "2"
            txtTutorialMessageTopSpeak.setText(R.string.txt2_tutorial_speak_and_listen)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnTwoSpeak.isGone = false
            stopAnimation(btnOneSpeak)
            stopAnimation(btnThreeSpeak)
            startAnimation(btnTwoSpeak, R.anim.zoom_in)
        } else if (status == 2 || status == 4 && !next) {
            status = 3
            btnNextSpeak.setText(R.string.btn_tutorial3)
            btnNumberTopSpeak.text = "3"
            txtTutorialMessageTopSpeak.setText(R.string.txt3_tutorial_speak)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnThreeSpeak.isGone = false
            stopAnimation(btnTwoSpeak)
            stopAnimation(btnFourSpeak)
            startAnimation(btnThreeSpeak, R.anim.zoom_in)
        } else if (status == 3 || status == 5 && !next) {
            status = 4
            btnNextSpeak.setText(R.string.btn_tutorial3)
            btnNumberTopSpeak.text = "4"
            txtTutorialMessageTopSpeak.setText(R.string.txt4_tutorial_speak)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnFourSpeak.isGone = false
            btnFourSpeak.text = "4"
            stopAnimation(btnThreeSpeak)
            stopAnimation(btnFourSpeak)
            startAnimation(btnFourSpeak, R.anim.zoom_in)
        } else if (status == 4 || status == 6 && !next) {
            status = 5
            btnNextSpeak.setText(R.string.btn_tutorial3)
            btnNumberTopSpeak.text = "5"
            txtTutorialMessageTopSpeak.setText(R.string.txt5_tutorial_speak)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnFourSpeak.isGone = false
            btnFourSpeak.text = "5"
            imgBtnRecordSpeak.setImageResource(R.drawable.stop_cv)
            stopAnimation(btnFourSpeak)
            startAnimation(btnFourSpeak, R.anim.zoom_in)
        } else if (status == 5 || status == 7 && !next) {
            status = 6
            btnNextSpeak.setText(R.string.btn_tutorial3)
            btnNumberTopSpeak.text = "6"
            txtTutorialMessageTopSpeak.setText(R.string.txt6_tutorial_speak)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnFourSpeak.isGone = false
            btnFourSpeak.text = "6"
            imgBtnRecordSpeak.setImageResource(R.drawable.listen2_cv)
            stopAnimation(btnFourSpeak)
            startAnimation(btnFourSpeak, R.anim.zoom_in)
        } else if (status == 6 || status == 8 && !next) {
            status = 7
            btnNextSpeak.setText(R.string.btn_tutorial3)
            btnNumberTopSpeak.text = "7"
            txtTutorialMessageTopSpeak.setText(R.string.txt7_tutorial_speak)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnFourSpeak.isGone = false
            btnFourSpeak.text = "7"
            imgBtnListenAgainSpeak.isGone = false
            imgBtnSendSpeak.isGone = false
            imgBtnRecordSpeak.setImageResource(R.drawable.speak2_cv)
            stopAnimation(btnFourSpeak)
            stopAnimation(btnEightSpeak)
            startAnimation(btnFourSpeak, R.anim.zoom_in)
        } else if (status == 7 || status == 9 && !next) {
            status = 8
            btnNextSpeak.setText(R.string.btn_tutorial3)
            btnNumberBottomSpeak.text = "8"
            btnNumberTopSpeak.text = "9"
            txtTutorialMessageTopSpeak.setText(R.string.txt8_tutorial_speak)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnNumberBottomSpeak.isGone = true
            txtTutorialMessageBottomSpeak.isGone = true
            btnEightSpeak.isGone = false
            btnNineSpeak.isGone = true
            imgBtnListenAgainSpeak.isGone = false
            imgBtnSendSpeak.isGone = false
            imgBtnRecordSpeak.setImageResource(R.drawable.speak2_cv)
            stopAnimation(btnFourSpeak)
            stopAnimation(btnNineSpeak)
            startAnimation(btnEightSpeak, R.anim.zoom_in)
        } else if (status == 8 || status == 10 && !next) {
            status = 9
            btnNextSpeak.setText(R.string.btn_tutorial5)
            btnNumberTopSpeak.text = "9"
            txtTutorialMessageTopSpeak.setText(R.string.txt9_tutorial_speak)
            btnNumberTopSpeak.isGone = false
            txtTutorialMessageTopSpeak.isGone = false
            btnNumberBottomSpeak.isGone = true
            txtTutorialMessageBottomSpeak.isGone = true
            btnNineSpeak.isGone = false
            imgBtnListenAgainSpeak.isGone = false
            imgBtnRecordSpeak.setImageResource(R.drawable.speak2_cv)
            imgBtnSendSpeak.isGone = false
            stopAnimation(btnEightSpeak)
            startAnimation(btnNineSpeak, R.anim.zoom_in)
        } else if (status == 9) {
            firstRunPrefManager.speak = false
            if (ContextCompat.checkSelfPermission(
                    this@FirstRunSpeak,
                    Manifest.permission.RECORD_AUDIO
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@FirstRunSpeak,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_REQUEST_CODE
                )
            } else {
                openActualSpeakSection()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openActualSpeakSection()
                }
            }
        }
    }

    private fun openActualSpeakSection() {
        Intent(this, SpeakActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

}