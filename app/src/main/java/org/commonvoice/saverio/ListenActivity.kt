package org.commonvoice.saverio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import android.webkit.WebView
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
import org.json.JSONArray
import org.json.JSONObject


class ListenActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    private val LANGUAGE_NAME = "LANGUAGE"
    private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    private val USER_CONNECT_ID = "USER_CONNECT_ID"
    private val FIRST_RUN_LISTEN = "FIRST_RUN_LISTEN"

    var url: String =
        "https://voice.mozilla.org/api/v1/{{*{{lang}}*}}/" //API url -> replace {{*{{lang}}*}} with the selected_language

    val url_without_lang: String =
        "https://voice.mozilla.org/api/v1/" //API url (without lang)

    var idSentence: Int = 0
    var textSentence: String = ""
    var globSentence: String = ""
    var soundSentence: String = ""
    var status: Int = 0 //1->clip stopped | 2->clip re-starting

    var selectedLanguageVar = ""

    var mediaPlayer: MediaPlayer? = null //audioplayer to play/pause clips

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

        checkPermissions()
        checkConnection()

        var firstRun: Boolean = true
        val sharedPrefFirstRun: SharedPreferences =
            getSharedPreferences(FIRST_RUN_LISTEN, PRIVATE_MODE)
        firstRun = sharedPrefFirstRun.getBoolean(FIRST_RUN_LISTEN, true)
        if (firstRun) {
            val intent = Intent(this, FirstRunListen::class.java).also {
                startActivity(it)
                finish()
            }
        }

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selectedLanguageVar = sharedPref2.getString(LANGUAGE_NAME, "en")
        this.url = this.url.replace("{{*{{lang}}*}}", this.selectedLanguageVar)

        var skipButton: Button = this.findViewById(R.id.btn_skip_listen)
        skipButton.setOnClickListener {
            StopListening()
            API_request()
        }

        var startStopListening: Button = this.findViewById(R.id.btn_start_listen)
        startStopListening.setOnClickListener {
            if (this.status == 0 || this.status == 2) {
                StartListening() //0->play | 2->re-play
            } else if (this.status == 1)
                StopListening()
        }

        var yesClip: Button = this.findViewById(R.id.btn_yes_thumb)
        yesClip.setOnClickListener {
            YesClip()
        }

        var noClip: Button = this.findViewById(R.id.btn_no_thumb)
        noClip.setOnClickListener {
            NoClip()
        }

        //var sentence: TextView = this.findViewById(R.id.textSpeakSentence)

        //API request
        API_request()
    }

    fun API_request() {
        checkConnection()

        StopListening()
        var sentence: TextView = this.findViewById(R.id.textListenSentence)
        var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
        var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var btnSkip: Button = this.findViewById(R.id.btn_skip_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        this.idSentence = 0
        this.textSentence = ""
        this.globSentence = ""
        this.soundSentence = ""
        btnYes.isVisible = false
        btnNo.isVisible = false
        btnListen.isEnabled = false
        btnSkip.isEnabled = false
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
                    val jsonResult = it.toString()
                    if (jsonResult.length > 2) {
                        val jsonObj = JSONObject(
                            jsonResult.substring(
                                jsonResult.indexOf("{"),
                                jsonResult.lastIndexOf("}") + 1
                            )
                        )
                        this.idSentence = jsonObj.getString("id").toInt()
                        this.textSentence = jsonObj.getString("text")
                        this.soundSentence = jsonObj.getString("sound")
                        this.globSentence = jsonObj.getString("glob")
                        /*println(" >>>> id:"+this.id_sentence)
                        println(" >>>> text:"+this.text_sentence)
                        println(" >>>> sound:"+this.sound_sentence)
                        println(" >>>> glob:"+this.glob_sentence)*/
                        //this.text_sentence = json_result//just for testing
                        sentence.text = this.textSentence
                        btnListen.isEnabled = true
                        msg.text = getString(R.string.txt_press_icon_below_listen_1)

                        this.mediaPlayer = MediaPlayer().apply {
                            //setAudioStreamType(AudioManager.STREAM_MUSIC) //to send the object to the initialized state
                            setDataSource(soundSentence) //to set media source and send the object to the initialized state
                            prepare() //to send the object to the prepared state, this may take time for fetching and decoding
                        }
                        this.mediaPlayer?.setAuxEffectSendLevel(Float.MAX_VALUE)
                        btnSkip.isEnabled = true

                        btnYes.isVisible = false
                        btnNo.isVisible = false
                    } else {
                        error1()
                        btnSkip.isEnabled = true

                        btnYes.isVisible = false
                        btnNo.isVisible = false
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
                        var cookieId = sharedPref3.getString(USER_CONNECT_ID, "")
                        headers.put(
                            "Cookie",
                            "connect.sid=" + cookieId
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
            btnSkip.isEnabled = true
        }
    }

    fun error1() {
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        var skipText: Button = this.findViewById(R.id.btn_skip_listen)
        msg.text = getString(R.string.txt_error_1_try_again_press_skip).replace(
            "{{*{{skip_button}}*}}",
            skipText.text.toString()
        )
        skipText.isEnabled = true
    }

    override fun onBackPressed() {
        var btnSkip: Button = this.findViewById(R.id.btn_skip_listen)
        var txtSentence: TextView = this.findViewById(R.id.textListenSentence)
        if (btnSkip.isEnabled || txtSentence.text == "...") {
            StopListening()
            var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
            btnSkip.isEnabled = false
            msg.text = getString(R.string.txt_closing)
            finish()
        }
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
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        this.status = 2
        msg.text = getString(R.string.txt_clip_again)
        btnListen.setBackgroundResource(R.drawable.listen2_cv)
        this.mediaPlayer?.pause()
    }

    fun YesClip() {
        validateClip(true)
    }

    fun NoClip() {
        validateClip(false)
    }

    fun validateClip(value: Boolean) {
        try {
            StopListening()
            var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
            var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
            var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
            var btnListen: Button = this.findViewById(R.id.btn_start_listen)
            var btnSkip: Button = this.findViewById(R.id.btn_skip_listen)
            btnNo.isVisible = false
            btnYes.isVisible = false
            btnListen.isEnabled = false
            btnSkip.isEnabled = false
            msg.text = getString(R.string.txt_sending_validation)

            var path = "clips/{{*{{sentence_id}}*}}/votes" //API to get sentences
            path = path.replace("{{*{{sentence_id}}*}}", this.idSentence.toString())
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
                    ValidationSuccessful(value)
                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    //error1()
                    ValidationError(value)
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
                        var cookieId = sharedPref3.getString(USER_CONNECT_ID, "")
                        headers.put(
                            "Cookie",
                            "connect.sid=" + cookieId
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

    fun checkPermissions() {
        try {
            val PERMISSIONS = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    RECORD_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            //
        }
    }

    fun ValidationSuccessful(value: Boolean) {
        //Validation sent
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        if (value) {
            Toast.makeText(this, getString(R.string.txt_clip_validated_yes), Toast.LENGTH_SHORT)
                .show()
            msg.text = getString(R.string.txt_clip_validated_yes)
        } else {
            Toast.makeText(this, getString(R.string.txt_clip_validated_no), Toast.LENGTH_SHORT)
                .show()
            msg.text = getString(R.string.txt_clip_validated_no)
        }
        StopListening()
        API_request()
    }

    fun ValidationError(value: Boolean) {
        //Sending validation error
        error1()
        StopListening()
        var btnYes: Button = this.findViewById(R.id.btn_yes_thumb)
        var btnNo: Button = this.findViewById(R.id.btn_no_thumb)
        var btnListen: Button = this.findViewById(R.id.btn_start_listen)
        var msg: TextView = this.findViewById(R.id.textMessageAlertListen)
        var btnSkip: Button = this.findViewById(R.id.btn_skip_listen)
        btnNo.isVisible = true
        btnYes.isVisible = value
        btnListen.isEnabled = true
        btnSkip.isEnabled = true
        msg.text = getString(R.string.txt_error_3_sending_validation_failed).replace(
            "{{*{{skip_button}}*}}",
            getString(R.string.btn_skip_sentence)
        )
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

    fun checkConnection(): Boolean {
        if (ListenActivity.checkInternet(this)) {
            return true
        } else {
            openNoConnection()
            return false
        }
    }

    companion object {
        fun checkInternet(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                //Connection OK
                return true
            } else {
                //No connection
                return false
            }

        }
    }

    fun openNoConnection() {
        val intent = Intent(this, NoConnectionActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
}