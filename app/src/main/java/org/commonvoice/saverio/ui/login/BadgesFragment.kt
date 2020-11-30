package org.commonvoice.saverio.ui.login

import android.annotation.SuppressLint
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import org.commonvoice.saverio.MessageDialog
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.AllBadgesBinding
import org.commonvoice.saverio.ui.recyclerview.badges.Badge
import org.commonvoice.saverio.ui.recyclerview.badges.BadgeAdapter
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt


class BadgesFragment : ViewBoundFragment<AllBadgesBinding>() {

    override fun inflate(layoutInflater: LayoutInflater, container: ViewGroup?): AllBadgesBinding {
        return AllBadgesBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val statsPrefManager by inject<StatsPrefManager>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()

        activity?.setTitle(R.string.labelAllBadges)

        binding.btnCloseBadges.setOnClickListener {
            findNavController().navigateUp()
        }

        loadBadges()

        if (mainPrefManager.areGesturesEnabled) {
            binding.layoutAllBadges.setOnTouchListener(object :
                OnSwipeTouchListener(requireContext()) {
                override fun onSwipeRight() {
                    activity?.onBackPressed()
                }
            })
        }
    }

    private fun loadBadges() {
        val columnsNumber = determineColumnNumber()

        binding.badgesRecyclerView.apply {
            setOnTouchListener(
                object : OnSwipeTouchListener(requireContext()) {
                    override fun onSwipeRight() {
                        activity?.onBackPressed()
                    }
                }
            )
            layoutManager = GridLayoutManager(requireContext(), columnsNumber)
            adapter = BadgeAdapter(
                Badge.generateBadgeData(
                    statsPrefManager.parsedLevel,
                    statsPrefManager.allTimeRecorded,
                    statsPrefManager.allTimeValidated
                )
            ) { badge ->
                val message = when (badge) {
                    is Badge.Level -> {
                        getString(R.string.message_got_badge_because_levels).replace(
                            "{{*{{n_total}}*}}",
                            badge.value.toString()
                        )
                    }
                    is Badge.ListenAchievement -> {
                        getString(R.string.message_got_badge_because_clips).replace(
                            "{{*{{n_clips}}*}}",
                            badge.value.toString()
                        )
                    }
                    is Badge.SpeakAchievement -> {
                        getString(R.string.message_got_badge_because_sentences).replace(
                            "{{*{{n_sentences}}*}}",
                            badge.value.toString()
                        )
                    }
                }

                if (badge.badgeValue > 0) {
                    showMessageDialog("", message, type = 5)
                }
            }
        }

        setTheme()
    }

    private fun determineColumnNumber(): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

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
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
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
                requireContext(),
                type,
                title,
                messageText,
                details = details,
                height = height
            )
            message.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
    }

    private fun setTheme() {
        theme.setElement(binding.layoutAllBadges)
        theme.setElement(requireContext(), binding.btnCloseBadges)
    }

}