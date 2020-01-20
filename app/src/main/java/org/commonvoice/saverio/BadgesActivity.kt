package org.commonvoice.saverio

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.all_badges.*
import java.util.*


class BadgesActivity() : AppCompatActivity() {

    var level: Int = 0
    var recorded: Int = 0
    var validated: Int = 0
    private var PRIVATE_MODE = 0
    private val LEVEL_SAVED = "LEVEL_SAVED"
    private val RECORDINGS_SAVED = "RECORDINGS_SAVED"
    private val VALIDATIONS_SAVED = "VALIDATIONS_SAVED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_badges)

        var btnCloseBadges = this.btnCloseBadges
        btnCloseBadges.setOnClickListener {
            finish()
        }
        this.level = this.getSavedLevel()
        this.recorded = this.getSavedRecording()
        this.validated = this.getSavedValidation()
        loadBadges()
        //checkNewBadges(2, 2, 2)//remove this
    }

    fun loadBadges() {
        var imagesLevel: List<ImageView> = listOf<ImageView>(
            this.imageFirstLevelBadge,
            this.imageSecondLevelBadge,
            this.imageThirdLevelBadge,
            this.imageFourthLevelBadge,
            this.imageFifthLevelBadge,
            this.imageSixthLevelBadge,
            this.imageSeventhLevelBadge,
            this.imageEighthLevelBadge,
            this.imageNinthLevelBadge,
            this.imageTenthLevelBadge
        )
        var imagesRecording: List<ImageView> = listOf<ImageView>(
            this.imageR5Badge,
            this.imageR50Badge,
            this.imageR100Badge,
            this.imageR500Badge,
            this.imageR1KBadge,
            this.imageR5KBadge,
            this.imageR10KBadge
        )
        var imagesValidation: List<ImageView> = listOf<ImageView>(
            this.imageV5Badge,
            this.imageV50Badge,
            this.imageV100Badge,
            this.imageV500Badge,
            this.imageV1KBadge,
            this.imageV5KBadge,
            this.imageV10KBadge
        )
        loadLevels(imagesLevel)
        loadRecorded(imagesRecording)
        loadValidated(imagesValidation)

        setTheme(this)
    }

    fun setTheme(view: Context) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        theme.setElement(isDark, this.findViewById(R.id.layoutAllBadges) as ConstraintLayout)
        theme.setElement(isDark, view, this.findViewById(R.id.btnCloseBadges) as Button)
    }

    fun setOff(iv: ImageView) {
        iv.alpha = 0.3f
        iv.setColorFilter(Color.argb(150, 230, 230, 230))
    }

    fun setOn(iv: ImageView) {
        iv.alpha = 1.0f
        iv.setColorFilter(Color.argb(0, 230, 230, 230))
    }

    fun loadLevels(images: List<ImageView>) {
        //load level badges
        loadImages(images, this.level)
    }

    fun loadRecorded(images: List<ImageView>) {
        //load recorded-badges
        loadImages(images, this.recorded)
    }

    fun loadValidated(images: List<ImageView>) {
        //load validated-badges
        loadImages(images, this.validated)
    }

    fun loadImages(images: List<ImageView>, value: Int) {
        for (x in 0 until images.size) {
            //set to not-reached every level badges
            setOff(images[x])
        }
        for (x in 0 until value) {
            //set to reached badges
            setOn(images[x])
        }
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

    fun getSavedRecording(): Int {
        var value = getSharedPreferences(RECORDINGS_SAVED, PRIVATE_MODE).getInt(RECORDINGS_SAVED, 0)
        println("recordings: " + value)
        return when (value) {
            in 0..4 -> 0
            in 5..49 -> 1
            in 50..99 -> 2
            in 100..499 -> 3
            in 500..999 -> 4
            in 1000..4999 -> 5
            in 5000..9999 -> 6
            in 10000..100000000 -> 7
            else -> 0
        }
    }

    fun getSavedValidation(): Int {
        var value =
            getSharedPreferences(VALIDATIONS_SAVED, PRIVATE_MODE).getInt(VALIDATIONS_SAVED, 0)
        println("validations: " + value)
        return when (value) {
            in 0..4 -> 0
            in 5..49 -> 1
            in 50..99 -> 2
            in 100..499 -> 3
            in 500..999 -> 4
            in 1000..4999 -> 5
            in 5000..9999 -> 6
            in 10000..100000000 -> 7
            else -> 0
        }
    }

    fun checkNewBadges(newLevel: Int, newRecorded: Int, newValidated: Int) {
        //check if it's reached a new badge, in that case show a push-notification
        //call this method also from external class
        //parameters are necessary because compare savedInt with the newInt

        //sendNotification("Example", "Title", 1)
    }

    private fun sendNotification(
        message: String,
        title: String,
        id: Int
    ) {
        val intent = Intent(this, BadgesActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */,
            intent, PendingIntent.FLAG_ONE_SHOT
        )
        val notificationBuilder: Notification.Builder? = Notification.Builder(this)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            id /* ID of notification */,
            notificationBuilder?.build()
        )
    }

    fun setNewLevel(value: Int) {
        //notify
    }

    fun setNewRecordings(value: Int) {
        //notify
    }

    fun setNewValidations(value: Int) {
        //notify
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