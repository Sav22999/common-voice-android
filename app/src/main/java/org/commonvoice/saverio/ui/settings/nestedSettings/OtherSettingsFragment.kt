package org.commonvoice.saverio.ui.settings.nestedSettings

import android.app.Activity
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentOtherSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.koin.android.ext.android.inject

class OtherSettingsFragment : ViewBoundFragment<FragmentOtherSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOtherSettingsBinding {
        return FragmentOtherSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val settingsPrefManager by inject<SettingsPrefManager>()

    override fun onStart() {
        super.onStart()

        withBinding {
            buttonBackSettingsSubSectionOther.setOnClickListener {
                activity?.onBackPressed()
            }

            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsOther.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            switchCheckUpdates.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.automaticallyCheckForUpdates = isChecked
            }
            switchCheckUpdates.isChecked = settingsPrefManager.automaticallyCheckForUpdates

            switchGeneralNotifications.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.notifications = isChecked
            }
            switchGeneralNotifications.isChecked = settingsPrefManager.notifications

            switchDailygoalNotifications.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.dailyGoalNotifications = isChecked
                settingsSectionCustomiseDailyGoalNotifications.isGone = !isChecked
                if (!isChecked) {
                    settingsPrefManager.dailyGoalNotificationsHour = 17
                    settingsPrefManager.dailyGoalNotificationsLastSentDate = ""
                }
                textHourDailyGoalNotifications.setText(settingsPrefManager.dailyGoalNotificationsHour.toString())
            }
            switchDailygoalNotifications.isChecked = settingsPrefManager.dailyGoalNotifications
            settingsSectionCustomiseDailyGoalNotifications.isGone =
                !settingsPrefManager.dailyGoalNotifications
            textHourDailyGoalNotifications.setText(settingsPrefManager.dailyGoalNotificationsHour.toString())

            textHourDailyGoalNotifications.addTextChangedListener {
                val valueTemp = textHourDailyGoalNotifications.text.toString()
                if (valueTemp != "") {
                    if (valueTemp.toInt() > 24) {
                        textHourDailyGoalNotifications.setText("24")
                    }
                    settingsPrefManager.dailyGoalNotificationsHour =
                        (textHourDailyGoalNotifications.text).toString().toInt()
                } else {
                    settingsPrefManager.dailyGoalNotificationsHour = 17
                }
            }
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsOther)

            theme.setElements(requireContext(), settingsSubSectionOther)
            theme.setElements(requireContext(), settingsSectionCustomiseDailyGoalNotifications)
            theme.setElements(requireContext(), otherSubSubSectionDailyGoalNotificationsHour)

            theme.setElement(requireContext(), 3, settingsSubSectionOther)
            theme.setElement(requireContext(), 3, settingsSectionCustomiseDailyGoalNotifications)
            theme.setElement(
                requireContext(),
                3,
                otherSubSubSectionDailyGoalNotificationsHour,
                R.color.colorTabBackgroundInactive,
                R.color.colorTabBackgroundInactiveDT
            )

            theme.setTextView(
                requireContext(),
                textHourDailyGoalNotifications,
                border = false,
                intern = true
            )

            theme.setElement(
                requireContext(),
                labelDetailsTextDailyGoalNotifications,
                R.color.colorGray,
                R.color.colorLightGray,
                textSize = 15f
            )

            theme.setTitleBar(requireContext(), titleSettingsSubSectionOther, textSize = 20F)
        }
    }
}