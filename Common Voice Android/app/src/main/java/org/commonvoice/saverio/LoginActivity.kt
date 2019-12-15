package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import org.commonvoice.saverio.MainActivity as MainActivity


class LoginActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    private val USER_CONNECT_ID = "USER_CONNECT_ID"
    private val USER_NAME = "USERNAME"
    var user_id: String = ""
    var user_name: String = ""

    val url_without_lang: String =
        "https://voice.mozilla.org/api/v1/" //API url (without lang)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkRecordVoicePermission()

        /*var btnLoginSignUp: Button = findViewById(R.id.btn_login_signup)
        btnLoginSignUp.setOnClickListener {
            openWebBrowser()
        }
        var txtEmail: EditText = findViewById(R.id.txt_email_login)
        txtEmail.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                openWebBrowser()
                return@OnKeyListener true
            }
            false
        })*/

        val sharedPref3: SharedPreferences = getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
        if (sharedPref3.getBoolean(LOGGED_IN_NAME, false) == false) {
            openWebBrowser("login")
        } else {
            openWebBrowser("logout")
        }
    }

    override fun onBackPressed() {
        openMainAfterLogin()
    }

    fun openWebBrowser(type: String) {
        //val email = findViewById<EditText>(R.id.txt_email_login).text

        if (type == "login") {
            //if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || true) {
            setContentView(R.layout.fragment_webbrowser)

            webView = findViewById(R.id.webViewBrowser)

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    // Loading started
                    var txtLoading: TextView = findViewById(R.id.textLoadingPage)
                    txtLoading.isGone = false
                    txtLoading.isVisible = true
                    txtLoading.text = getString(R.string.txt_loading_page)
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Loading finished
                    var txtLoading: TextView = findViewById(R.id.textLoadingPage)
                    txtLoading.isGone = true
                    txtLoading.isVisible = false

                    var cookies: String? = CookieManager.getInstance().getCookie(url)
                    //println(" ---->> "+url+" >> "+CookieManager.getInstance().getCookie(url)+" <<---- ")
                    if (url == "https://voice.mozilla.org/en" && cookies != null && cookies.contains(
                            "connect.sid="
                        )
                    ) {
                        var array_cookies = cookies.split("; ")
                        //println(" -->> ALL COOKIES -->> " + array_cookies.toString() + " <<--")
                        var my_cookie: String = ""
                        if (array_cookies[0].contains("connect.sid=")) my_cookie =
                            array_cookies[0].substring(12)
                        var i = 1
                        while (i < array_cookies.count() || my_cookie == "") {
                            if (array_cookies[i].contains("connect.sid=")) my_cookie =
                                array_cookies[i].substring(12, array_cookies[i].length - 1)
                            i++;
                        }
                        user_id = my_cookie
                        //println(" -->> MY COOKIE -->> "+my_cookie+" <<--")

                        val sharedPref: SharedPreferences =
                            getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
                        var editor = sharedPref.edit()
                        editor.putBoolean(LOGGED_IN_NAME, true)
                        editor.apply()

                        val sharedPref2: SharedPreferences =
                            getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
                        editor = sharedPref2.edit()
                        editor.putString(USER_CONNECT_ID, user_id)
                        editor.apply()

                        //println(" -->> LOGGED IN <<-- ")

                        getUsernameFromAPI()
                    }
                }
            }

            //webView.loadUrl("https://accounts.firefox.com/signup?email=" + email)
            webView.loadUrl("https://voice.mozilla.org/login")
            //}
        } else if (type == "logout") {
            val sharedPref: SharedPreferences =
                getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
            var editor = sharedPref.edit()
            editor.putBoolean(LOGGED_IN_NAME, false)
            editor.apply()

            val sharedPref2: SharedPreferences =
                getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
            editor = sharedPref2.edit()
            editor.putString(USER_CONNECT_ID, "")
            editor.apply()

            val sharedPref3: SharedPreferences =
                getSharedPreferences(USER_NAME, PRIVATE_MODE)
            editor = sharedPref3.edit()
            editor.putString(USER_NAME, "")
            editor.apply()

            openMainAfterLogin()
        }
    }

    fun openMainAfterLogin() {
        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    fun getUsernameFromAPI() {
        try {
            val path = "user_client" //API to get sentences

            val que = Volley.newRequestQueue(this)
            val req = object : StringRequest(Request.Method.GET, url_without_lang + path,
                Response.Listener {
                    val json_result = it.toString()
                    if (json_result.length > 2) {
                        val jsonObj = JSONObject(
                            json_result.substring(
                                json_result.indexOf("{"),
                                json_result.lastIndexOf("}") + 1
                            )
                        )
                        user_name = jsonObj.getString("username")

                        val sharedPref: SharedPreferences =
                            getSharedPreferences(USER_NAME, PRIVATE_MODE)
                        var editor = sharedPref.edit()
                        editor.putString(USER_NAME, user_name)
                        editor.apply()
                    } else {
                        error2()
                    }
                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    error2()
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    //it permits to get the audio to validate (just if user doesn't do the log-in/sign-up)
                    headers.put(
                        "Cookie",
                        "connect.sid=" + user_id
                    )
                    return headers
                }
            }
            que.add(req)
        } catch (e: Exception) {
            error2()
        }

        openMainAfterLogin()
    }

    fun error2() {
        //error while getting the username
        //this.user_name = ""
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