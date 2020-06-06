package org.commonvoice.saverio

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_listen.*
import kotlinx.android.synthetic.main.activity_speak.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class ListenActivity : VariableLanguageActivity(R.layout.activity_listen) {

    private val listenViewModel: ListenViewModel by stateViewModel()

    private val connectionManager: ConnectionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        setupUI()

        connectionManager.liveInternetAvailability.observe(this, Observer { available ->
            if (!listenViewModel.showingHidingAirplaneIcon && (listenViewModel.airplaneModeIconVisible == available)) {
                listenViewModel.showingHidingAirplaneIcon = true
                if (!available) {
                    this.startAnimation(this.imageAirplaneModeListen, R.anim.zoom_in)
                    listenViewModel.airplaneModeIconVisible = true
                } else {
                    this.startAnimation(this.imageAirplaneModeListen, R.anim.zoom_out_speak_listen)
                    listenViewModel.airplaneModeIconVisible = false
                }
                listenViewModel.showingHidingAirplaneIcon = false
                this.imageAirplaneModeListen.isGone = available
            }
        })
    }

    private fun setupInitialUIState() {
        buttonSkipListen.onClick {
            listenViewModel.skipClip()
        }

        buttonYesClip.isGone = true
        buttonNoClip.isGone = true
    }

    private fun setupUI() {
        listenViewModel.currentClip.observe(this, Observer { clip ->
            loadUIStateStandby(clip)
        })

        listenViewModel.state.observe(this, Observer { state ->
            when (state) {
                ListenViewModel.Companion.State.STANDBY -> {
                    loadUIStateLoading()
                    listenViewModel.loadNewClip()
                }
                ListenViewModel.Companion.State.LISTENING -> {
                    loadUIStateListening()
                }
                ListenViewModel.Companion.State.LISTENED -> {
                    loadUIStateListened()
                }
            }
        })
    }

    private fun showMessageDialog(title: String, text: String) {
        MessageDialog(this, 0, title, text, details = "").show()
    }

    private fun setupGestures() {
        nestedScrollListen.setOnTouchListener(object : OnSwipeTouchListener(this@ListenActivity) {
            override fun onSwipeLeft() {
                listenViewModel.skipClip()
            }

            override fun onSwipeRight() {
                onBackPressed()
            }

            override fun onSwipeTop() {
                if (mainPrefManager.deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    openReportDialog()
                }
            }
        })
    }

    fun setTheme(view: Context) {
        val theme: DarkLightTheme = DarkLightTheme()

        val isDark = theme.getTheme(view)
        theme.setElement(isDark, layoutListen)
        theme.setElement(isDark, view, 1, listenSectionBottom)
        theme.setElement(
            isDark,
            view,
            textMessageAlertListen,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
        theme.setElement(isDark, view, buttonReportListen, background = false)
        theme.setElement(isDark, view, buttonSkipListen)
    }

    private fun openReportDialog() {

    }

    private fun loadUIStateLoading() {
        if (!listenViewModel.stopped) {
            textSentenceListen.text = "..."
            textMessageAlertListen.setText(R.string.txt_loading_sentence)
            buttonStartStopListen.isEnabled = false
            buttonReportListen.isGone = true
        }
        //buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
        if (!listenViewModel.opened) {
            listenViewModel.opened = true
            startAnimation(buttonStartStopListen, R.anim.zoom_in_speak_listen)
            startAnimation(buttonSkipListen, R.anim.zoom_in_speak_listen)
        }
    }

    private fun loadUIStateStandby(clip: Clip) {
        textSentenceListen.text = clip.sentence.sentenceText
        if (!listenViewModel.listenedOnce) textMessageAlertListen.setText(R.string.txt_press_icon_below_listen_1)
        else textMessageAlertListen.setText(R.string.txt_clip_correct_or_wrong)

        //buttonReportListen.isGone = false

        buttonStartStopListen.isEnabled = true
        buttonStartStopListen.onClick {
            listenViewModel.startListening()
        }

        if (mainPrefManager.areGesturesEnabled) {
            setupGestures()
        }

        if (listenViewModel.stopped) {
            //stopped recording
            buttonStartStopListen.setBackgroundResource(R.drawable.listen2_cv)
        } else {
            buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)

            hideButtons()

            listenViewModel.listenedOnce = false
            listenViewModel.startedOnce = false
        }

        setTheme(this)
    }

    private fun loadUIStateListening() {
        stopButtons()

        textMessageAlertListen.setText(R.string.txt_press_icon_below_listen_2)

        buttonNoClip.isVisible = true
        if (!listenViewModel.startedOnce) startAnimation(buttonNoClip, R.anim.zoom_in_speak_listen)
        if (!listenViewModel.listenedOnce) buttonYesClip.isVisible = false
        listenViewModel.startedOnce = true

        buttonStartStopListen.setBackgroundResource(R.drawable.stop_cv)

        buttonNoClip.onClick {
            listenViewModel.validate(result = false)
        }
        buttonStartStopListen.onClick {
            listenViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() {
        buttonYesClip.isVisible = true
        buttonNoClip.isVisible = true
        if (!listenViewModel.listenedOnce) startAnimation(buttonYesClip, R.anim.zoom_in_speak_listen)
        listenViewModel.listenedOnce = true

        textMessageAlertListen.setText(R.string.txt_clip_correct_or_wrong)

        buttonStartStopListen.setBackgroundResource(R.drawable.listen2_cv)

        buttonYesClip.onClick {
            listenViewModel.validate(result = true)
        }
        buttonStartStopListen.onClick {
            listenViewModel.startListening()
        }
    }

    override fun onBackPressed() {
        textMessageAlertListen.setText(R.string.txt_closing)
        buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
        textSentenceListen.text = "..."
        buttonReportListen.isGone = true
        buttonStartStopListen.isEnabled = false
        buttonSkipListen.isEnabled = false
        buttonYesClip.isGone = true
        buttonNoClip.isGone = true

        listenViewModel.stop()

        super.onBackPressed()
    }

    private fun hideButtons() {
        stopButtons()
        buttonYesClip.isEnabled = false
        buttonNoClip.isEnabled = false
        if (listenViewModel.startedOnce) startAnimation(buttonNoClip, R.anim.zoom_out_speak_listen)
        if (listenViewModel.listenedOnce) startAnimation(buttonYesClip, R.anim.zoom_out_speak_listen)
        buttonYesClip.isVisible = false
        buttonNoClip.isVisible = false
        buttonYesClip.isEnabled = true
        buttonNoClip.isEnabled = true
    }

    private fun stopButtons() {
        stopAnimation(buttonNoClip)
        stopAnimation(buttonYesClip)
    }

    private fun startAnimation(img: ImageView, zoomType: Int) {
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, zoomType)
        img.startAnimation(animation)
    }

    private fun startAnimation(btn: Button, zoomType: Int) {
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, zoomType)
        btn.startAnimation(animation)
    }

    private fun stopAnimation(img: ImageView) {
        img.clearAnimation()
    }

    private fun stopAnimation(btn: Button) {
        btn.clearAnimation()
    }
}