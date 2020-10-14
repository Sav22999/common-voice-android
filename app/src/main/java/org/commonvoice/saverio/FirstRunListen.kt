package org.commonvoice.saverio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.isGone
import org.commonvoice.saverio.databinding.FirstRunListenBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.koin.android.ext.android.inject


class FirstRunListen : ViewBoundActivity<FirstRunListenBinding>(
  FirstRunListenBinding::inflate  
) {

    private val firstRunPrefManager: FirstRunPrefManager by inject()

    private var status: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.seekBarFirstRunListen.isEnabled = false
        binding.seekBarFirstRunListen.progress = 0

        goNextOrBack()
        binding.btnNextListen.setOnClickListener {
            goNextOrBack()
        }

        binding.layoutFirstRunListen.setOnTouchListener(object :
            OnSwipeTouchListener(this@FirstRunListen) {
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
        theme.setElements(this@FirstRunListen, firstRunListenSectionBottom)
        theme.setElement(this@FirstRunListen, 1, firstRunListenSectionBottom)
        theme.setElement(layoutFirstRunListen)
        theme.setElements(this@FirstRunListen, layoutFirstRunListenNoSmartphone)
        theme.setElement(layoutFirstRunListenNoSmartphone)
        theme.setElement(this@FirstRunListen, btnReadNowGuidelinesListen)
        theme.setElement(this@FirstRunListen, btnNextListen)
        theme.setElement(
            this@FirstRunListen,
            seekBarFirstRunListen,
            R.color.colorBackground,
            R.color.colorBackgroundDT
        )
    }

    override fun onBackPressed() {
        finish()
    }

    fun goNextOrBack(next: Boolean = true) = withBinding {
        if (status in 0..8) {
            btnNumberBottomListen.isGone = true
            txtTutorialMessageBottomListen.isGone = true
            btnNumberTopListen.isGone = true
            txtTutorialMessageTopListen.isGone = true
            btnOneListen.isGone = true
            btnTwoListen.isGone = true
            btnThreeListen.isGone = true
            btnFourListen.isGone = true
            btnSevenListen.isGone = true
            btnEightListen.isGone = true
            imgBtnPlayListen.setImageResource(R.drawable.listen_cv)
            imgBtnYesListen.isGone = true
            imgBtnNoListen.isGone = true
        }

        if (next) seekBarFirstRunListen.progress = status
        else if (!next && status > 1) this.seekBarFirstRunListen.progress = status - 2

        if (status == 0 && next || status == 2 && !next || status == 1 && !next) {
            status = 1
            btnNextListen.setText(R.string.btn_tutorial1)
            btnNumberBottomListen.text = "1"
            txtTutorialMessageBottomListen.setText(R.string.txt1_tutorial_listen)
            btnNumberBottomListen.isGone = false
            txtTutorialMessageBottomListen.isGone = false
            btnOneListen.isGone = false
            stopAnimation(btnTwoListen)
            startAnimation(btnOneListen, R.anim.zoom_in)
        } else if (status == 1 && next || status == 3 && !next) {
            status = 2
            btnNextListen.setText(R.string.btn_tutorial3)
            btnNumberTopListen.text = "2"
            txtTutorialMessageTopListen.setText(R.string.txt2_tutorial_speak_and_listen)
            btnNumberTopListen.isGone = false
            txtTutorialMessageTopListen.isGone = false
            btnTwoListen.isGone = false
            stopAnimation(btnOneListen)
            stopAnimation(btnThreeListen)
            startAnimation(btnTwoListen, R.anim.zoom_in)
        } else if (status == 2 || status == 4 && !next) {
            status = 3
            btnNextListen.setText(R.string.btn_tutorial3)
            btnNumberTopListen.text = "3"
            txtTutorialMessageTopListen.setText(R.string.txt3_tutorial_listen)
            btnNumberTopListen.isGone = false
            txtTutorialMessageTopListen.isGone = false
            btnThreeListen.isGone = false
            stopAnimation(btnTwoListen)
            stopAnimation(btnFourListen)
            startAnimation(btnThreeListen, R.anim.zoom_in)
        } else if (status == 3 || status == 5 && !next) {
            status = 4
            btnNextListen.setText(R.string.btn_tutorial3)
            btnNumberTopListen.text = "4"
            txtTutorialMessageTopListen.setText(R.string.txt4_tutorial_listen)
            btnNumberTopListen.isGone = false
            txtTutorialMessageTopListen.isGone = false
            btnFourListen.isGone = false
            stopAnimation(btnThreeListen)
            stopAnimation(btnFourListen)
            startAnimation(btnFourListen, R.anim.zoom_in)
        } else if (status == 4 || status == 6 && !next) {
            status = 5
            btnNextListen.setText(R.string.btn_tutorial3)
            btnNumberTopListen.text = "5"
            txtTutorialMessageTopListen.setText(R.string.txt5_tutorial_listen)
            btnNumberTopListen.isGone = false
            txtTutorialMessageTopListen.isGone = false
            btnFourListen.isGone = false
            btnFourListen.text = "5"
            imgBtnPlayListen.setImageResource(R.drawable.stop_cv)
            stopAnimation(btnFourListen)
            startAnimation(btnFourListen, R.anim.zoom_in)
        } else if (status == 5 || status == 7 && !next) {
            status = 6
            btnNextListen.setText(R.string.btn_tutorial3)
            btnNumberTopListen.text = "6"
            txtTutorialMessageTopListen.setText(R.string.txt6_tutorial_listen)
            btnNumberTopListen.isGone = false
            txtTutorialMessageTopListen.isGone = false
            btnFourListen.isGone = false
            btnFourListen.text = "6"
            imgBtnPlayListen.setImageResource(R.drawable.listen2_cv)
            imgBtnYesListen.isGone = false
            imgBtnNoListen.isGone = false
            stopAnimation(btnFourListen)
            stopAnimation(btnSevenListen)
            startAnimation(btnFourListen, R.anim.zoom_in)
        } else if (status == 6 || status == 8 && !next) {
            status = 7
            btnNextListen.setText(R.string.btn_tutorial3)
            btnNumberTopListen.text = "7"
            txtTutorialMessageTopListen.setText(R.string.txt7_tutorial_listen)
            btnNumberTopListen.isGone = false
            txtTutorialMessageTopListen.isGone = false
            btnSevenListen.isGone = false
            imgBtnPlayListen.setImageResource(R.drawable.listen2_cv)
            imgBtnYesListen.isGone = false
            imgBtnNoListen.isGone = false
            stopAnimation(btnFourListen)
            stopAnimation(btnEightListen)
            startAnimation(btnSevenListen, R.anim.zoom_in)
        } else if (status == 7 || status == 9 && !next) {
            status = 8
            btnNextListen.setText(R.string.btn_tutorial3)
            btnNumberTopListen.text = "8"
            txtTutorialMessageTopListen.setText(R.string.txt8_tutorial_listen)
            btnNumberTopListen.isGone = false
            txtTutorialMessageTopListen.isGone = false
            btnEightListen.isGone = false
            imgBtnPlayListen.setImageResource(R.drawable.listen2_cv)
            imgBtnYesListen.isGone = false
            imgBtnNoListen.isGone = false
            layoutFirstRunListenNoSmartphone.isGone = true
            stopAnimation(btnSevenListen)
            stopAnimation(btnReadNowGuidelinesListen)
            startAnimation(btnEightListen, R.anim.zoom_in)
        } else if (status == 8 || status == 10 && !next) {
            status = 9
            btnNextListen.setText(R.string.btn_tutorial5)
            layoutFirstRunListenNoSmartphone.isGone = false
            stopAnimation(btnEightListen)
            startAnimation(btnReadNowGuidelinesListen, R.anim.zoom_in_first_launch)
            btnReadNowGuidelinesListen.setOnClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://mzl.la/2Z5OxEQ"))
                startActivity(browserIntent)
            }
        } else if (status == 9) {
            firstRunPrefManager.listen = false
            Intent(this@FirstRunListen, ListenActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }

}