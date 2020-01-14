package org.commonvoice.saverio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    private val USER_CONNECT_ID = "USER_CONNECT_ID"
    private val USER_NAME = "USERNAME"
    var userId: String = ""
    var userName: String = ""

    val urlWithoutLang: String =
        "https://voice.mozilla.org/api/v1/" //API url (without lang)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //checkPermissions()
        checkConnection()

        var btnLoginSignUp: Button = findViewById(R.id.btnLogout)
        btnLoginSignUp.setOnClickListener {
            openWebBrowser("logout")
        }

        val sharedPref3: SharedPreferences = getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
        if (sharedPref3.getBoolean(LOGGED_IN_NAME, false) == false) {
            try {
                val intent = intent
                var url = intent.data
                //println("Url: "+ url.toString())
                if (url.toString().contains("https://auth.mozilla.auth0.com/passwordless/verify_redirect?")) {
                    openWebBrowser(url.toString())
                } else {
                    openWebBrowser("login")
                }
            } catch (e: Exception) {
                openWebBrowser("login")
            }
        } else {
            loadUserData("profile")
        }
    }

    override fun onBackPressed() {
        openMainAfterLogin()
    }

    fun openProfileSection() {
        setContentView(R.layout.activity_login)
        loadUserData("profile")
    }

    fun openWebBrowser(type: String) {
        //val email = findViewById<EditText>(R.id.txt_email_login).text

        if (type == "login" || (type != "login" && type != "logout" && type.contains("https://auth.mozilla.auth0.com/passwordless/verify_redirect?"))) {
            setContentView(R.layout.activity_webbrowser)

            var txtLoading: TextView = findViewById(R.id.txtLoadingWebBrowser)
            var bgLoading: ImageView = findViewById(R.id.imgBackgroundWebBrowser)
            var imgLoading: ImageView = findViewById(R.id.imgRobotWebBrowser)

            webView = findViewById(R.id.webViewBrowser)

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    // Loading started
                    txtLoading.isGone = false
                    bgLoading.isGone = false
                    imgLoading.isGone = false
                    startAnimation(imgLoading)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    // Loading finished
                    txtLoading.isGone = true
                    bgLoading.isGone = true
                    imgLoading.isGone = true
                    stopAnimation(imgLoading)

                    var cookies: String? = CookieManager.getInstance().getCookie(url)
                    //println(" ---->> "+url+" >> "+CookieManager.getInstance().getCookie(url)+" <<---- ")
                    if (url == "https://voice.mozilla.org/en" && cookies != null && cookies.contains(
                            "connect.sid="
                        )
                    ) {
                        loginSuccessful()
                        openProfileSection()
                        var arrayCookies = cookies.split("; ")
                        //println(" -->> ALL COOKIES -->> " + array_cookies.toString() + " <<--")
                        var myCookie: String = ""
                        if (arrayCookies[0].contains("connect.sid=")) myCookie =
                            arrayCookies[0].substring(12)
                        var i = 1
                        while (i < arrayCookies.count() || myCookie == "") {
                            if (arrayCookies[i].contains("connect.sid=")) myCookie =
                                arrayCookies[i].substring(12, arrayCookies[i].length - 1)
                            i++;
                        }
                        userId = myCookie
                        //println(" -->> MY COOKIE -->> "+my_cookie+" <<--")

                        val sharedPref: SharedPreferences =
                            getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
                        var editor = sharedPref.edit()
                        editor.putBoolean(LOGGED_IN_NAME, true)
                        editor.apply()

                        val sharedPref2: SharedPreferences =
                            getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
                        editor = sharedPref2.edit()
                        editor.putString(USER_CONNECT_ID, userId)
                        editor.apply()

                        //println(" -->> LOGGED IN <<-- ")

                        loadUserData("userName")
                    }
                }
            }

            //webView.loadUrl("https://accounts.firefox.com/signup?email=" + email)
            if (type == "login") {
                webView.loadUrl("https://voice.mozilla.org/login")
            } else {
                webView.loadUrl(type)
            }
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

    fun loginSuccessful() {
        //login successful -> show username and log-out button
        Toast.makeText(
            this,
            getString(R.string.txt_login_successful_alert),
            Toast.LENGTH_LONG
        ).show()
    }

    fun openMainAfterLogin() {
        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    fun doAnimation() {
        val img = findViewById<View>(R.id.imgRobotWebBrowser) as ImageView

        val aniSlide: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_in)
        img.startAnimation(aniSlide)

    }

    fun loadUserData(type: String) {
        if (type == "profile") {
            var profileImage: ImageView = findViewById(R.id.imageProfileImage)
            var profileEmail: EditText = findViewById(R.id.textProfileEmail)
            var profileUsername: EditText = findViewById(R.id.textProfileUsername)
            var profileAge: EditText = findViewById(R.id.textProfileAge)
            var profileGender: EditText = findViewById(R.id.textProfileGender)
            profileEmail.setText("...")
            profileUsername.setText("...")
            profileAge.setText("...")
            profileGender.setText("...")
        }
        try {
            val path = "user_client" //API to get sentences

            val que = Volley.newRequestQueue(this)
            val req = object : StringRequest(Request.Method.GET, urlWithoutLang + path,
                Response.Listener {
                    val jsonResult = it.toString()
                    if (jsonResult.length > 2) {
                        val jsonObj = JSONObject(
                            jsonResult.substring(
                                jsonResult.indexOf("{"),
                                jsonResult.lastIndexOf("}") + 1
                            )
                        )
                        if (type == "userName") {
                            userName = jsonObj.getString("username")
                        } else if (type == "profile") {
                            userName = jsonObj.getString("username")
                            var imageUrl = jsonObj.getString("avatar_url")

                            var profileImage: ImageView = findViewById(R.id.imageProfileImage)
                            var profileImageBorder: ImageView =
                                findViewById(R.id.imageProfileImageBorder)
                            //should set also the profileImage
                            var profileEmail: EditText = findViewById(R.id.textProfileEmail)
                            var profileUsername: EditText = findViewById(R.id.textProfileUsername)
                            val sharedPref1: SharedPreferences =
                                getSharedPreferences(USER_NAME, PRIVATE_MODE)
                            val editor = sharedPref1.edit()
                            editor.putString(USER_NAME, userName)
                            editor.apply()
                            var profileAge: EditText = findViewById(R.id.textProfileAge)
                            var profileGender: EditText = findViewById(R.id.textProfileGender)
                            profileEmail.setText(jsonObj.getString("email").toString())
                            profileUsername.setText(jsonObj.getString("username").toString())
                            profileAge.setText(getAgeString(jsonObj.getString("age").toString()))
                            profileGender.setText(getGenderString(jsonObj.getString("gender").toString()))
                            if (imageUrl != "null" && imageUrl != "") {
                                DownLoadImage(profileImage, profileImageBorder).execute(imageUrl)
                            } else {
                                DownLoadImage(profileImage, profileImageBorder).execute("null")
                            }
                        }

                        val sharedPref: SharedPreferences =
                            getSharedPreferences(USER_NAME, PRIVATE_MODE)
                        var editor = sharedPref.edit()
                        editor.putString(USER_NAME, userName)
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
                    if (userId == "") {
                        val sharedPref1: SharedPreferences =
                            getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
                        if (sharedPref1.getBoolean(LOGGED_IN_NAME, false)) {
                            val sharedPref2: SharedPreferences =
                                getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
                            userId = sharedPref2.getString(USER_CONNECT_ID, "")
                        }
                    }
                    val headers = HashMap<String, String>()
                    headers.put(
                        "Cookie",
                        "connect.sid=" + userId
                    )
                    return headers
                }
            }
            que.add(req)
        } catch (e: Exception) {
            println(" -->> Something wrong: " + e.toString() + " <<-- ")
            error2()
        }
    }

    fun error2() {
        //error while getting the username
        this.userName = ""
        //openMainAfterLogin()
        this.textProfileEmail.setText("?")
        this.textProfileUsername.setText("?")
        this.textProfileAge.setText("?")
        this.textProfileGender.setText("?")
    }

    fun getAgeString(age: String): String {
        var value: String = when (age) {
            "teens" -> "< 19"
            "twenties" -> "19-29"
            "thirties" -> "30-39"
            "fourties" -> "40-49"
            "fifties" -> "50-59"
            "sixties" -> "60-69"
            "seventies" -> "70-79"
            "eighties" -> "80-89"
            "nineties" -> "> 89"
            else -> "?"
        }
        return value
    }

    fun getGenderString(gender: String): String {
        var value: String = when (gender) {
            "male" -> getString(R.string.txt_gender_male)
            "female" -> getString(R.string.txt_gender_female)
            "other" -> getString(R.string.txt_gender_other)
            else -> "?"
        }
        return value
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions()
                }
            }
        }
    }

    fun checkConnection(): Boolean {
        if (LoginActivity.checkInternet(this)) {
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
            openMainAfterLogin()
        }
    }

    fun startAnimation(img: ImageView) {
        var animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.login)
        img.startAnimation(animation)
    }

    fun stopAnimation(img: ImageView) {
        img.clearAnimation()
    }
}