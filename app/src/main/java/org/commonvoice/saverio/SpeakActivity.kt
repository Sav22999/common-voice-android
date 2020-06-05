package org.commonvoice.saverio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_speak.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.models.Sentence
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import kotlin.random.Random

class SpeakActivity : VariableLanguageActivity(R.layout.activity_speak) {

    private val speakViewModel: SpeakViewModel by stateViewModel()

    private val connectionManager: ConnectionManager by inject()

    private val permissionRequestCode by lazy {
        Random.nextInt(10000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        if (obtainPermissions()) {
            setupUI()
        }

        connectionManager.liveInternetAvailability.observe(this, Observer { available ->
            this.imageAirplaneModeSpeak.isGone = available
        })
    }

    override fun onBackPressed() {
        textMessageAlertSpeak.setText(R.string.txt_closing)

        speakViewModel.stop()

        super.onBackPressed()
    }

    private fun setupUI() {
        speakViewModel.currentSentence.observe(this, Observer { sentence ->
            setupUIStateStandby(sentence)
        })

        if (mainPrefManager.areGesturesEnabled) {
            setupGestures()
        }

        speakViewModel.state.observe(this, Observer {
            when (it) {
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
                    showMessageDialog("", getString(R.string.txt_recording_too_short))
                    speakViewModel.currentSentence.value?.let { sentence ->
                        setupUIStateStandby(sentence)
                    }
                }
            }
        })

        speakViewModel.hasReachedGoal.observe(this, Observer {
            if (it) {
                showMessageDialog(
                    "",
                    getString(R.string.daily_goal_achieved_message).replace(
                        "{{*{{n_clips}}*}}",
                        "${speakViewModel.getDailyGoal().validations}"
                    ).replace(
                        "{{*{{n_sentences}}*}}",
                        "${speakViewModel.getDailyGoal().recordings}"
                    )
                )
            }
        })

        setTheme(this)
    }

    private fun showMessageDialog(title: String, text: String) {
        MessageDialog(this, 0, title, text, details = "").show()
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
        }
    }

    private fun loadUIStateLoading() {
        textMessageAlertSpeak.setText(R.string.txt_loading_sentence)
        textSentenceSpeak.text = "..."

        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)

        buttonReportSpeak.visibility = View.GONE
        buttonRecordOrListenAgain.visibility = View.GONE
        buttonSkipSpeak.visibility = View.GONE
        buttonSendSpeak.visibility = View.GONE
        buttonStartStopSpeak.visibility = View.GONE
    }

    private fun setupUIStateStandby(sentence: Sentence) {
        buttonStartStopSpeak.visibility = View.VISIBLE
        buttonSkipSpeak.visibility = View.VISIBLE
        buttonReportSpeak.visibility = View.VISIBLE

        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)

        textSentenceSpeak.text = sentence.sentenceText
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_speak_1)

        buttonStartStopSpeak.onClick {
            speakViewModel.startRecording()
        }
    }

    private fun loadUIStateRecording() {
        buttonStartStopSpeak.setBackgroundResource(R.drawable.stop_cv)

        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_speak_2)

        buttonStartStopSpeak.onClick {
            speakViewModel.stopRecording()
        }
    }

    private fun loadUIStateRecorded() {
        buttonRecordOrListenAgain.visibility = View.VISIBLE

        buttonStartStopSpeak.setBackgroundResource(R.drawable.listen2_cv)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.speak2_cv)
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_listen_1)

        buttonStartStopSpeak.onClick {
            speakViewModel.startListening()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.redoRecording()
        }
    }

    private fun loadUIStateListening() {
        buttonRecordOrListenAgain.visibility = View.GONE

        buttonStartStopSpeak.setBackgroundResource(R.drawable.stop_cv)
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_listen_2)

        buttonStartStopSpeak.onClick {
            speakViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() {
        buttonSkipSpeak.visibility = View.VISIBLE
        buttonSendSpeak.visibility = View.VISIBLE
        buttonRecordOrListenAgain.visibility = View.VISIBLE

        textMessageAlertSpeak.setText(R.string.txt_recorded_correct_or_wrong)
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak2_cv)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.listen2_cv)

        buttonStartStopSpeak.onClick {
            speakViewModel.redoRecording()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.startListening()
        }
    }

    private fun obtainPermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequestCode
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionRequestCode) {
            if (grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                setupUI()
            }
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
