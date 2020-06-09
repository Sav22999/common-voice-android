package org.commonvoice.saverio

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_speak.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.ui.dialogs.SpeakDialogFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.models.Sentence
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import kotlin.math.max
import kotlin.random.Random

class SpeakActivity : VariableLanguageActivity(R.layout.activity_speak) {

    private val speakViewModel: SpeakViewModel by stateViewModel()

    private val connectionManager: ConnectionManager by inject()

    private val statsPrefManager: StatsPrefManager by inject()

    private var numberSentThisSession: Int = 0

    private val permissionRequestCode by lazy {
        Random.nextInt(10000)
    }

    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        setupUI()

        connectionManager.liveInternetAvailability.observe(this, Observer { available ->
            if (!speakViewModel.showingHidingAirplaneIcon && (speakViewModel.airplaneModeIconVisible == available)) {
                speakViewModel.showingHidingAirplaneIcon = true
                if (!available) {
                    this.startAnimation(this.imageAirplaneModeSpeak, R.anim.zoom_in)
                    speakViewModel.airplaneModeIconVisible = true
                } else {
                    this.startAnimation(this.imageAirplaneModeSpeak, R.anim.zoom_out_speak_listen)
                    speakViewModel.airplaneModeIconVisible = false
                }
                speakViewModel.showingHidingAirplaneIcon = false
                this.imageAirplaneModeSpeak.isGone = available
            }
        })
    }

    override fun onBackPressed() {
        textMessageAlertSpeak.setText(R.string.txt_closing)
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)
        textSentenceSpeak.text = "..."
        textSentenceSpeak.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.title_very_big)
        )
        buttonRecordOrListenAgain.isGone = true
        buttonReportSpeak.isGone = true
        buttonSkipSpeak.isEnabled = false
        buttonStartStopSpeak.isEnabled = false
        buttonSendSpeak.isGone = true

        speakViewModel.stop(true)

        super.onBackPressed()
    }

    private fun setupUI() {
        setTheme(this)

        speakViewModel.currentSentence.observe(this, Observer { sentence ->
            setupUIStateStandby(sentence)
        })

        if (mainPrefManager.areGesturesEnabled) {
            setupGestures()
        }

        speakViewModel.state.observe(this, Observer {
            checkState(it)
        })

        statsPrefManager.dailyGoal.observe(this, Observer {
            if ((this.numberSentThisSession > 0) && it.checkDailyGoal()) {
                showMessageDialog(
                    "",
                    getString(R.string.daily_goal_achieved_message).replace(
                        "{{*{{n_clips}}*}}",
                        "${it.validations}"
                    ).replace(
                        "{{*{{n_sentences}}*}}",
                        "${it.recordings}"
                    )
                )
            }
        })
    }

    private fun checkState(status: SpeakViewModel.Companion.State?) {
        when (status) {
            SpeakViewModel.Companion.State.STANDBY -> {
                loadUIStateLoading()
                speakViewModel.loadNewSentence()
            }
            SpeakViewModel.Companion.State.RECORDING -> {
                loadUIStateRecording()
            }
            SpeakViewModel.Companion.State.RECORDED -> {
                loadUIStateRecorded()
            }
            SpeakViewModel.Companion.State.LISTENING -> {
                loadUIStateListening()
            }
            SpeakViewModel.Companion.State.LISTENED -> {
                loadUIStateListened()
            }
            SpeakViewModel.Companion.State.RECORDING_ERROR -> {
                showMessageDialog("", getString(R.string.messageDialogGenericError))
                speakViewModel.currentSentence.value?.let { sentence ->
                    setupUIStateStandby(sentence)
                }
            }
            SpeakViewModel.Companion.State.RECORDING_TOO_SHORT -> {
                showMessageDialog("", getString(R.string.txt_recording_too_short))
                speakViewModel.currentSentence.value?.let { sentence ->
                    setupUIStateStandby(sentence)
                }
            }
            SpeakViewModel.Companion.State.RECORDING_TOO_LONG -> {
                showMessageDialog("", getString(R.string.txt_recording_too_long))
                speakViewModel.currentSentence.value?.let { sentence ->
                    setupUIStateStandby(sentence)
                }
            }
        }
    }

    private fun showMessageDialog(title: String, text: String) {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        //val width = metrics.widthPixels
        val height = metrics.heightPixels
        MessageDialog(this, 0, title, text, details = "", height = height).show()
    }

    private fun setupGestures() {
        nestedScrollSpeak.setOnTouchListener(object : OnSwipeTouchListener(this@SpeakActivity) {
            override fun onSwipeLeft() {
                speakViewModel.skipSentence()
            }

            override fun onSwipeRight() {
                onBackPressed()
            }

            override fun onSwipeTop() {
                if (mainPrefManager.deviceOrientation == ORIENTATION_PORTRAIT) {
                    openReportDialog()
                }
            }
        })
    }

    fun setTheme(view: Context) {
        val theme: DarkLightTheme = DarkLightTheme()

        val isDark = theme.getTheme(view)
        theme.setElement(isDark, layoutSpeak)
        theme.setElement(isDark, view, 1, speakSectionBottom)
        theme.setElement(
            isDark,
            view,
            textMessageAlertSpeak,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
        theme.setElement(isDark, view, buttonReportSpeak, background = false)
        theme.setElement(isDark, view, buttonSkipSpeak)
    }

    private fun openReportDialog() {
        speakViewModel.stop()
        loadUIStateLoading()
        speakViewModel.loadNewSentence()

        SpeakDialogFragment().show(supportFragmentManager, "SPEAK_REPORT")
    }

    private fun setupInitialUIState() {
        buttonSkipSpeak.onClick {
            speakViewModel.skipSentence()
        }

        buttonReportSpeak.onClick {
            openReportDialog()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.startListening()
        }

        buttonSendSpeak.onClick {
            speakViewModel.sendRecording()
            this.numberSentThisSession++
        }
    }

    private fun loadUIStateLoading() {
        textMessageAlertSpeak.setText(R.string.txt_loading_sentence)
        // textSentenceSpeak.text = "..."

        buttonRecordOrListenAgain.isGone = true
        //buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)
        if (!speakViewModel.opened) {
            speakViewModel.opened = true
            startAnimation(buttonStartStopSpeak, R.anim.zoom_in_speak_listen)
            startAnimation(buttonSkipSpeak, R.anim.zoom_in_speak_listen)
        }
        buttonReportSpeak.isGone = true
        buttonSendSpeak.isGone = true
        buttonSkipSpeak.isEnabled = false
        buttonStartStopSpeak.isEnabled = false
    }

    private fun setupUIStateStandby(sentence: Sentence) {
        buttonSkipSpeak.isEnabled = true
        buttonStartStopSpeak.isEnabled = true

        buttonReportSpeak.isGone = false

        buttonRecordOrListenAgain.isGone = true
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)

        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_speak_1)
        textSentenceSpeak.text = sentence.sentenceText
        when (textSentenceSpeak.text.length) {
            in 0..20 -> {
                textSentenceSpeak.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.title_very_big)
                )
            }
            in 21..40 -> {
                textSentenceSpeak.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.title_big)
                )
            }
            in 41..50 -> {
                textSentenceSpeak.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.title_medium)
                )
            }
            in 51..80 -> {
                textSentenceSpeak.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.title_normal)
                )
            }
            else -> {
                textSentenceSpeak.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.title_small)
                )
            }
        }

        buttonStartStopSpeak.onClick {
            checkPermission()
            speakViewModel.startRecording()
            animateAudioBar()
        }
    }

    private fun loadUIStateRecording() {
        buttonRecordOrListenAgain.isGone = true
        buttonStartStopSpeak.setBackgroundResource(R.drawable.stop_cv)

        buttonSendSpeak.isGone = true
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_speak_2)
        speakViewModel.listened = false

        buttonStartStopSpeak.onClick {
            checkPermission()
            speakViewModel.stopRecording()
        }
    }

    private fun loadUIStateRecorded() {
        buttonRecordOrListenAgain.isGone = false
        startAnimation(buttonRecordOrListenAgain, R.anim.zoom_in_speak_listen)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.speak2_cv)

        buttonStartStopSpeak.setBackgroundResource(R.drawable.listen2_cv)
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_listen_1)

        buttonStartStopSpeak.onClick {
            speakViewModel.startListening()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.redoRecording()
            animateAudioBar()
        }
    }

    private fun loadUIStateListening() {
        buttonRecordOrListenAgain.isGone = true
        buttonStartStopSpeak.setBackgroundResource(R.drawable.stop_cv)
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_listen_2)

        buttonStartStopSpeak.onClick {
            speakViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() {
        buttonSendSpeak.isGone = false
        if (!speakViewModel.listened) startAnimation(buttonSendSpeak, R.anim.zoom_in_speak_listen)
        textMessageAlertSpeak.setText(R.string.txt_recorded_correct_or_wrong)
        buttonRecordOrListenAgain.isGone = false
        startAnimation(buttonRecordOrListenAgain, R.anim.zoom_in_speak_listen)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.listen2_cv)
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak2_cv)

        speakViewModel.listened = true

        buttonStartStopSpeak.onClick {
            speakViewModel.redoRecording()
            animateAudioBar()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.startListening()
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    onBackPressed()
                }
            }
        }
    }

    private fun animateAudioBar() {
        var count = 0
        while (count < speakSectionAudioBar.childCount) {
            val element = speakSectionAudioBar.getChildAt(count)
            animateAudioBar(element)
            count++
        }
    }

    private fun animateAudioBar(view: View) {
        if (speakViewModel.isRecordingNow()) {
            view.isVisible = true
            val animation: ValueAnimator =
                ValueAnimator.ofInt(view.height, (30..300).random())
            animation.duration = 400
            animation.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val value = animation.animatedValue as Int
                    view.layoutParams.height = value
                    view.requestLayout()
                }
            })
            animation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    animateAudioBar(view)
                }
            })
            animation.start()
        } else {
            view.isVisible = false
        }
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
