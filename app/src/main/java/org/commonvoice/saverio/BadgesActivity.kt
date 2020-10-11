package org.commonvoice.saverio

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import org.commonvoice.saverio.databinding.AllBadgesBinding
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.ui.recyclerview.badges.Badge
import org.commonvoice.saverio.ui.recyclerview.badges.BadgeAdapter
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt


class BadgesActivity : ViewBoundActivity<AllBadgesBinding>(
    AllBadgesBinding::inflate
) {

    private val statsPrefManager by inject<StatsPrefManager>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.labelAllBadges)
        supportActionBar?.setTitle(R.string.labelAllBadges)

        binding.btnCloseBadges.setOnClickListener {
            finish()
        }

        loadBadges()

        if (mainPrefManager.areGesturesEnabled) {
            binding.layoutAllBadges.setOnTouchListener(object : OnSwipeTouchListener(this@BadgesActivity) {
                override fun onSwipeRight() {
                    onBackPressed()
                }
            })
        }
    }

    private fun loadBadges() {
        val columnsNumber = determineColumnNumber()

        binding.badgesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@BadgesActivity, columnsNumber)
            adapter = BadgeAdapter(
                Badge.generateBadgeData(
                    statsPrefManager.parsedLevel,
                    statsPrefManager.allTimeRecorded,
                    statsPrefManager.allTimeValidated
                )
            ) { badge ->
                val message = badge.let {
                    when (it) {
                        is Badge.Level -> {
                            getString(R.string.message_got_badge_because_levels).replace(
                                "{{*{{n_total}}*}}",
                                it.value.toString()
                            )
                        }
                        is Badge.ListenAchievement -> {
                            getString(R.string.message_got_badge_because_clips).replace(
                                "{{*{{n_clips}}*}}",
                                it.value.toString()
                            )
                        }
                        is Badge.SpeakAchievement -> {
                            getString(R.string.message_got_badge_because_sentences).replace(
                                "{{*{{n_sentences}}*}}",
                                it.value.toString()
                            )
                        }
                    }
                }

                showMessageDialog("", message, type = 5)
            }
        }

        setTheme()
    }

    private fun determineColumnNumber(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val widthDp = displayMetrics.widthPixels / displayMetrics.density

        return (widthDp / 120.0f).roundToInt()
    }

    private fun showMessageDialog(
        title: String,
        text: String,
        errorCode: String = "",
        details: String = "",
        type: Int = 0
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
            var message: MessageDialog? = null
            message = MessageDialog(
                this,
                type,
                title,
                messageText,
                details = details,
                height = height
            )
            message?.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
    }

    private fun setTheme() {
        theme.setElement(binding.layoutAllBadges)
        theme.setElement(this, binding.btnCloseBadges)
    }

}