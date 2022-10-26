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
import androidx.lifecycle.ViewModelProviders
import org.commonvoice.saverio_lib.viewmodels.GenericViewModel


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
            val viewModel = activity?.run {
                ViewModelProviders.of(this).get(GenericViewModel::class.java)
            } ?: throw Exception("?? Invalid Activity ??")
            if (viewModel.fromFragment.value != "settings") activity?.onBackPressed()

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
            val listOfHoursSecond = arrayOf(
                getString(R.string.label_dailygoal_notifications_second_alert_none),
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
                    R.layout.spinner_text,
                    listOfHours
                )
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_text)
            textHourDailyGoalNotificationsFirstAlert.setAdapter(adapter)

            val adapterSecond: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.spinner_text,
                    listOfHoursSecond
                )
            adapterSecond.setDropDownViewResource(R.layout.spinner_dropdown_text)
            textHourDailyGoalNotificationsSecondAlert.setAdapter(adapterSecond)

            switchDailygoalNotifications.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.dailyGoalNotifications = isChecked
                settingsSectionCustomiseDailyGoalNotifications.isGone = !isChecked
                if (!isChecked) {
                    settingsPrefManager.dailyGoalNotificationsHour = 17
                    settingsPrefManager.dailyGoalNotificationsHourSecond = -1
                }
                setChoosingHourSelection(listOfHours, listOfHoursSecond)
            }
            switchDailygoalNotifications.isChecked = settingsPrefManager.dailyGoalNotifications
            settingsSectionCustomiseDailyGoalNotifications.isGone =
                !settingsPrefManager.dailyGoalNotifications
            setChoosingHourSelection(listOfHours, listOfHoursSecond)

            textHourDailyGoalNotificationsFirstAlert.onItemSelectedListener =
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

                        if (settingsPrefManager.dailyGoalNotificationsHourSecond > 0) {
                            if (settingsPrefManager.dailyGoalNotificationsHour > settingsPrefManager.dailyGoalNotificationsHourSecond) {
                                val temp = settingsPrefManager.dailyGoalNotificationsHour
                                settingsPrefManager.dailyGoalNotificationsHour =
                                    settingsPrefManager.dailyGoalNotificationsHourSecond
                                settingsPrefManager.dailyGoalNotificationsHourSecond = temp
                                setChoosingHourSelection(listOfHours, listOfHoursSecond)
                            } else if (settingsPrefManager.dailyGoalNotificationsHour == settingsPrefManager.dailyGoalNotificationsHourSecond) {
                                settingsPrefManager.dailyGoalNotificationsHourSecond = -1
                                setChoosingHourSelection(listOfHours, listOfHoursSecond)
                            }
                        }
                    }
                }
            textHourDailyGoalNotificationsSecondAlert.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (adapterSecond.getItem(position)
                                .toString() == getString(R.string.label_dailygoal_notifications_second_alert_none)
                        ) {
                            settingsPrefManager.dailyGoalNotificationsHourSecond = -1
                        } else {
                            settingsPrefManager.dailyGoalNotificationsHourSecond =
                                adapterSecond.getItem(position).toString().replace(":00", "").toInt()
                            println(adapterSecond.getItem(position).toString())

                            if (settingsPrefManager.dailyGoalNotificationsHour > settingsPrefManager.dailyGoalNotificationsHourSecond) {
                                val temp = settingsPrefManager.dailyGoalNotificationsHour
                                settingsPrefManager.dailyGoalNotificationsHour =
                                    settingsPrefManager.dailyGoalNotificationsHourSecond
                                settingsPrefManager.dailyGoalNotificationsHourSecond = temp
                                setChoosingHourSelection(listOfHours, listOfHoursSecond)
                            } else if (settingsPrefManager.dailyGoalNotificationsHour == settingsPrefManager.dailyGoalNotificationsHourSecond) {
                                settingsPrefManager.dailyGoalNotificationsHourSecond = -1
                                setChoosingHourSelection(listOfHours, listOfHoursSecond)
                            }
                        }
                    }
                }
        }

        setTheme()
    }

    private fun setChoosingHourSelection(
        listOfHours: Array<String>,
        listOfHoursSecond: Array<String>
    ) {
        withBinding {
            textHourDailyGoalNotificationsFirstAlert.setSelection(
                listOfHours.indexOf(
                    settingsPrefManager.dailyGoalNotificationsHour.toString() + ":00"
                )
            )
            var selectionSecondAlert =
                settingsPrefManager.dailyGoalNotificationsHourSecond.toString() + ":00"
            if (settingsPrefManager.dailyGoalNotificationsHourSecond < 0) {
                selectionSecondAlert =
                    getString(R.string.label_dailygoal_notifications_second_alert_none)
            }
            textHourDailyGoalNotificationsSecondAlert.setSelection(
                listOfHoursSecond.indexOf(
                    selectionSecondAlert
                )
            )
        }
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsOther)

            theme.setElements(requireContext(), settingsSubSectionOther)
            theme.setElements(requireContext(), settingsSectionCustomiseDailyGoalNotifications)
            theme.setElements(requireContext(), otherSubSubSectionDailyGoalNotificationsFirstAlert)
            theme.setElements(requireContext(), otherSubSubSectionDailyGoalNotificationsSecondAlert)

            theme.setElement(requireContext(), 3, settingsSubSectionOther)
            theme.setElement(requireContext(), 3, settingsSectionCustomiseDailyGoalNotifications)
            theme.setElement(
                requireContext(),
                3,
                otherSubSubSectionDailyGoalNotificationsFirstAlert,
                R.color.colorTabBackgroundInactive,
                R.color.colorTabBackgroundInactiveDT
            )
            theme.setElement(
                requireContext(),
                3,
                otherSubSubSectionDailyGoalNotificationsSecondAlert,
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

            theme.setSpinner(
                requireContext(),
                textHourDailyGoalNotificationsFirstAlert,
                R.drawable.spinner_background,
                R.drawable.spinner_background_dark
            )
            theme.setSpinner(
                requireContext(),
                textHourDailyGoalNotificationsSecondAlert,
                R.drawable.spinner_background,
                R.drawable.spinner_background_dark
            )

            theme.setTitleBar(requireContext(), titleSettingsSubSectionOther, textSize = 20F)
        }
    }
}