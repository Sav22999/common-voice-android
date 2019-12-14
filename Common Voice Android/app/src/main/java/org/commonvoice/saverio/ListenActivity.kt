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
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_listen.*
import kotlinx.android.synthetic.main.activity_speak.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class ListenActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    private val LANGUAGE_NAME = "LANGUAGE"
    var url: String =
        "https://voice.mozilla.org/api/v1/{{*{{lang}}*}}/" //API url -> replace {{*{{lang}}*}} with the selected_language

    var id_sentence: Int = 0
    var text_sentence: String = ""
    var glob_sentence: String = ""
    var sound_sentence: String = ""
    var status: Int = 0 //1->clip stopped | 2->clip re-starting

    var selected_language = ""

    var mediaPlayer: MediaPlayer? = null //audioplayer to play/pause clips

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

        checkRecordVoicePermission()

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selected_language = sharedPref2.getString(LANGUAGE_NAME, "en")
        this.url = this.url.replace("{{*{{lang}}*}}", this.selected_language)

        var skip_button: Button = this.findViewById(R.id.btn_skip_listen)
        skip_button.setOnClickListener {
            API_request()
        }

        var start_stop_listening: Button = this.findViewById(R.id.btn_start_listen)
        start_stop_listening.setOnClickListener {
            if (this.status == 0 || this.status == 2) {
                StartListening() //0->play | 2->re-play
            } else if (this.status == 1)
                StopListening()
        }

        var yes_clip: Button = this.findViewById(R.id.btn_yes_thumb)
        yes_clip.setOnClickListener {
            YesClip()
        }

        var no_clip: Button = this.findViewById(R.id.btn_no_thumb)
        no_clip.setOnClickListener {
            NoClip()
        }

        //var sentence: TextView = this.findViewById(R.id.textSpeakSentence)

        //API request
        API_request()
    }

    fun API_request() {
        var sentence: TextView = this.findViewById(R.id.textListenSentence)
        var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
        var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        this.id_sentence = 0
        this.text_sentence = ""
        this.glob_sentence = ""
        this.sound_sentence = ""
        btnYes.isVisible = false
        btnNo.isVisible = false
        btnListen.isEnabled = false
        this.status = 0
        msg.text = getText(R.string.txt_loading_clip)
        sentence.text = "..."
        try {
            val path = "clips" //API to get sentences
            val params = JSONArray()
            //params.put("")

            btnListen.setBackgroundResource(R.drawable.listen_cv)
            val que = Volley.newRequestQueue(this)
            val req = object : JsonArrayRequest(Request.Method.GET, url + path, params,
                Response.Listener {
                    val json_result = it.toString()
                    if (json_result.length > 2) {
                        val jsonObj = JSONObject(
                            json_result.substring(
                                json_result.indexOf("{"),
                                json_result.lastIndexOf("}") + 1
                            )
                        )
                        this.id_sentence = jsonObj.getString("id").toInt()
                        this.text_sentence = jsonObj.getString("text")
                        this.sound_sentence = jsonObj.getString("sound")
                        this.glob_sentence = jsonObj.getString("glob")
                        //this.text_sentence = json_result//just for testing
                        sentence.text = this.text_sentence
                        btnListen.isEnabled = true
                        msg.text = "Press the icon below to start the clip"

                        this.mediaPlayer = MediaPlayer().apply {
                            //setAudioStreamType(AudioManager.STREAM_MUSIC) //to send the object to the initialized state
                            setDataSource(sound_sentence) //to set media source and send the object to the initialized state
                            prepare() //to send the object to the prepared state, this may take time for fetching and decoding
                        }
                    } else {
                        error1()
                    }

                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    error1()
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    //it permits to get the audio to validate (just if user doesn't do the log-in/sign-up)
                    headers.put(
                        "Authorization",
                        "Basic MzVmNmFmZTItZjY1OC00YTNhLThhZGMtNzQ0OGM2YTM0MjM3OjNhYzAwMWEyOTQyZTM4YzBiNmQwMWU0M2RjOTk0YjY3NjA0YWRmY2Q="
                    )
                    return headers
                }
            }
            que.add(req)
        } catch (e: Exception) {
            error1()
        }
    }

    fun error1() {
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        var skip_text: Button = this.findViewById(R.id.btn_skip_listen)
        msg.text = "Error. Try again, so press ${skip_text.text} button"
    }

    fun StartListening() {
        var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
        var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        this.id_sentence = 0
        this.text_sentence = ""
        this.glob_sentence = ""
        this.sound_sentence = ""
        btnYes.isVisible = false
        btnNo.isVisible = true
        this.status = 1
        msg.text = "Press the icon below to stop the clip"
        btnListen.setBackgroundResource(R.drawable.stop_cv)

        this.mediaPlayer?.seekTo(0)
        this.mediaPlayer?.start()

        this.mediaPlayer!!.setOnCompletionListener {
            FinishListening()
        }
    }

    fun FinishListening()
    {
        if(this.mediaPlayer?.isPlaying==false) {
            //when clip is finished
            var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
            var btnListen: Button = this.findViewById(R.id.btn_start_listen)
            var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
            btnYes.isVisible = true
            msg.text =
                "The clip is correct? Press the Thumb up\nThe clip is wrong? Press the Thumb down"
            btnListen.setBackgroundResource(R.drawable.listen2_cv)
            this.status = 2
        }
    }

    fun StopListening() {
        var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
        var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        this.id_sentence = 0
        this.text_sentence = ""
        this.glob_sentence = ""
        this.sound_sentence = ""
        this.status = 2
        msg.text = "Press the icon below to start the clip again"
        btnListen.setBackgroundResource(R.drawable.listen2_cv)
        this.mediaPlayer?.pause()
    }

    fun YesClip() {
        Toast.makeText(this, "Clip validated \"Yes\"!", Toast.LENGTH_SHORT).show()
        StopListening()

        //when listening is validated
        API_request()
    }

    fun NoClip() {
        Toast.makeText(this, "Clip validated \"No\"!", Toast.LENGTH_SHORT).show()
        StopListening()

        //when listening is validated
        API_request()
    }

    fun checkRecordVoicePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET),
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