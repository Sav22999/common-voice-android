package org.commonvoice.saverio

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_speak.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.milliseconds
import kotlin.time.seconds
import org.commonvoice.saverio.MainActivity as main


class SpeakActivity : AppCompatActivity() {

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

    var id_sentence: String = ""
    var text_sentence: String = ""
    var status: Int =
        0 //1->recording started | 2->recording finished | 3->recording listened | 4->recording sent | 5->listening stopped | 6->recording too long

    var selected_language = ""
    var mediaRecorder: MediaRecorder? = null //audiorecorder
    var output: String? = null //path of the recording
    var mediaPlayer: MediaPlayer? = null //audioplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        checkPermissions()

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selected_language = sharedPref2.getString(LANGUAGE_NAME, "en")
        this.url = this.url.replace("{{*{{lang}}*}}", this.selected_language)


        var skip_button: Button = this.findViewById(R.id.btn_skip_speak)
        skip_button.setOnClickListener {
            StopRecording()
            StopListening()
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
            else if (this.status == 5)
                StopListening()
        }

        var send_recording: Button = this.findViewById(R.id.btn_send_speak)
        send_recording.setOnClickListener {
            SendRecording()
        }

        var listen_again: Button = this.findViewById(R.id.btn_listen_again)
        listen_again.setOnClickListener {
            ListenRecording()
        }

        //API request
        API_request()
    }

    fun API_request() {
        DeleteRecording()

        var sentence: TextView = this.findViewById(R.id.textSpeakSentence)
        var btnRecord: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        this.id_sentence = ""
        this.text_sentence = ""
        btnRecord.isEnabled = false
        btnSend.isVisible = false
        btnListenAgain.isGone = true
        btnListenAgain.isVisible = false
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
                        msg.text = getString(R.string.txt_press_icon_below_speak_1)
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
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        var skip_text: Button = this.findViewById(R.id.btn_skip_speak)
        msg.text = getString(R.string.txt_error_try_again_press_skip).replace(
            "{{*{{skip_button}}*}}",
            skip_text.text.toString()
        )
    }

    override fun onBackPressed() {
        StopRecording()
        DeleteRecording()
        finish()
    }

    fun StartRecording() {
        //start or re-start recording
        checkPermissions()
        try {
            mediaRecorder = MediaRecorder()
            output = externalCacheDir?.absolutePath + "/" + this.id_sentence + ".mp3"
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
            var btn_record: Button = this.findViewById(R.id.btn_start_speak)
            btn_record.setBackgroundResource(R.drawable.stop_cv)
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
                var btn_record: Button = this.findViewById(R.id.btn_start_speak)
                var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
                btn_record.setBackgroundResource(R.drawable.listen2_cv)
                msg.text = getString(R.string.txt_sentence_recorded)
                this.status = 2 //recording successful
            }
        } catch (e: Exception) {
            //println(" -->> Something wrong: "+e.toString()+" <<-- ")
            RecordingFailed()
        }
    }

    fun RecordingFailed() {
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.speak_cv)
        if (this.status == 6) {
            msg.text = getString(R.string.txt_recording_too_long)
        } else {
            msg.text = getString(R.string.txt_sentence_record_failed)
        }
        this.status = 0 //recording failed
    }

    fun DeleteRecording() {
        val path = externalCacheDir?.absolutePath + "/" + this.id_sentence + ".mp3"
        var file = File(path)
        if (this.id_sentence != "" && file.exists()) {
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
        val output_listening = this.output
        if (output_listening != null) {
            val sampleUri: Uri = output_listening.toUri() // your uri here
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
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.listen2_cv)
        var btn_listen_again: Button = this.findViewById(R.id.btn_listen_again)
        btn_listen_again.setBackgroundResource(R.drawable.listen2_cv)
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
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var btnListenAgain: Button = this.findViewById(R.id.btn_listen_again)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.record2_cv)
        var btn_listen_again: Button = this.findViewById(R.id.btn_listen_again)
        btn_listen_again.setBackgroundResource(R.drawable.listen2_cv)
        this.status = 3 //listened the recording
        btnSend.isVisible = true
        btnListenAgain.isGone = false
        btnListenAgain.isVisible = true
        msg.text = getString(R.string.txt_recorded_correct_or_wrong)
    }

    fun SendRecording() {
        //send recording
        StopListening()
        var btn_record: Button = this.findViewById(R.id.btn_start_speak)
        var btnSend: Button = this.findViewById(R.id.btn_send_speak)
        var msg: TextView = this.findViewById(R.id.textMessageAlertSpeak)
        btn_record.setBackgroundResource(R.drawable.speak_cv)
        btn_record.isEnabled = false
        this.status = 4
        btnSend.isVisible = false
        msg.text = getString(R.string.txt_sending_recording)
        Toast.makeText(this, getString(R.string.txt_recording_sent), Toast.LENGTH_SHORT).show()


        /*
        if (Build.VERSION.SDK_INT<26) {
            msg.text = "Error: your android version doens't permit to send record to server. Sorry."
        } else {
            val encoded = Files.readAllBytes(Paths.get(this.output!!))
            println(" -->> readAllBytes -->> "+encoded.toString())

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
        */

        //when recording is sent
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