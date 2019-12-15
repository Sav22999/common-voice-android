package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.net.URL
import org.commonvoice.saverio.MainActivity as main


class SpeakActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    private val LANGUAGE_NAME = "LANGUAGE"
    var url: String =
        "https://voice.mozilla.org/api/v1/{{*{{lang}}*}}/" //API url -> replace {{*{{lang}}*}} with the selected_language

    val url_without_lang: String =
        "https://voice.mozilla.org/api/v1/" //API url (without lang)

    var id_sentence: String = ""
    var text_sentence: String = ""
    var status: Int =
        0 //1->recording started | 2->recording finished | 3->recording listened | 4->recording sent

    var selected_language = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        checkRecordVoicePermission()

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selected_language = sharedPref2.getString(LANGUAGE_NAME, "en")
        this.url = this.url.replace("{{*{{lang}}*}}", this.selected_language)


        var skip_button: Button = this.findViewById(R.id.btn_skip_speak)
        skip_button.setOnClickListener {
            API_request()
        }

        var start_stop_recording: Button = this.findViewById(R.id.btn_start_speak)
        start_stop_recording.setOnClickListener {
            if (this.status == 0 || this.status == 3)
                StartRecording() //0->record | 3->record again
            else if (this.status == 1)
                StopRecording()
            else if (this.status == 2)
                ListenRecording()
        }

        var send_recording: Button = this.findViewById(R.id.btn_send_speak)
        send_recording.setOnClickListener {
            SendRecording()
        }

        //var sentence: TextView = this.findViewById(R.id.textSpeakSentence)

        //API request
        API_request()
    }

    fun API_request() {
        var sentence: TextView = this.findViewById(R.id.textSpeakSentence)
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        this.id_sentence = ""
        this.text_sentence = ""
        btnRecord.isEnabled = false
        btnSend.isVisible = false
        this.status = 0
        msg.text = getText(R.string.txt_loading_sentence)
        sentence.text = "..."

        try {
            val path = "sentences" //API to get sentences
            val params = JSONArray()
            //params.put("")

            val que = Volley.newRequestQueue(this)
            val req = JsonArrayRequest(Request.Method.GET, url + path, params,
                Response.Listener {
                    val json_result = it.toString()
                    if (json_result.length > 2) {
                        val jsonObj = JSONObject(
                            json_result.substring(
                                json_result.indexOf("{"),
                                json_result.lastIndexOf("}") + 1
                            )
                        )
                        this.id_sentence = jsonObj.getString("id")
                        this.text_sentence = jsonObj.getString("text")
                        sentence.text = this.text_sentence
                        btnRecord.isEnabled = true
                        btnRecord.setBackgroundResource(R.drawable.speak_cv)
                        msg.text = getString(R.string.txt_press_icon_below_speak_1)
                    } else {
                        error1()
                    }
                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    error1()
                }
            )
            que.add(req)
        } catch (e: Exception) {
            error1()
        }
    }

    fun error1() {
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        var skip_text: Button = this.findViewById(R.id.btn_skip_speak)
        msg.text = getString(R.string.txt_error_try_again_press_skip).replace("{{*{{skip_button}}*}}",skip_text.text.toString())
    }

    fun StartRecording() {
        checkRecordVoicePermission()
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.stop_cv)
        btnSend.isVisible = false
        this.status = 1
        msg.text = getString(R.string.txt_press_icon_below_speak_2)
    }

    fun StopRecording() {
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.listen2_cv)
        this.status = 2 //recording successful
        //this.status = 0 //recording failed
        msg.text = getString(R.string.txt_sentence_recorded)
    }

    fun ListenRecording() {
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.record2_cv)
        this.status = 3 //listened the recording
        btnSend.isVisible = true
        msg.text = getString(R.string.txt_recorded_correct_or_wrong)
    }

    fun SendRecording() {
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.speak_cv)
        btn_record.isEnabled = false
        this.status = 4
        btnSend.isVisible = false
        msg.text = getString(R.string.txt_sending_recording)
        Toast.makeText(this, getString(R.string.txt_recording_sent), Toast.LENGTH_SHORT).show()

        //when recording is sent
        API_request()
    }

    fun checkRecordVoicePermission() {
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
                    checkRecordVoicePermission()
                }
            }
        }
    }
}