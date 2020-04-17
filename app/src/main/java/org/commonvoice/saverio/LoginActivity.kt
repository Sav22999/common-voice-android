package org.commonvoice.saverio

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import java.util.*
import kotlin.collections.HashMap


class LoginActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    private val USER_CONNECT_ID = "USER_CONNECT_ID"
    private val USER_NAME = "USERNAME"
    private val LEVEL_SAVED = "LEVEL_SAVED"
    private val RECORDINGS_SAVED = "RECORDINGS_SAVED"
    private val VALIDATIONS_SAVED = "VALIDATIONS_SAVED"
    private val TODAY_CONTRIBUTING =
        "TODAY_CONTRIBUTING" //saved as "yyyy/mm/dd, n_recorded, n_validated"
    var userId: String = ""
    var userName: String = ""

    val urlWithoutLang: String =
        "https://voice.mozilla.org/api/v1/" //API url (without lang)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //checkPermissions()
        checkConnection()

        var btnLoginSignUp: Button = this.findViewById(R.id.btnLogout)
        btnLoginSignUp.setOnClickListener {
            logoutAndExit()
        }

        var btnOpenBadge: Button = this.findViewById(R.id.btnBadges)
        btnOpenBadge.setOnClickListener {
            var intent = Intent(this, BadgesActivity::class.java).also {
                startActivity(it)
            }
        }

        var txtLevel: TextView = this.findViewById(R.id.textLevel)
        var txtChangeSettingsOnWebsite: TextView = this.findViewById(R.id.labelToModifyInformation)
        txtChangeSettingsOnWebsite.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txtChangeSettingsOnWebsite.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://voice.mozilla.org/profile/info")
                )
            )
        }

        val unit = try {
            actionBar?.setTitle(getString(R.string.button_home_profile))
        } catch (exception: Exception) {
            println("!! Exception: (LoginActivity) I can't set Title in ActionBar (method1) -- " + exception.toString() + " !!")
        }
        try {
            supportActionBar?.setTitle(getString(R.string.button_home_profile))
        } catch (exception: Exception) {
            println("!! Exception: (LoginActivity) I can't set Title in ActionBar (method2) -- " + exception.toString() + " !!")
        }

        if (getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE).getBoolean(
                LOGGED_IN_NAME,
                false
            ) == false
        ) {
            try {
                val intent = intent
                var url = intent.data
                //println("Url: "+ url.toString())
                if (url.toString()
                        .contains("https://auth.mozilla.auth0.com/passwordless/verify_redirect?")
                ) {
                    openWebBrowser(url.toString())
                } else {
                    openWebBrowser("login")
                }
            } catch (e: Exception) {
                openWebBrowser("login")
            }
        } else {
            loadUserData("profile")
            setTheme(this)
        }
    }

    override fun onBackPressed() {
        openMainAfterLogin()
    }

    fun getSavedLevel(): Int {
        var value = getSharedPreferences(LEVEL_SAVED, PRIVATE_MODE).getInt(LEVEL_SAVED, 0)
        println("level: " + value)
        return when (value) {
            in 0..20 -> 1
            in 5..49 -> 2
            in 50..99 -> 3
            in 100..499 -> 4
            in 500..999 -> 5
            in 1000..4999 -> 6
            in 5000..9999 -> 7
            in 10000..49999 -> 8
            in 50000..99999 -> 9
            in 100000..100000000 -> 10
            else -> 1
        }
    }

    fun setLevel(txtLevel: TextView) {
        txtLevel.isGone = false
        var nLevel = getSavedLevel()
        var nameLevel: String = ""
        when (nLevel) {
            1 -> {
                nameLevel = getString(R.string.txt_level1_name)
                txtLevel.setBackgroundResource(R.color.colorLevel1)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
            2 -> {
                nameLevel = getString(R.string.txt_level2_name)
                txtLevel.setBackgroundResource(R.color.colorLevel2)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
            3 -> {
                nameLevel = getString(R.string.txt_level3_name)
                txtLevel.setBackgroundResource(R.color.colorLevel3)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
            4 -> {
                nameLevel = getString(R.string.txt_level4_name)
                txtLevel.setBackgroundResource(R.color.colorLevel4)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
            5 -> {
                nameLevel = getString(R.string.txt_level5_name)
                txtLevel.setBackgroundResource(R.color.colorLevel5)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
            6 -> {
                nameLevel = getString(R.string.txt_level6_name)
                txtLevel.setBackgroundResource(R.color.colorLevel6)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
            7 -> {
                nameLevel = getString(R.string.txt_level7_name)
                txtLevel.setBackgroundResource(R.color.colorLevel7)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
            8 -> {
                nameLevel = getString(R.string.txt_level8_name)
                txtLevel.setBackgroundResource(R.color.colorLevel8)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
            9 -> {
                nameLevel = getString(R.string.txt_level9_name)
                txtLevel.setBackgroundResource(R.color.colorLevel9)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
            10 -> {
                nameLevel = getString(R.string.txt_level10_name)
                txtLevel.setBackgroundResource(R.color.colorLevel10)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
            else -> {
                nameLevel = getString(R.string.txt_level1_name)
                txtLevel.setBackgroundResource(R.color.colorLevel1)
                txtLevel.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
        }
        txtLevel.setText(
            getString(R.string.txt_your_level).replace(
                "{{*{{level}}*}}",
                nLevel.toString()
            ) + "\n\"" + nameLevel + "\""
        )
    }

    fun setTheme(view: Context) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        theme.setElement(isDark, this.findViewById(R.id.layoutLogin) as ConstraintLayout)
        theme.setElement(isDark, view, this.findViewById(R.id.btnBadges) as Button)
        theme.setElement(isDark, view, this.findViewById(R.id.btnLogout) as Button)
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.labelToModifyInformation) as TextView,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
        theme.setTextView(isDark, view, this.findViewById(R.id.textProfileUsername) as TextView)
        theme.setTextView(isDark, view, this.findViewById(R.id.textProfileEmail) as TextView)
        theme.setTextView(isDark, view, this.findViewById(R.id.textProfileAge) as TextView)
        theme.setTextView(isDark, view, this.findViewById(R.id.textProfileGender) as TextView)
        theme.setElement(isDark, view, this.findViewById(R.id.labelProfileUsername) as TextView)
        theme.setElement(isDark, view, this.findViewById(R.id.labelProfileEmail) as TextView)
        theme.setElement(isDark, view, this.findViewById(R.id.labelProfileAge) as TextView)
        theme.setElement(isDark, view, this.findViewById(R.id.labelProfileGender) as TextView)
        theme.setElement(
            isDark,
            this.findViewById(R.id.imageProfileImageBorder) as ImageView,
            R.drawable.background_profile_image,
            R.drawable.background_profile_image_darktheme
        )
    }

    fun openProfileSection() {
        //setContentView(R.layout.activity_login)
        //loadUserData("profile")
        reopenLogin()
    }

    fun showLoading() {
        var txtLoading: TextView = findViewById(R.id.txtLoadingWebBrowser)
        var bgLoading: ImageView = findViewById(R.id.imgBackgroundWebBrowser)
        var imgLoading: ImageView = findViewById(R.id.imgRobotWebBrowser)
        txtLoading.isGone = false
        bgLoading.isGone = false
        imgLoading.isGone = false
        startAnimation(imgLoading)
    }

    fun hideLoading() {
        var txtLoading: TextView = findViewById(R.id.txtLoadingWebBrowser)
        var bgLoading: ImageView = findViewById(R.id.imgBackgroundWebBrowser)
        var imgLoading: ImageView = findViewById(R.id.imgRobotWebBrowser)
        txtLoading.isGone = true
        bgLoading.isGone = true
        imgLoading.isGone = true
        stopAnimation(imgLoading)
    }

    fun openWebBrowser(type: String) {
        //val email = findViewById<EditText>(R.id.txt_email_login).text

        if (type == "login" || (type != "login" && type != "logout" && type.contains("https://auth.mozilla.auth0.com/passwordless/verify_redirect?"))) {
            setContentView(R.layout.activity_webbrowser)

            webView = findViewById(R.id.webViewBrowser)

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.userAgentString = webView.settings.userAgentString.replace("; wv", "")
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    // Loading started
                    showLoading()
                    //println("-->> PageStarted - URL: " + url + "<<--")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    // Loading finished
                    hideLoading()
                    //println("-->> PageFinished - URL: " + url + "<<--")

                    var cookies: String? = CookieManager.getInstance().getCookie(url)
                    //println(" ---->> " + url + " >> " + CookieManager.getInstance().getCookie(url) + " <<---- " )
                    if (url!!.contains("https://voice.mozilla.org/") && cookies != null && cookies.contains(
                            "connect.sid="
                        )
                    ) {
                        loginSuccessful()
                        showLoading()
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
                        //println(" -->> MY COOKIE -->> " + myCookie + " <<--")

                        getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE).edit()
                            .putBoolean(LOGGED_IN_NAME, true).apply()
                        getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE).edit()
                            .putString(USER_CONNECT_ID, userId).apply()

                        //println(" -->> LOGGED IN <<-- ")

                        loadUserData("userName")
                    } else {
                        println("??-- I can't get cookie - Something was wrong --??")
                    }
                }
            }
            if (type == "login") {
                webView.loadUrl("https://voice.mozilla.org/login")
            } else {
                webView.loadUrl(type)
            }
        }
    }

    fun logoutAndExit(exit: Boolean = true) {
        getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE).edit()
            .putBoolean(LOGGED_IN_NAME, false).apply()
        getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE).edit()
            .putString(USER_CONNECT_ID, "").apply()
        getSharedPreferences(USER_NAME, PRIVATE_MODE).edit().putString(USER_NAME, "").apply()
        getSharedPreferences(TODAY_CONTRIBUTING, PRIVATE_MODE).edit()
            .putString(TODAY_CONTRIBUTING, "?, ?, ?").apply()
        setLevelRecordingsValidations(0, 0)
        setLevelRecordingsValidations(1, 0)
        setLevelRecordingsValidations(2, 0)
        if (exit) {
            openMainAfterLogin()
        }
    }

    fun setLevelRecordingsValidations(type: Int, value: Int) {
        when (type) {
            0 -> {
                //level
                getSharedPreferences(LEVEL_SAVED, PRIVATE_MODE).edit()
                    .putInt(LEVEL_SAVED, value).apply()
            }
            1 -> {
                //recordings
                getSharedPreferences(RECORDINGS_SAVED, PRIVATE_MODE).edit()
                    .putInt(RECORDINGS_SAVED, value).apply()
            }
            2 -> {
                //validations
                getSharedPreferences(VALIDATIONS_SAVED, PRIVATE_MODE).edit()
                    .putInt(VALIDATIONS_SAVED, value).apply()
            }
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
        val returnIntent = Intent()
        //returnIntent.putExtra("key", "value")
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    fun reopenLogin() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        openMainAfterLogin()
        return false
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
                    // println("-->> "+it.toString()+" <<--")
                    if (it.toString() != "null") {
                        val jsonResult = it.toString()
                        if (jsonResult.length > 2) {
                            try {
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

                                    var profileImage: ImageView =
                                        findViewById(R.id.imageProfileImage)
                                    var profileImageBorder: ImageView =
                                        findViewById(R.id.imageProfileImageBorder)
                                    //should set also the profileImage
                                    var profileEmail: EditText = findViewById(R.id.textProfileEmail)
                                    var profileUsername: EditText =
                                        findViewById(R.id.textProfileUsername)
                                    getSharedPreferences(USER_NAME, PRIVATE_MODE).edit()
                                        .putString(USER_NAME, userName).apply()
                                    var profileAge: EditText = findViewById(R.id.textProfileAge)
                                    var profileGender: EditText =
                                        findViewById(R.id.textProfileGender)
                                    profileEmail.setText(jsonObj.getString("email").toString())
                                    profileUsername.setText(
                                        jsonObj.getString("username").toString()
                                    )
                                    profileAge.setText(
                                        getAgeString(
                                            jsonObj.getString("age").toString()
                                        )
                                    )
                                    profileGender.setText(
                                        getGenderString(
                                            jsonObj.getString("gender").toString()
                                        )
                                    )
                                    if (imageUrl != "null" && imageUrl != "") {
                                        DownLoadImage(
                                            profileImage,
                                            profileImageBorder
                                        ).execute(imageUrl)
                                    } else {
                                        DownLoadImage(
                                            profileImage,
                                            profileImageBorder
                                        ).execute("null")
                                    }
                                    val clips_count =
                                        jsonObj.getString("clips_count").toInt() //recordings
                                    val votes_count =
                                        jsonObj.getString("votes_count").toInt() //validations
                                    setLevelRecordingsValidations(0, clips_count + votes_count)
                                    setLevelRecordingsValidations(1, clips_count)
                                    setLevelRecordingsValidations(2, votes_count)

                                    setLevel(findViewById(R.id.textLevel))
                                }
                                if (userName != "") {
                                    getSharedPreferences(USER_NAME, PRIVATE_MODE).edit()
                                        .putString(USER_NAME, userName).apply()
                                }
                            } catch (e: Exception) {
                                error2(e.toString())
                            }
                        } else {
                            error2()
                        }
                    } else {
                        error4()
                    }
                }, Response.ErrorListener {
                    //println(" -->> Something wrong: "+it.toString()+" <<-- ")
                    error2(it.toString())
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    if (userId == "") {
                        if (getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE).getBoolean(
                                LOGGED_IN_NAME,
                                false
                            )
                        ) {
                            userId = getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE).getString(
                                USER_CONNECT_ID,
                                ""
                            ) ?: ""
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
            hideLoading()
            error2(e.toString())
        }
    }

    fun error2(details: String = "") {
        //error while getting the username
        this.userName = ""
        //openMainAfterLogin()
        try {
            this.textProfileEmail.setText("?")
            this.textProfileUsername.setText("?")
            this.textProfileAge.setText("?")
            this.textProfileGender.setText("?")
        } catch (e: Exception) {
            //
        }

        //EXL01
        showMessageDialog(
            getString(R.string.messageDialogErrorTitle),
            getString(R.string.messageDialogErrorCode),
            errorCode = "L01",
            details = details
        )
    }

    fun error4() {
        //User have to accept Privacy Policy on website
        logoutAndExit(false)
        showYouHaveToAcceptPrivacyPolicy()
    }

    fun showYouHaveToAcceptPrivacyPolicy() {
        setContentView(R.layout.you_have_to_accept_privacy_policy)

        //EXL02
        showMessageDialog(
            getString(R.string.youHaveToAcceptPrivacyPolicyTitle),
            getString(R.string.youHaveToAcceptPrivacyPolicy),
            errorCode = "L02"
        )

        var btnLoginSignUp: Button = this.findViewById(R.id.btnCloseLoginPrivacyPolicy)
        btnLoginSignUp.setOnClickListener {
            logoutAndExit()
        }
        var btnLoginOpenCommonVoice: Button = this.findViewById(R.id.btnOpenPrivacyPolicy)
        btnLoginOpenCommonVoice.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://voice.mozilla.org/"))
            startActivity(browserIntent)
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
            println("!!-- Exception: LoginActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
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


    override fun attachBaseContext(newBase: Context) {
        var tempLang = newBase.getSharedPreferences("LANGUAGE", 0).getString("LANGUAGE", "en")
        var lang = tempLang?.split("-")?.get(0) ?: ""
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
        //configuration.locales = localeList
        return createConfigurationContext(configuration)
    }
}