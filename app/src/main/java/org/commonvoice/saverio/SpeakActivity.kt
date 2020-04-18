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
import android.os.LocaleList
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
    private val TODAY_CONTRIBUTING =
        "TODAY_CONTRIBUTING" //saved as "yyyy/mm/dd, n_recorded, n_validated"

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

    var listened_first_time: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        var firstRun: Boolean =
            getSharedPreferences(FIRST_RUN_SPEAK, PRIVATE_MODE).getBoolean(FIRST_RUN_SPEAK, true)
        if (firstRun) {
            //First-run
            val intent = Intent(this, FirstRunSpeak::class.java).also {
                startActivity(it)
                finish()
            }
        } else {
            checkPermissions()
            checkConnection()

            this.selectedLanguage =
                getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE).getString(LANGUAGE_NAME, "en")
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
            //API request
            API_request()
        }
        setTheme(this)
    }

    fun setTheme(view: Context) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        theme.setElement(isDark, this.findViewById(R.id.layoutSpeak) as ConstraintLayout)
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.textMessageAlertSpeak) as TextView,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
        theme.setElement(isDark, view, this.findViewById(R.id.btn_skip_speak) as Button)
    }

    fun getDateToSave(savedDate: String): String {
        var todayDate: String = "?"
        if (Build.VERSION.SDK_INT < 26) {
            val dateTemp = SimpleDateFormat("yyyy/MM/dd")
            todayDate = dateTemp.format(Date()).toString()
        } else {
            val dateTemp = LocalDateTime.now()
            todayDate =
                dateTemp.year.toString() + "/" + dateTemp.monthValue.toString() + "/" + dateTemp.dayOfMonth.toString()
        }
        if (checkDateToday(todayDate, savedDate)) {
            return savedDate
        } else {
            return todayDate
        }
    }

    fun checkDateToday(todayDate: String, savedDate: String): Boolean {
        //true -> savedDate is OK, false -> savedDate is old
        if (todayDate == "?" || savedDate == "?") {
            return false
        } else if (todayDate == savedDate) {
            return true
        } else if (todayDate.split("/")[0] > savedDate.split("/")[0]) {
            return false
        } else if (todayDate.split("/")[1] > savedDate.split("/")[1]) {
            return false
        } else if (todayDate.split("/")[2] > savedDate.split("/")[2]) {
            return false
        } else {
            return true
        }
    }

    fun incrementContributing() {
        //just if the user is logged-in
        if (getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE).getBoolean(LOGGED_IN_NAME, false)) {
            //user logged
            var contributing = getSharedPreferences(TODAY_CONTRIBUTING, PRIVATE_MODE).getString(
                TODAY_CONTRIBUTING,
                "?, ?, ?"
            ).split(", ")
            var dateContributing = contributing[0]
            var dateContributingToSave = getDateToSave(dateContributing)
            var nRecorded: String = "?"
            if (dateContributingToSave == dateContributing) {
                //same date
                nRecorded = contributing[1]
                if (nRecorded == "?") {
                    nRecorded = "0"
                }
            } else {
                //new date
                nRecorded = "0"
            }
            nRecorded = (nRecorded.toInt() + 1).toString()
            var contributingToSave =
                dateContributingToSave + ", " + nRecorded + ", " + contributing[2]
            getSharedPreferences(TODAY_CONTRIBUTING, PRIVATE_MODE).edit()
                .putString(TODAY_CONTRIBUTING, contributingToSave).apply()
        } else {
            //user no logged
        }
    }

    fun showMessageDialog(
        title: String,
        text: String,
        errorCode: String = "",
        details: String = ""
    ) {
        try {
            var messageText = text
            if (errorCode != "") {
                if (messageText.contains("{{*{{error_code}}*}}")) {
                    messageText = messageText.replace("{{*{{error_code}}*}}", errorCode)
                } else {
                    messageText = messageText + "\n\n[Message Code: EX-" + errorCode + "]"
                }
            }
            val message: MessageDialog =
                MessageDialog(this, 0, title, messageText, details = details)
            message.show()
        } catch (exception: Exception) {
            println("!!-- Exception: SpeakActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
    }

    fun API_request() {
        DeleteRecording()
        checkConnection()
        this.listened_first_time = false

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
                        //println(" -->> Something wrong: "+it.toString()+" <<-- ")
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
                    var logged = getSharedPreferences(
                        LOGGED_IN_NAME,
                        PRIVATE_MODE
                    ).getBoolean(LOGGED_IN_NAME, false)
                    if (logged) {
                        var cookieId =
                            getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE).getString(
                                USER_CONNECT_ID,
                                ""
                            )
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
        msg.text = getString(R.string.txt_error_1_try_again_tap_skip).replace(
            "{{*{{skip_button}}*}}",
            skipText.text.toString()
        )
        skipText.isEnabled = true
        this.listened_first_time = false
        //EXS04
        showMessageDialog(
            "",
            getString(R.string.txt_error_1_try_again_tap_skip).replace(
                "{{*{{skip_button}}*}}",
                skipText.text.toString()
            ),
            errorCode = "S04"
        )
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
        this.listened_first_time = false

        //EXS03
        showMessageDialog(
            getString(R.string.messageDialogErrorTitle),
            getString(R.string.txt_error_2_sending_failed),
            errorCode = "S03"
        )
    }

    override fun onBackPressed() {
        closeSpeak()
    }

    fun closeSpeak() {
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
            this.listened_first_time = false
            mediaRecorder = MediaRecorder()
            output = externalCacheDir?.absolutePath + "/" + this.idSentence + ".aac"
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
            //EXS01
            showMessageDialog(
                getString(R.string.messageDialogErrorTitle),
                "General error in SpeakActivity\nPlease, contact the developer and attach this screen-error.",
                errorCode = "S01",
                details = e.toString()
            )
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    fun StopRecording() {
        //stop recording
        try {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (exception_temp: Exception) {
                //
            }

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
            //EXS02
            /*showMessageDialog(
                getString(R.string.messageDialogErrorTitle),
                "General error in SpeakActivity\nPlease, contact the developer and attach this screen-error.",
                errorCode = "S02",
                details = e.toString()
            )*/
            //RecordingFailed()
        }
    }

    fun RecordingFailed() {
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btnRecord.setBackgroundResource(R.drawable.speak2_cv)
        if (this.status == 6) {
            msg.text = getString(R.string.txt_recording_too_long)
            showMessageDialog(
                "",
                getString(R.string.txt_recording_too_long)
            )
        } else {
            msg.text = getString(R.string.txt_sentence_record_failed)
            showMessageDialog(
                "",
                getString(R.string.txt_sending_recording_failed)
            )
        }
        this.status = 0 //recording failed
    }

    fun DeleteRecording() {
        val path = externalCacheDir?.absolutePath + "/" + this.idSentence + ".aac"
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
        if (this.mediaPlayer?.isPlaying == true) {
            var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
            var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
            var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
            btnListenAgain.setBackgroundResource(R.drawable.listen2_cv)
            if (this.listened_first_time) {
                btnRecord.setBackgroundResource(R.drawable.speak2_cv)
                btnListenAgain.isVisible = true
            } else {
                btnRecord.setBackgroundResource(R.drawable.listen2_cv)
            }
            btnRecord.isEnabled = true
            btnListenAgain.isEnabled = true
            var btnSendRecording: Button = this.findViewById(R.id.btn_send_speak)
            if (!btnSendRecording.isVisible) {
                this.status = 2 //re-listening recording -> because it's stopped
                msg.text = getString(R.string.txt_listening_stopped)
            } else {
                FinishListening()
            }
            this.mediaPlayer?.stop()
        }
    }

    fun FinishListening() {
        //finish listening
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        if (this.mediaPlayer?.isPlaying == false) {
            this.listened_first_time = true
            btnRecord.setBackgroundResource(R.drawable.speak2_cv)
            btnListenAgain.setBackgroundResource(R.drawable.listen2_cv)
        }
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

        var encoded: ByteArray? = if (Build.VERSION.SDK_INT < 26) {
            msg.text =
                "Error: your android version doesn't permit to send the recording to server. Sorry."
            //EXS05
            showMessageDialog(
                getString(R.string.messageDialogErrorTitle),
                "Error: your android version doesn't permit to send the recording to server. Sorry.",
                errorCode = "S05"
            )
            null
        } else {
            println("output: " + output)
            //encoded = Files.readAllBytes(Paths.get(this.output!!))
            Files.readAllBytes(Paths.get(this.output))
        }

        try {
            val path = "clips" //API to get sentences

            tempUrl = tempUrl.replace("{{*{{lang}}*}}", this.selectedLanguage)

            val que = Volley.newRequestQueue(this)
            val req = object : StringRequest(Request.Method.POST, tempUrl + path,
                Response.Listener {
                    val json_result = it.toString()
                    println(">> Successful: " + it.toString())
                    RecordingSent()
                }, Response.ErrorListener {
                    println(" -->> Something wrong: " + it.toString() + " <<-- ")
                    error2()
                    println(">> Error: " + it.message.toString())
                    RecordingError()
                    btnSkip.isEnabled = true
                }
            ) {
                override fun getBodyContentType(): String {
                    return "audio/mpeg; codecs=aac"//Use this function to set Content-Type for Volley
                }

                override fun getBody(): ByteArray? = encoded

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    var logged = getSharedPreferences(
                        LOGGED_IN_NAME,
                        PRIVATE_MODE
                    ).getBoolean(LOGGED_IN_NAME, false)
                    if (logged) {
                        var cookie_id =
                            getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE).getString(
                                USER_CONNECT_ID,
                                ""
                            )
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
                    headers.put("client_id", formatted + "CVAndroidUnofficialSav")
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
        incrementContributing()
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
        //MX-S06
        showMessageDialog(
            "",
            getString(R.string.txt_sending_recording_failed_and_skip).replace(
                "{{*{{skip_button}}*}}",
                getString(R.string.btn_skip_sentence)
            ),
            errorCode = "S06"
        )
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
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    //checkPermissions()
                    closeSpeak()
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
            closeSpeak()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        var tempLang = newBase.getSharedPreferences("LANGUAGE", 0).getString("LANGUAGE", "en")
        var lang = tempLang.split("-")[0]
        val langSupportedYesOrNot = TranslationsLanguages()
        if (!langSupportedYesOrNot.isSupported(lang)) {
            lang = langSupportedYesOrNot.getDefaultLanguage()
        }
        super.attachBaseContext(newBase.wrap(Locale(lang)))
    }

    fun Context.wrap(desiredLocale: Locale): Context {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            return getUpdatedContextApi23(desiredLocale)

        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N)
            getUpdatedContextApi24(desiredLocale)
        else
            getUpdatedContextApi25(desiredLocale)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun Context.getUpdatedContextApi23(locale: Locale): Context {
        val configuration = resources.configuration
        configuration.locale = locale
        return createConfigurationContext(configuration)
    }

    private fun Context.getUpdatedContextApi24(locale: Locale): Context {
        val configuration = resources.configuration
        configuration.setLocale(locale)
        return createConfigurationContext(configuration)
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun Context.getUpdatedContextApi25(locale: Locale): Context {
        val localeList = LocaleList(locale)
        val configuration = resources.configuration
        configuration.locales = localeList
        return createConfigurationContext(configuration)
    }
}
