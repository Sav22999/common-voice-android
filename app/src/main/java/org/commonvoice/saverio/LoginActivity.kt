package org.commonvoice.saverio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.work.WorkManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_webbrowser.*
import kotlinx.android.synthetic.main.bottomsheet_login.view.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.LoginViewModel
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : VariableLanguageActivity(R.layout.activity_login) {

    private val loginViewModel: LoginViewModel by viewModel()
    private val workManager: WorkManager by inject()

    private val statsPrefManager by inject<StatsPrefManager>()

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView
    private var PRIVATE_MODE = 0
    //private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    //private val TODAY_CONTRIBUTING = "TODAY_CONTRIBUTING" //saved as "yyyy/mm/dd, n_recorded, n_validated"

    private val settingsSwitchData: HashMap<String, String> =
        hashMapOf(
            "LOGGED_IN_NAME" to "LOGGED",
            "USER_NAME" to "USERNAME",
            "DAILY_GOAL" to "DAILY_GOAL",
            "TODAY_CONTRIBUTING" to "TODAY_CONTRIBUTING"
        )

    var userId: String = ""
    var userName: String = ""

    val urlWithoutLang: String =
        "https://commonvoice.mozilla.org/api/v1/" //API url (without lang)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    Uri.parse("https://commonvoice.mozilla.org/profile/info")
                )
            )
        }

        try {
            actionBar.setTitle(getString(R.string.button_home_profile))
        } catch (exception: Exception) {
            println("!! Exception: (LoginActivity) I can't set Title in ActionBar (method1) -- " + exception.toString() + " !!")
        }
        try {
            supportActionBar?.setTitle(getString(R.string.button_home_profile))
        } catch (exception: Exception) {
            println("!! Exception: (LoginActivity) I can't set Title in ActionBar (method2) -- " + exception.toString() + " !!")
        }

        if (getSharedPreferences(settingsSwitchData["LOGGED_IN_NAME"], PRIVATE_MODE).getBoolean(
                settingsSwitchData["LOGGED_IN_NAME"],
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
                    setTheme2()
                }
            } catch (e: Exception) {
                openWebBrowser("login")
                setTheme2()
            }

            if (mainPrefManager.areGesturesEnabled) {
                layoutWebBrowser.setOnTouchListener(object :
                    OnSwipeTouchListener(this@LoginActivity) {
                    override fun onSwipeRight() {
                        onBackPressed()
                    }
                })
            }
        } else {
            loadUserData("profile")
            setTheme()

            if (mainPrefManager.areGesturesEnabled) {
                nestedScrollLogin.setOnTouchListener(object :
                    OnSwipeTouchListener(this@LoginActivity) {
                    override fun onSwipeRight() {
                        onBackPressed()
                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        openMainAfterLogin()
    }

    fun getSavedLevel(): Int {
        return statsPrefManager.parsedLevel
    }

    fun setLevel(textLevel: TextView) {
        textLevel.isGone = false
        val nLevel = getSavedLevel()
        textLevel.text = getString(R.string.txt_your_level).replace(
            "{{*{{level}}*}}",
            nLevel.toString()
        )
    }

    fun setTheme() {
        theme.setElement(this.findViewById(R.id.layoutLogin) as ConstraintLayout)
        theme.setElement(this, 3, this.findViewById(R.id.loginSectionData))
        theme.setElement(this, 3, this.findViewById(R.id.loginSectionInformation))
        theme.setElement(this, 1, this.findViewById(R.id.loginSectionLogout))
        theme.setElement(this, this.findViewById(R.id.btnBadges) as Button)
        theme.setElement(this, this.findViewById(R.id.btnLogout) as Button)
        theme.setElement(
            this,
            this.findViewById(R.id.labelToModifyInformation) as TextView,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
        theme.setTextView(
            this,
            this.findViewById(R.id.textProfileUsername) as TextView
        )
        theme.setTextView(
            this,
            this.findViewById(R.id.textProfileEmail) as TextView
        )
        theme.setTextView(
            this,
            this.findViewById(R.id.textProfileAge) as TextView
        )
        theme.setTextView(
            this,
            this.findViewById(R.id.textProfileGender) as TextView
        )
        theme.setElement(this, this.findViewById(R.id.labelProfileUsername) as TextView)
        theme.setElement(this, this.findViewById(R.id.labelProfileEmail) as TextView)
        theme.setElement(this, this.findViewById(R.id.labelProfileAge) as TextView)
        theme.setElement(this, this.findViewById(R.id.labelProfileGender) as TextView)
        theme.setTextView(this, textLevel as TextView, border = false)
    }

    fun setTheme2() {
        theme.setElement(
            this,
            this.findViewById(R.id.btnAlreadyAVerificationLinkWebBrowser) as Button
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
        var btnHaveLink: Button = findViewById(R.id.btnAlreadyAVerificationLinkWebBrowser)
        txtLoading.isGone = false
        bgLoading.isGone = false
        imgLoading.isGone = false
        btnHaveLink.isGone = true
        stopAnimation(btnHaveLink)
        startAnimation(imgLoading, R.anim.login)
    }

    fun hideLoading(showButton: Boolean = false) {
        var txtLoading: TextView = findViewById(R.id.txtLoadingWebBrowser)
        var bgLoading: ImageView = findViewById(R.id.imgBackgroundWebBrowser)
        var imgLoading: ImageView = findViewById(R.id.imgRobotWebBrowser)
        var btnHaveLink: Button = findViewById(R.id.btnAlreadyAVerificationLinkWebBrowser)
        txtLoading.isGone = true
        bgLoading.isGone = true
        imgLoading.isGone = true
        stopAnimation(imgLoading)
        if (showButton) {
            btnHaveLink.isGone = false
            startAnimation(btnHaveLink, R.anim.zoom_in)
        }
    }

    fun openWebBrowser(type: String) {
        //val email = findViewById<EditText>(R.id.txt_email_login).text

        if (type == "login" || (type != "login" && type != "logout" && type.contains("https://auth.mozilla.auth0.com/passwordless/verify_redirect?"))) {
            setContentView(R.layout.activity_webbrowser)

            val bottomSheet = BottomSheetDialog(this)
            val viewBottomSheet = layoutInflater.inflate(R.layout.bottomsheet_login, null)
            bottomSheet.setContentView(viewBottomSheet)

            btnAlreadyAVerificationLinkWebBrowser.setOnClickListener {
                bottomSheet.show()
                viewBottomSheet.textURLVerificationLink.setText("")
                viewBottomSheet.textURLVerificationLink.requestFocus()
            }
            viewBottomSheet.textURLVerificationLink.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    if ((viewBottomSheet.textURLVerificationLink.text).contains("https://auth.mozilla.auth0.com/passwordless/verify_redirect?")) {
                        bottomSheet.dismiss()
                        showLoading()
                        webView.loadUrl(viewBottomSheet.textURLVerificationLink.text.toString())
                        return@OnKeyListener true
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            getString(R.string.txt_verification_link_not_valid),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@OnKeyListener false
                }
                false
            })

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
                    hideLoading(showButton = (webView.url.contains("https://auth.mozilla.auth0.com/login")))
                    //println("-->> PageFinished - URL: " + url + "<<--")

                    var cookies: String? = CookieManager.getInstance().getCookie(url)
                    //println(" ---->> " + url + " >> " + CookieManager.getInstance().getCookie(url) + " <<---- " )
                    if (url!!.contains("https://commonvoice.mozilla.org/") && cookies != null && cookies.contains(
                            "connect.sid="
                        )
                    ) {
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
                            i++
                        }
                        userId = myCookie
                        //println(" -->> MY COOKIE -->> " + myCookie + " <<--")

                        getSharedPreferences(
                            settingsSwitchData["LOGGED_IN_NAME"],
                            PRIVATE_MODE
                        ).edit()
                            .putBoolean(settingsSwitchData["LOGGED_IN_NAME"], true).apply()

                        mainPrefManager.sessIdCookie = userId

                        loginViewModel.clearDB()

                        SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
                        ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)

                        //println(" -->> LOGGED IN <<-- ")

                        loadUserData("userName")
                    } else {
                        println("??-- I can't get cookie - Something was wrong --??")
                    }
                }
            }
            if (type == "login") {
                webView.loadUrl("https://commonvoice.mozilla.org/login")
            } else {
                webView.loadUrl(type)
            }
        }
    }

    fun logoutAndExit(exit: Boolean = true) {
        mainPrefManager.sessIdCookie = null
        getSharedPreferences(settingsSwitchData["LOGGED_IN_NAME"], PRIVATE_MODE).edit()
            .putBoolean(settingsSwitchData["LOGGED_IN_NAME"], false).apply()
        getSharedPreferences(settingsSwitchData["USER_NAME"], PRIVATE_MODE).edit()
            .putString(settingsSwitchData["USER_NAME"], "").apply()
        setLevelRecordingsValidations(0, 0)
        setLevelRecordingsValidations(1, 0)
        setLevelRecordingsValidations(2, 0)
        getSharedPreferences(settingsSwitchData["DAILY_GOAL"], PRIVATE_MODE).edit()
            .putInt(settingsSwitchData["DAILY_GOAL"], 0).apply()
        if (exit) {
            openMainAfterLogin()
        }
        CookieManager.getInstance().flush()
        CookieManager.getInstance().removeAllCookies(null)
        loginViewModel.clearDB()
    }

    fun setLevelRecordingsValidations(type: Int, value: Int) {
        when (type) {
            0 -> {
                //level
                statsPrefManager.allTimeLevel = value
            }
            1 -> {
                //recordings
                statsPrefManager.allTimeRecorded = value
            }
            2 -> {
                //validations
                statsPrefManager.allTimeValidated = value
            }
        }
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

    fun loadUserData(type: String) {
        if (type == "profile") {
            //var profileImage: ImageView = findViewById(R.id.imageProfileImage)
            var profileEmail: EditText = findViewById(R.id.textProfileEmail)
            var profileUsername: EditText = findViewById(R.id.textProfileUsername)
            var profileAge: EditText = findViewById(R.id.textProfileAge)
            var profileGender: EditText = findViewById(R.id.textProfileGender)
            var allBadgesButton: Button = findViewById(R.id.btnBadges)
            profileEmail.setText("···")
            profileUsername.setText("···")
            profileAge.setText("···")
            profileGender.setText("···")
            allBadgesButton.isEnabled = false
        }
        try {
            val path = "user_client" //API to get sentences

            if (userId == "") {
                if (getSharedPreferences(
                        settingsSwitchData["LOGGED_IN_NAME"],
                        PRIVATE_MODE
                    ).getBoolean(
                        settingsSwitchData["LOGGED_IN_NAME"],
                        false
                    )
                ) {
                    userId = mainPrefManager.sessIdCookie ?: ""
                }
            }

            val que = Volley.newRequestQueue(this)
            val req = object : StringRequest(Request.Method.GET, urlWithoutLang + path,
                Response.Listener {
                    //println("-->> " + it.toString() + " <<--")
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
                                    var profileEmail: EditText = findViewById(R.id.textProfileEmail)
                                    var profileUsername: EditText =
                                        findViewById(R.id.textProfileUsername)
                                    getSharedPreferences(
                                        settingsSwitchData["USER_NAME"],
                                        PRIVATE_MODE
                                    ).edit()
                                        .putString(settingsSwitchData["USER_NAME"], userName)
                                        .apply()
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
                                            profileImage
                                        ).execute(imageUrl)
                                    } else {
                                        DownLoadImage(
                                            profileImage
                                        ).execute("null")
                                    }
                                    var allBadgesButton: Button = findViewById(R.id.btnBadges)
                                    allBadgesButton.isEnabled = true
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
                                    getSharedPreferences(
                                        settingsSwitchData["USER_NAME"],
                                        PRIVATE_MODE
                                    ).edit()
                                        .putString(settingsSwitchData["USER_NAME"], userName)
                                        .apply()
                                }
                            } catch (e: Exception) {
                                println(" -->> Something wrong: " + it.toString() + " <<-- ")
                                error2(e.toString())
                            }
                        } else {
                            error2()
                        }
                    } else {
                        error4()
                    }
                }, Response.ErrorListener {
                    println(" -->> Something wrong: " + it.toString() + " <<-- ")
                    error2(it.toString())
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    //println(">>1>>" + userId)
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
            getString(R.string.youHaveToAcceptPrivacyPolicy)
        )

        var btnLoginSignUp: Button = this.findViewById(R.id.btnCloseLoginPrivacyPolicy)
        btnLoginSignUp.setOnClickListener {
            logoutAndExit()
        }
        var btnLoginOpenCommonVoice: Button = this.findViewById(R.id.btnOpenPrivacyPolicy)
        btnLoginOpenCommonVoice.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://commonvoice.mozilla.org/"))
            startActivity(browserIntent)
        }
    }

    fun showMessageDialog(
        title: String,
        text: String,
        errorCode: String = "",
        details: String = ""
    ) {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        //val width = metrics.widthPixels
        val height = metrics.heightPixels
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
                MessageDialog(this, 0, title, messageText, details = details, height = height)
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

}