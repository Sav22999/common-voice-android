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
                    speakViewModel.getSentence()/*.observe(this, Observer {
                        speakViewModel.currentRecording = it
                        setupUIStateStandy()
                    })*/
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

        buttonReportSpeak.onClick {

        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.startListening()
        }

        buttonSendSpeak.onClick {
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

        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)

        buttonReportSpeak.visibility = View.GONE
        buttonRecordOrListenAgain.visibility = View.GONE
        buttonSkipSentence.visibility = View.GONE
        buttonSendSpeak.visibility = View.GONE
        buttonStartStopSpeak.visibility = View.GONE
    }

    private fun setupUIStateStandy() {
        buttonStartStopSpeak.visibility = View.VISIBLE
        buttonSkipSentence.visibility = View.VISIBLE
        buttonReportSpeak.visibility = View.VISIBLE

        //textViewSentence.text = speakViewModel.currentRecording.sentence
        textViewAlert.setText(R.string.txt_press_icon_below_speak_1)

        buttonStartStopSpeak.onClick {
            speakViewModel.startRecording()
        }
    }

    private fun loadUIStateRecording() {
        buttonStartStopSpeak.setBackgroundResource(R.drawable.stop_cv)

        textViewAlert.setText(R.string.txt_press_icon_below_speak_2)

        buttonStartStopSpeak.onClick {
            speakViewModel.stopRecording()
        }
    }

    private fun loadUIStateRecorded() {
        buttonRecordOrListenAgain.visibility = View.VISIBLE

        buttonStartStopSpeak.setBackgroundResource(R.drawable.listen_cv)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.speak2_cv)
        textViewAlert.setText(R.string.txt_press_icon_below_listen_1)

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
        textViewAlert.setText(R.string.txt_press_icon_below_listen_2)

        buttonStartStopSpeak.onClick {
            speakViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() {
        buttonSkipSentence.visibility = View.VISIBLE
        buttonSendSpeak.visibility = View.VISIBLE
        buttonRecordOrListenAgain.visibility = View.VISIBLE

        textViewAlert.setText(R.string.txt_recorded_correct_or_wrong)
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak2_cv)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.listen2_cv)

        buttonStartStopSpeak.onClick {
            speakViewModel.redoRecording()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.startListening()
        }
    }

    private fun obtainPermissions() {

    }

}
