package org.commonvoice.saverio

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder.encode
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class SpeakActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    private val LANGUAGE_NAME = "LANGUAGE"
    private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    private val USER_CONNECT_ID = "USER_CONNECT_ID"
    private val FIRST_RUN_SPEAK = "FIRST_RUN_SPEAK"

    var url: String =
        "https://voice.mozilla.org/api/v1/{{*{{lang}}*}}/" //API url -> replace {{*{{lang}}*}} with the selected_language
    var tempUrl: String =
        "https://voice.allizom.org/api/v1/{{*{{lang}}*}}/" //TEST API url

    val urlWithoutLang: String =
        "https://voice.mozilla.org/api/v1/" //API url (without lang)

    var idSentence: String = ""
    var textSentence: String = ""
    var status: Int =
        0 //1->recording started | 2->recording finished | 3->recording listened | 4->recording sent | 5->listening stopped | 6->recording too long

    var selectedLanguage = ""
    var mediaRecorder: MediaRecorder? = null //audio recorder
    var output: String? = null //path of the recording
    var mediaPlayer: MediaPlayer? = null //audio player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        checkPermissions()
        checkConnection()

        var firstRun: Boolean = true
        val sharedPrefFirstRun: SharedPreferences =
            getSharedPreferences(FIRST_RUN_SPEAK, PRIVATE_MODE)
        firstRun = sharedPrefFirstRun.getBoolean(FIRST_RUN_SPEAK, true)
        if (firstRun) {
            val intent = Intent(this, FirstRunSpeak::class.java).also {
                startActivity(it)
                finish()
            }
        }

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selectedLanguage = sharedPref2.getString(LANGUAGE_NAME, "en")
        this.url = this.url.replace("{{*{{lang}}*}}", this.selectedLanguage)

        var skipButton: Button = this.findViewById(R.id.btn_skip_speak)
        skipButton.setOnClickListener {
            StopRecording()
            StopListening()
            API_request()
        }

        var startStopRecording: Button = this.findViewById(R.id.btn_start_speak)
        startStopRecording.setOnClickListener {
            if (this.status == 0 || this.status == 3)
                StartRecording() //0->record | 3->record again
            else if (this.status == 1)
                StopRecording()
            else if (this.status == 2)
                ListenRecording()
            else if (this.status == 5)
                StopListening()
        }

        var sendRecording: Button = this.findViewById(R.id.btn_send_speak)
        sendRecording.setOnClickListener {
            SendRecording()
        }

        var listenAgain: Button = this.findViewById(R.id.btn_listen_again)
        listenAgain.setOnClickListener {
            ListenRecording()
        }

        val sharedPref: SharedPreferences = getSharedPreferences(FIRST_RUN_SPEAK, PRIVATE_MODE)
        val defaultValue = false //actually it's "true", false is hust for testing
        if (sharedPref.getBoolean(FIRST_RUN_SPEAK, defaultValue)) {
            //First-run
            //setContentView(R.layout.first_run_speak)
        } else {
            //API request
            API_request()
        }
    }

    fun API_request() {
        DeleteRecording()
        checkConnection()

        var sentence: TextView = this.findViewById(R.id.textSpeakSentence)
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        var btnSkip: Button = this.findViewById(R.id.btn_skip_speak)
        this.idSentence = ""
        this.textSentence = ""
        btnRecord.isEnabled = false
        btnSend.isVisible = false
        btnListenAgain.isGone = true
        btnListenAgain.isVisible = false
        btnSkip.isEnabled = false
        this.status = 0
        msg.text = getText(R.string.txt_loading_sentence)
        sentence.text = "..."
        btnRecord.setBackgroundResource(R.drawable.speak_cv)

        try {
            val path = "sentences" //API to get sentences
            val params = JSONArray()
            //params.put("")

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
                        this.idSentence = jsonObj.getString("id")
                        this.textSentence = jsonObj.getString("text")
                        sentence.text = this.textSentence
                        btnRecord.isEnabled = true
                        msg.text = getString(R.string.txt_press_icon_below_speak_1)
                    } else {
                        error1()
                    }
                    btnSkip.isEnabled = true
                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    error1()
                    btnSkip.isEnabled = true
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
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
                            "Basic OWNlYzFlZTgtZGZmYS00YWU1LWI3M2MtYjg3NjM1OThjYmVjOjAxOTdmODU2NjhlMDQ3NTlhOWUxNzZkM2Q2MDdkOTEzNDE3ZGZkMjA="
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
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        var skipText: Button = this.findViewById(R.id.btn_skip_speak)
        msg.text = getString(R.string.txt_error_1_try_again_press_skip).replace(
            "{{*{{skip_button}}*}}",
            skipText.text.toString()
        )

        skipText.isEnabled = true
    }

    fun error2() {
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        msg.text = getString(R.string.txt_error_2_sending_failed)

        var skipText: Button = this.findViewById(R.id.btn_skip_speak)
        skipText.isEnabled = true
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        btnRecord.setBackgroundResource(R.drawable.speak2_cv)
        btnRecord.isEnabled = true
        this.status = 3
    }

    override fun onBackPressed() {
        var btnSkip: Button = this.findViewById(R.id.btn_skip_speak)
        var txtSentence: TextView = this.findViewById(R.id.textSpeakSentence)
        if (btnSkip.isEnabled || txtSentence.text == "...") {
            StopRecording()
            DeleteRecording()
            var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
            btnSkip.isEnabled = false
            msg.text = getString(R.string.txt_closing)
            finish()
        }
    }

    fun StartRecording() {
        //start or re-start recording
        checkPermissions()
        try {
            mediaRecorder = MediaRecorder()
            output = externalCacheDir?.absolutePath + "/" + this.idSentence + ".mp3"
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            if (Build.VERSION.SDK_INT < 26) {
                mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                //println(" -->> Versione API < 26")
            } else {
                mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                //println(" -->> Versione API >= 26")
            }
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            mediaRecorder?.setMaxDuration(10001)
            mediaRecorder?.setOutputFile(output)
            mediaRecorder?.setAudioEncodingBitRate(16 * 44100)
            mediaRecorder?.setAudioSamplingRate(44100)
            mediaRecorder?.prepare()
            mediaRecorder?.start()

            var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
            var btnSend: Button = this.findViewById(R.id.btn_send_speak)
            var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
            btnRecord.setBackgroundResource(R.drawable.stop_cv)
            var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
            btnSend.isVisible = false
            btnListenAgain.isGone = true
            btnListenAgain.isVisible = false
            msg.text = getString(R.string.txt_press_icon_below_speak_2)
            this.status = 1
        } catch (e: Exception) {
            //println(" -->> Something wrong: "+e.toString()+" <<-- ")
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    fun StopRecording() {
        //stop recording
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()

            val sampleUri2: String? = this.output // your uri here
            var metaRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
            metaRetriever.setDataSource(sampleUri2)
            var duration: String =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            //println(" >>>> " + duration.toLong())
            if (duration.toLong() > 10000) {
                RecordingTooLong()
            } else {
                var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
                var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
                btnRecord.setBackgroundResource(R.drawable.listen2_cv)
                msg.text = getString(R.string.txt_sentence_recorded)
                this.status = 2 //recording successful
            }
        } catch (e: Exception) {
            //println(" -->> Something wrong: "+e.toString()+" <<-- ")
            RecordingFailed()
        }
    }

    fun RecordingFailed() {
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btnRecord.setBackgroundResource(R.drawable.speak2_cv)
        if (this.status == 6) {
            msg.text = getString(R.string.txt_recording_too_long)
        } else {
            msg.text = getString(R.string.txt_sentence_record_failed)
        }
        this.status = 0 //recording failed
    }

    fun DeleteRecording() {
        val path = externalCacheDir?.absolutePath + "/" + this.idSentence + ".mp3"
        var file = File(path)
        if (this.idSentence != "" && file.exists()) {
            file.delete()
        }
    }

    fun RecordingTooLong() {
        this.status = 6 //too long
        RecordingFailed()
        DeleteRecording()
    }

    fun ListenRecording() {
        //listen recording
        val outputListening = this.output
        if (outputListening != null) {
            val sampleUri: Uri = outputListening.toUri() // your uri here
            mediaPlayer = MediaPlayer().apply {
                //setAudioStreamType(AudioManager.)
                setDataSource(
                    applicationContext,
                    sampleUri
                )
                prepare()
                seekTo(0)
                start()

                setOnCompletionListener {
                    FinishListening()
                }
            }
        }
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btnRecord.setBackgroundResource(R.drawable.stop_cv)
        btnListenAgain.isGone = true
        btnListenAgain.isVisible = false
        this.status = 5
        msg.text = getString(R.string.txt_listening_again_recording)
    }

    fun StopListening() {
        //stop listening
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btnRecord.setBackgroundResource(R.drawable.listen2_cv)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        btnListenAgain.setBackgroundResource(R.drawable.listen2_cv)
        var btnSendRecording: Button = this.findViewById(R.id.btn_send_speak)
        if (!btnSendRecording.isVisible) {
            this.status = 2 //re-listening recording -> because it's stopped
            msg.text = getString(R.string.txt_listening_stopped)
        } else {
            FinishListening()
        }
        this.mediaPlayer?.stop()
    }

    fun FinishListening() {
        //finish listening
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btnRecord.setBackgroundResource(R.drawable.speak2_cv)
        var btnListenAgain1: Button = this.findViewById(R.id.btn_listen_again)
        btnListenAgain1.setBackgroundResource(R.drawable.listen2_cv)
        this.status = 3 //listened the recording
        btnSend.isVisible = true
        btnListenAgain.isGone = false
        btnListenAgain.isVisible = true
        msg.text = getString(R.string.txt_recorded_correct_or_wrong)
    }

    fun SendRecording() {
        //sending recording
        StopListening()
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btnSkip: Button = this.findViewById(R.id.btn_skip_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btnRecord.setBackgroundResource(R.drawable.speak_cv)
        btnRecord.isEnabled = false
        this.status = 4
        btnSend.isVisible = false
        btnSkip.isEnabled = false
        btnListenAgain.isVisible = false
        msg.text = getString(R.string.txt_sending_recording)

        var encoded: ByteArray? = null
        var encoded2: String? = null
        if (Build.VERSION.SDK_INT < 26) {
            msg.text = "Error: your android version doesn't permit to send record to server. Sorry."
        } else {
            println("output: " + output)
            //encoded = Files.readAllBytes(Paths.get(this.output!!))
            encoded = Files.readAllBytes(Paths.get(this.output))

            val byteArray = output?.let { it.toByteArray() }

            println(" -->> byteArray -->>" + byteArray)
            println(" -->> encoded -->> " + encoded.toString())

            encoded2 = readFileAsLinesUsingBufferedReader(output!!)
            println(" -->> fileReadStream: " + encoded2.toString())

            /*
            File file = new File(path);
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            */
        }

        try {
            val path = "clips" //API to get sentences
            val params: String? = encoded2

            tempUrl = tempUrl.replace("{{*{{lang}}*}}", this.selectedLanguage)

            val que = Volley.newRequestQueue(this)
            val req = object : StringRequest(Request.Method.POST, tempUrl + path,
                Response.Listener {
                    val json_result = it.toString()
                    println(">> Successful: " + it.toString())
                    RecordingSent()
                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    error2()
                    println(">> Error: " + it.toString())
                    RecordingError()
                    btnSkip.isEnabled = true
                }
            ) {
                /*override fun getBodyContentType(): String {
                    return "application/octet-stream"//Use this function to set Content-Type for Volley
                }*/

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray? {
                    var returnValue = encoded2
                    println("--->>--->>: " + returnValue)
                    return returnValue?.toByteArray(Charset.defaultCharset())
                }


                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
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
                    headers.put("Content-Type", "application/octet-stream")
                    headers.put("sentence", encode(textSentence, "UTF-8").replace("+", "%20"))
                    headers.put("sentence_id", idSentence)
                    var formatted = ""
                    if (Build.VERSION.SDK_INT >= 26) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                        formatted = current.format(formatter)
                    } else {
                        formatted = SimpleDateFormat("yyyyMMddhhmmssSSS").format(Date()).toString()
                    }
                    headers.put("client_id", formatted+"CVAndroidUnofficialSav")
                    println(
                        " >> text_sentence >> " + encode(textSentence, "UTF-8").replace(
                            "+",
                            "%20"
                        )
                    )
                    println(" >> id_sentence >> " + idSentence)
                    return headers
                }
            }
            que.add(req)
        } catch (e: Exception) {
            error2()
            println(">> Exception: " + e.toString())
            btnSkip.isEnabled = true
        }
    }

    fun getFileBytes(context: Context, path: String): ByteArray {
        return File(path).readBytes()
    }

    fun RecordingSent() {
        //when recording is sent
        println("Recording sent!")
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        msg.text = getString(R.string.txt_recording_sent)
        Toast.makeText(this, getString(R.string.txt_recording_sent), Toast.LENGTH_SHORT).show()
        API_request()
    }

    fun RecordingError() {
        //when recording sending is failed
        println("Sending recording failed!")
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        msg.text = getString(R.string.txt_sending_recording_failed_and_skip).replace(
            "{{*{{skip_button}}*}}",
            getString(R.string.btn_skip_sentence)
        )
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btnSkip: Button = this.findViewById(R.id.btn_skip_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        btnRecord.isEnabled = true
        btnSend.isVisible = true
        btnListenAgain.isVisible = true
        btnSkip.isEnabled = true
        Toast.makeText(this, getString(R.string.txt_sending_recording_failed), Toast.LENGTH_SHORT)
            .show()
    }

    fun readFileAsLinesUsingBufferedReader(fileName: String): String =
        File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)

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
        if (SpeakActivity.checkInternet(this)) {
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