package org.commonvoice.saverio

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.all_badges.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.ui.recyclerview.badges.BadgeAdapter
import org.commonvoice.saverio.ui.recyclerview.badges.BadgeData


class BadgesActivity : VariableLanguageActivity(R.layout.all_badges) {

    var level: Int = 0
    var recorded: Int = 0
    var validated: Int = 0
    private val PRIVATE_MODE = 0
    private val LEVEL_SAVED = "LEVEL_SAVED"
    private val RECORDINGS_SAVED = "RECORDINGS_SAVED"
    private val VALIDATIONS_SAVED = "VALIDATIONS_SAVED"

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

        loadBadges()

        if (mainPrefManager.areGesturesEnabled) {
            layoutAllBadges.setOnTouchListener(object : OnSwipeTouchListener(this@BadgesActivity) {
                override fun onSwipeRight() {
                    onBackPressed()
                }
            })
        }
    }

    fun loadBadges() {
        badgesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@BadgesActivity, 3)
            adapter = BadgeAdapter(
                BadgeData.generateBadgeData(
                    getSharedPreferences(LEVEL_SAVED, PRIVATE_MODE).getInt(LEVEL_SAVED, 0),
                    getSharedPreferences(RECORDINGS_SAVED, PRIVATE_MODE).getInt(RECORDINGS_SAVED, 0),
                    getSharedPreferences(VALIDATIONS_SAVED, PRIVATE_MODE).getInt(VALIDATIONS_SAVED, 0)
                )
            )
        }

        setTheme(this)
    }

    fun setTheme(view: Context) {
        val theme = DarkLightTheme()

        val isDark = theme.getTheme(view)
        theme.setElement(isDark, this.findViewById(R.id.layoutAllBadges) as ConstraintLayout)
        theme.setElement(isDark, view, this.findViewById(R.id.btnCloseBadges) as Button)
    }

}