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
import com.android.volley.toolbox.JsonObjectRequest
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
    private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    private val USER_CONNECT_ID = "USER_CONNECT_ID"
    var url: String =
        "https://voice.mozilla.org/api/v1/{{*{{lang}}*}}/" //API url -> replace {{*{{lang}}*}} with the selected_language

    val url_without_lang: String =
        "https://voice.mozilla.org/api/v1/" //API url (without lang)

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

        checkPermissions()

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selected_language = sharedPref2.getString(LANGUAGE_NAME, "en")
        this.url = this.url.replace("{{*{{lang}}*}}", this.selected_language)

        var skip_button: Button = this.findViewById(R.id.btn_skip_listen)
        skip_button.setOnClickListener {
            StopListening()
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
                        /*println(" >>>> id:"+this.id_sentence)
                        println(" >>>> text:"+this.text_sentence)
                        println(" >>>> sound:"+this.sound_sentence)
                        println(" >>>> glob:"+this.glob_sentence)*/
                        //this.text_sentence = json_result//just for testing
                        sentence.text = this.text_sentence
                        btnListen.isEnabled = true
                        msg.text = getString(R.string.txt_press_icon_below_listen_1)

                        this.mediaPlayer = MediaPlayer().apply {
                            //setAudioStreamType(AudioManager.STREAM_MUSIC) //to send the object to the initialized state
                            setDataSource(sound_sentence) //to set media source and send the object to the initialized state
                            prepare() //to send the object to the prepared state, this may take time for fetching and decoding
                        }
                        this.mediaPlayer?.setAuxEffectSendLevel(Float.MAX_VALUE)
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
                    val sharedPref: SharedPreferences =
                        getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
                    var logged = sharedPref.getBoolean(LOGGED_IN_NAME, false)
                    if (logged) {
                        val sharedPref3: SharedPreferences =
                            getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
                        var cookie_id = sharedPref3.getString(USER_CONNECT_ID, "")
                        headers.put(
                            "Cookie",
                            "connect.sid=" + cookie_id
                        )
                    } else {
                        headers.put(
                            "Authorization",
                            "Basic MzVmNmFmZTItZjY1OC00YTNhLThhZGMtNzQ0OGM2YTM0MjM3OjNhYzAwMWEyOTQyZTM4YzBiNmQwMWU0M2RjOTk0YjY3NjA0YWRmY2Q="
                        )
                    }
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
        msg.text = getString(R.string.txt_error_try_again_press_skip).replace(
            "{{*{{skip_button}}*}}",
            skip_text.text.toString()
        )
    }

    override fun onBackPressed() {
        StopListening()
        finish()
    }

    fun StartListening() {
        var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
        var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        btnNo.isVisible = true
        this.status = 1
        msg.text = getString(R.string.txt_press_icon_below_listen_2)
        btnListen.setBackgroundResource(R.drawable.stop_cv)

        this.mediaPlayer?.seekTo(0)
        this.mediaPlayer?.start()

        this.mediaPlayer!!.setOnCompletionListener {
            FinishListening()
        }
    }

    fun FinishListening() {
        if (this.mediaPlayer?.isPlaying == false) {
            //when clip is finished
            var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
            var btnListen: Button = this.findViewById(R.id.btn_start_listen)
            var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
            btnYes.isVisible = true
            msg.text = getString(R.string.txt_clip_correct_or_wrong)
            btnListen.setBackgroundResource(R.drawable.listen2_cv)
            this.status = 2
        }
    }

    fun StopListening() {
        var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
        var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        this.status = 2
        msg.text = getString(R.string.txt_clip_again)
        btnListen.setBackgroundResource(R.drawable.listen2_cv)
        this.mediaPlayer?.pause()
    }

    fun YesClip() {
        validateClip(true)
        Toast.makeText(this, getString(R.string.txt_clip_validated_yes), Toast.LENGTH_SHORT).show()
    }

    fun NoClip() {
        validateClip(false)
        Toast.makeText(this, getString(R.string.txt_clip_validated_no), Toast.LENGTH_SHORT).show()
    }

    fun validateClip(value: Boolean) {
        try {
            var path = "clips/{{*{{sentence_id}}*}}/votes" //API to get sentences
            path = path.replace("{{*{{sentence_id}}*}}",this.id_sentence.toString())
            //println(" -->> "+path.toString())
            var params = JSONObject()
            params.put("isValid", value)
            params.put("challenge", null)
            //println(" -->> "+params.toString())

            val que = Volley.newRequestQueue(this)
            val req = object : JsonObjectRequest(Request.Method.POST, url + path, params,
                Response.Listener {
                    val json_result = it.toString()
                    //println(" -->> Votes -->> "+json_result)
                    /*if (json_result.length > 2) {
                        val jsonObj = JSONObject(
                            json_result.substring(
                                json_result.indexOf("{"),
                                json_result.lastIndexOf("}") + 1
                            )
                        )
                    }*/

                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    error1()
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    //it permits to get the audio to validate (just if user doesn't do the log-in/sign-up)
                    val sharedPref: SharedPreferences =
                        getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
                    var logged = sharedPref.getBoolean(LOGGED_IN_NAME, false)
                    if (logged) {
                        val sharedPref3: SharedPreferences =
                            getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
                        var cookie_id = sharedPref3.getString(USER_CONNECT_ID, "")
                        headers.put(
                            "Cookie",
                            "connect.sid=" + cookie_id
                        )
                    } else {
                        headers.put(
                            "Authorization",
                            "Basic MzVmNmFmZTItZjY1OC00YTNhLThhZGMtNzQ0OGM2YTM0MjM3OjNhYzAwMWEyOTQyZTM4YzBiNmQwMWU0M2RjOTk0YjY3NjA0YWRmY2Q="
                        )
                    }
                    return headers
                }
            }
            que.add(req)
        } catch (e: Exception) {
            error1()
        }
        StopListening()
        API_request()
    }

    fun checkPermissions() {
        try {
            checkRecordVoicePermission()
        } catch (e: java.lang.Exception) {
            //println(" -->> Exception: " + e.toString())
        }
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                checkStoragePermission()
            }
        } catch (e: java.lang.Exception) {
            //println(" -->> Exception: " + e.toString())
        }
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

    fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
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
                    checkPermissions()
                }
            }
        }
    }
}