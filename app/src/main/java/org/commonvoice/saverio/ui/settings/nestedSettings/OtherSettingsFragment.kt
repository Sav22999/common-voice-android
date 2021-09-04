package org.commonvoice.saverio.ui.settings.nestedSettings

import android.app.Activity
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentOtherSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.koin.android.ext.android.inject
import android.widget.ArrayAdapter


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

            val listOfHours = arrayOf(
                "0:00",
                "1:00",
                "2:00",
                "3:00",
                "4:00",
                "5:00",
                "6:00",
                "7:00",
                "8:00",
                "9:00",
                "10:00",
                "11:00",
                "12:00",
                "13:00",
                "14:00",
                "15:00",
                "16:00",
                "17:00",
                "18:00",
                "19:00",
                "20:00",
                "21:00",
                "22:00",
                "23:00"
            )
            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    listOfHours
                )
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            textHourDailyGoalNotifications.setAdapter(adapter)

            switchDailygoalNotifications.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.dailyGoalNotifications = isChecked
                settingsSectionCustomiseDailyGoalNotifications.isGone = !isChecked
                if (!isChecked) {
                    settingsPrefManager.dailyGoalNotificationsHour = 17
                }
                textHourDailyGoalNotifications.setSelection(listOfHours.indexOf(settingsPrefManager.dailyGoalNotificationsHour.toString() + ":00"))
            }
            switchDailygoalNotifications.isChecked = settingsPrefManager.dailyGoalNotifications
            settingsSectionCustomiseDailyGoalNotifications.isGone =
                !settingsPrefManager.dailyGoalNotifications
            textHourDailyGoalNotifications.setSelection(listOfHours.indexOf(settingsPrefManager.dailyGoalNotificationsHour.toString() + ":00"))

            textHourDailyGoalNotifications.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        settingsPrefManager.dailyGoalNotificationsHour =
                            adapter.getItem(position).toString().replace(":00", "").toInt()
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