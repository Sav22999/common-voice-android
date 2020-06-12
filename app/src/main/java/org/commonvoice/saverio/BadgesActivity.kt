package org.commonvoice.saverio

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.all_badges.*
import org.commonvoice.saverio.ui.VariableLanguageActivity


class BadgesActivity : VariableLanguageActivity(R.layout.all_badges) {

    var level: Int = 0
    var recorded: Int = 0
    var validated: Int = 0
    private val PRIVATE_MODE = 0
    private val LEVEL_SAVED = "LEVEL_SAVED"
    private val RECORDINGS_SAVED = "RECORDINGS_SAVED"
    private val VALIDATIONS_SAVED = "VALIDATIONS_SAVED"
    private val GESTURES = "GESTURES"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        try {
            actionBar?.title = getString(R.string.labelAllBadges)
        } catch (exception: Exception) {
            println("!! Exception: (BadgesActivity) I can't set Title in ActionBar (method1) -- " + exception.toString() + " !!")
        }
        try {
            supportActionBar?.setTitle(getString(R.string.labelAllBadges))
        } catch (exception: Exception) {
            println("!! Exception: (BadgesActivity) I can't set Title in ActionBar (method2) -- " + exception.toString() + " !!")
        }

        val btnCloseBadges = this.btnCloseBadges
        btnCloseBadges.setOnClickListener {
            finish()
        }
        this.level = this.getSavedLevel()
        this.recorded = this.getSavedRecording()
        this.validated = this.getSavedValidation()
        loadBadges()
        //checkNewBadges(2, 2, 2)//remove this

        if (getGestures()) {
            layoutAllBadges.setOnTouchListener(object : OnSwipeTouchListener(this@BadgesActivity) {
                override fun onSwipeRight() {
                    onBackPressed()
                }
            })
        }
    }

    fun getGestures(): Boolean {
        return getSharedPreferences(
            GESTURES,
            PRIVATE_MODE
        ).getBoolean(
            GESTURES,
            false
        )
    }

    fun loadBadges() {
        val imagesLevel: List<ImageView> = listOf<ImageView>(
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
        val imagesRecording: List<ImageView> = listOf<ImageView>(
            this.imageR5Badge,
            this.imageR50Badge,
            this.imageR100Badge,
            this.imageR500Badge,
            this.imageR1KBadge,
            this.imageR5KBadge,
            this.imageR10KBadge
        )
        val imagesValidation: List<ImageView> = listOf<ImageView>(
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
        val theme = DarkLightTheme()

        val isDark = theme.getTheme(view)
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
        val value = getSharedPreferences(LEVEL_SAVED, PRIVATE_MODE).getInt(LEVEL_SAVED, 0)
        println("level: " + value)
        return when (value) {
            in 0..9 -> 1
            in 10..49 -> 2
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
        val value = getSharedPreferences(RECORDINGS_SAVED, PRIVATE_MODE).getInt(RECORDINGS_SAVED, 0)
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
        val value =
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
}