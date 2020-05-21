package org.commonvoice.saverio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_speak.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class SpeakActivity : VariableLanguageActivity(R.layout.activity_speak) {

    private val speakViewModel: SpeakViewModel by stateViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 8573)
        }

        speakViewModel.state.observe(this, Observer {
            when(it) {
                SpeakViewModel.Companion.State.STANDBY -> {
                    loadUIStateLoading()
                    speakViewModel.getSentence().observe(this, Observer {
                        speakViewModel.currentRecording = it
                        setupUIStateStandy()
                    })
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
            }
        })
    }

    private fun setupInitialUIState() {
        buttonSkipSentence.onClick {
            speakViewModel.state.postValue(SpeakViewModel.Companion.State.STANDBY)
        }

        buttonReportSentence.onClick {

        }

        buttonDoItAgain.onClick {
            speakViewModel.startListening()
        }

        buttonSendRecording.onClick {
            speakViewModel.sendRecording().observe(this, Observer {
                if (it) {
                    Toast.makeText(this, "Registrazione inviata", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Errore", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun loadUIStateLoading() {
        textViewAlert.setText(R.string.txt_loading_sentence)
        textViewSentence.text = "..."

        buttonToggleRecording.setBackgroundResource(R.drawable.speak_cv)

        buttonReportSentence.visibility = View.GONE
        buttonDoItAgain.visibility = View.GONE
        buttonSkipSentence.visibility = View.GONE
        buttonSendRecording.visibility = View.GONE
        buttonToggleRecording.visibility = View.GONE
    }

    private fun setupUIStateStandy() {
        buttonToggleRecording.visibility = View.VISIBLE
        buttonSkipSentence.visibility = View.VISIBLE
        buttonReportSentence.visibility = View.VISIBLE

        textViewSentence.text = speakViewModel.currentRecording.sentence
        textViewAlert.setText(R.string.txt_press_icon_below_speak_1)

        buttonToggleRecording.onClick {
            speakViewModel.startRecording()
        }
    }

    private fun loadUIStateRecording() {
        buttonToggleRecording.setBackgroundResource(R.drawable.stop_cv)

        textViewAlert.setText(R.string.txt_press_icon_below_speak_2)

        buttonToggleRecording.onClick {
            speakViewModel.stopRecording()
        }
    }

    private fun loadUIStateRecorded() {
        buttonDoItAgain.visibility = View.VISIBLE

        buttonToggleRecording.setBackgroundResource(R.drawable.listen_cv)
        buttonDoItAgain.setBackgroundResource(R.drawable.speak2_cv)
        textViewAlert.setText(R.string.txt_press_icon_below_listen_1)

        buttonToggleRecording.onClick {
            speakViewModel.startListening()
        }

        buttonDoItAgain.onClick {
            speakViewModel.redoRecording()
        }
    }

    private fun loadUIStateListening() {
        buttonDoItAgain.visibility = View.GONE

        buttonToggleRecording.setBackgroundResource(R.drawable.stop_cv)
        textViewAlert.setText(R.string.txt_press_icon_below_listen_2)

        buttonToggleRecording.onClick {
            speakViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() {
        buttonSkipSentence.visibility = View.VISIBLE
        buttonSendRecording.visibility = View.VISIBLE
        buttonDoItAgain.visibility = View.VISIBLE

        textViewAlert.setText(R.string.txt_recorded_correct_or_wrong)
        buttonToggleRecording.setBackgroundResource(R.drawable.speak2_cv)
        buttonDoItAgain.setBackgroundResource(R.drawable.listen2_cv)

        buttonToggleRecording.onClick {
            speakViewModel.redoRecording()
        }

        buttonDoItAgain.onClick {
            speakViewModel.startListening()
        }
    }

    private fun obtainPermissions() {

    }

}
