package org.commonvoice.saverio

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
            this.imageAirplaneModeListen.isGone = available
            if (available) this.startAnimation(this.imageAirplaneModeListen, R.anim.zoom_in)
        })
    }

    private fun setupInitialUIState() {
        btn_skip_listen.onClick {
            listenViewModel.skipClip()
        }

        btn_yes_thumb.isVisible = false
        btn_no_thumb.isVisible = false
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

    private fun setupGestures() {
        nestedScrollSpeak.setOnTouchListener(object : OnSwipeTouchListener(this@ListenActivity) {
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

    private fun openReportDialog() {

    }

    private fun loadUIStateLoading() {
        if(!listenViewModel.stopped) {
            textMessageAlertListen.setText(R.string.txt_loading_sentence)
            textSentenceListen.text = "..."
        }

        btn_start_listen.isEnabled = false
    }

    private fun loadUIStateStandby(clip: Clip) {
        textSentenceListen.text = clip.sentence.sentenceText

        btn_start_listen.isEnabled = true
        btn_start_listen.onClick {
            listenViewModel.startListening()
        }

        if (listenViewModel.stopped) {
            //stopped recording
            btn_start_listen.setBackgroundResource(R.drawable.listen2_cv)
        } else {
            btn_start_listen.setBackgroundResource(R.drawable.listen_cv)

            hideButtons()

            listenViewModel.listenedOnce = false
            listenViewModel.startedOnce = false
        }
    }

    private fun loadUIStateListening() {
        stopButtons()

        btn_no_thumb.isVisible = true
        if (!listenViewModel.startedOnce) startAnimation(btn_no_thumb, R.anim.zoom_in)
        if (!listenViewModel.listenedOnce) btn_yes_thumb.isVisible = false
        listenViewModel.startedOnce = true

        btn_start_listen.setBackgroundResource(R.drawable.stop_cv)

        btn_no_thumb.onClick {
            listenViewModel.validate(result = false)
        }
        btn_start_listen.onClick {
            listenViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() {
        btn_yes_thumb.isVisible = true
        btn_no_thumb.isVisible = true
        if(!listenViewModel.listenedOnce) startAnimation(btn_yes_thumb, R.anim.zoom_in)
        listenViewModel.listenedOnce = true

        btn_start_listen.setBackgroundResource(R.drawable.listen2_cv)

        btn_yes_thumb.onClick {
            listenViewModel.validate(result = true)
        }
        btn_start_listen.onClick {
            listenViewModel.startListening()
        }
    }

    override fun onBackPressed() {
        textMessageAlertListen.setText(R.string.txt_closing)
        btn_start_listen.setBackgroundResource(R.drawable.listen_cv)
        textSentenceListen.text = "..."
        hideButtons()

        listenViewModel.stop()

        super.onBackPressed()
    }

    fun hideButtons() {
        stopButtons()
        btn_yes_thumb.isEnabled = false
        btn_no_thumb.isEnabled = false
        if (listenViewModel.startedOnce) startAnimation(btn_no_thumb, R.anim.zoom_out2)
        if (listenViewModel.listenedOnce) startAnimation(btn_yes_thumb, R.anim.zoom_out2)
        btn_yes_thumb.isVisible = false
        btn_no_thumb.isVisible = false
        btn_yes_thumb.isEnabled = true
        btn_no_thumb.isEnabled = true
    }

    fun stopButtons() {
        stopAnimation(btn_no_thumb)
        stopAnimation(btn_yes_thumb)
    }

    fun startAnimation(img: ImageView, zoomType: Int) {
        var animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, zoomType)
        img.startAnimation(animation)
    }

    fun startAnimation(btn: Button, zoomType: Int) {
        var animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, zoomType)
        btn.startAnimation(animation)
    }

    fun stopAnimation(img: ImageView) {
        img.clearAnimation()
    }

    fun stopAnimation(btn: Button) {
        btn.clearAnimation()
    }
}