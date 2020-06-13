package org.commonvoice.saverio.ui.dashboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val statsPrefManager: StatsPrefManager by inject()
    private val mainPrefManager: MainPrefManager by inject()

    private val textColors by lazy {
        val theme = DarkLightTheme()
        if (theme.getTheme(requireContext())) {
            Pair(
                ContextCompat.getColor(requireContext(), R.color.colorBlack),
                ContextCompat.getColor(requireContext(), R.color.colorWhite)
            )
        } else {
            Pair(
                ContextCompat.getColor(requireContext(), R.color.colorWhite),
                ContextCompat.getColor(requireContext(), R.color.colorBlack)
            )
        }
    }

    private val bgColors by lazy {
        val theme = DarkLightTheme()
        if (theme.getTheme(requireContext())) {
            Pair(
                ContextCompat.getColorStateList(requireContext(), R.color.colorWhiteTransparent),
                ContextCompat.getColorStateList(requireContext(), R.color.colorLightBlack)
            )
        } else {
            Pair(
                ContextCompat.getColorStateList(requireContext(), R.color.colorBlack),
                ContextCompat.getColorStateList(requireContext(), R.color.colorDarkWhite)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voicesOnlineSection()

        setTheme()

        loadDailyGoal()

        if (mainPrefManager.sessIdCookie != null) {
            loadUserStats()
        } else {
            buttonYouStatisticsDashboard.isVisible = false
            loadEveryoneStats()
        }

        buttonEveryoneStatisticsDashboard.onClick {
            loadEveryoneStats()
        }

        buttonYouStatisticsDashboard.onClick {
            loadUserStats()
        }

        buttonDashboardSetDailyGoal.onClick {
            //TODO absolutely change this
            if (mainPrefManager.sessIdCookie != null) {
                (activity as? MainActivity)?.openDailyGoalDialog()
            } else {
                (activity as? MainActivity)?.noLoggedInNoDailyGoal()
            }
        }
    }

    private fun loadDailyGoal() {
        if (statsPrefManager.dailyGoalObjective <= 0) {
            labelDashboardDailyGoalValue.setText(R.string.daily_goal_is_not_set)
        } else {
            labelDashboardDailyGoalValue.text = statsPrefManager.dailyGoalObjective.toString()
        }
    }

    private fun loadEveryoneStats() {
        everyoneStats()

        textColors.let { (selected, other) ->
            buttonEveryoneStatisticsDashboard.setTextColor(selected)
            buttonYouStatisticsDashboard.setTextColor(other)
        }
        bgColors.let { (selected, other) ->
            buttonEveryoneStatisticsDashboard.backgroundTintList = selected
            buttonYouStatisticsDashboard.backgroundTintList = other
        }
    }

    private fun loadUserStats() {
        userStats()

        textColors.let { (selected, other) ->
            buttonYouStatisticsDashboard.setTextColor(selected)
            buttonEveryoneStatisticsDashboard.setTextColor(other)
        }
        bgColors.let { (selected, other) ->
            buttonYouStatisticsDashboard.backgroundTintList = selected
            buttonEveryoneStatisticsDashboard.backgroundTintList = other
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun voicesOnlineSection() {
        val localTimeNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
        val localTimeMinusOne = (localTimeNow.toIntOrNull()?.minus(1)?.takeUnless {
            it < 0
        } ?: 23).toString()

        labelDashboardVoicesNow.text = "${getString(R.string.textHour)} $localTimeNow:00"
        labelDashboardVoicesBefore.text = "${getString(R.string.textHour)} ${localTimeMinusOne.padStart(2 - localTimeMinusOne.length, '0')}:00"

        dashboardViewModel.getHourlyVoices().observe(viewLifecycleOwner, Observer { list ->
            if (list.size == 2) {
                textDashboardVoicesNow.setText(list.last().count.toString())
                textDashboardVoicesBefore.setText(list.first().count.toString())
            }
        })
    }

    fun setTheme() {
        val context = requireContext()

        val theme = DarkLightTheme()
        val isDark = theme.getTheme(context)
        theme.setElements(context, layoutDashboard)

        theme.setElements(context, dashboardSectionStatistics)
        theme.setElements(context, dashboardSectionToday)
        theme.setElements(context, dashboardSectionEver)
        theme.setElements(context, dashboardSectionVoicesOnline)
        theme.setElements(context, dashboardSectionDailyGoal)

        theme.setElement(isDark, context, 3, dashboardSectionStatistics)
        theme.setElement(
            isDark,
            context,
            3,
            dashboardSectionToday,
            R.color.colorWhiteTransparent,
            R.color.colorLightBlack
        )
        theme.setElement(
            isDark,
            context,
            3,
            dashboardSectionEver,
            R.color.colorWhiteTransparent,
            R.color.colorLightBlack
        )
        theme.setElement(isDark, context, 3, dashboardSectionVoicesOnline)
        theme.setElement(isDark, context, 3, dashboardSectionDailyGoal)

        theme.setTextView(isDark, context, textDashboardVoicesNow)
        theme.setTextView(isDark, context, textDashboardVoicesBefore)
        theme.setElement(isDark, context, buttonDashboardSetDailyGoal)
    }

    private fun everyoneStats() {
        textTodaySpeak.text = "..."
        textTodayListen.text = "...."
        textEverSpeak.text = "..."
        textEverListen.text = "..."

        dashboardViewModel.getDailyClipsCount().observe(viewLifecycleOwner, Observer {
            textTodaySpeak.text = "$it"
        })

        dashboardViewModel.getDailyVotesCount().observe(viewLifecycleOwner, Observer {
            textTodayListen.text = "$it"
        })

        dashboardViewModel.getEverCount().observe(viewLifecycleOwner, Observer {
            textEverSpeak.text = "${it.total/3600}${getString(R.string.textHoursAbbreviation)}"
            textEverListen.text = "${it.valid/3600}${getString(R.string.textHoursAbbreviation)}"
        })
    }

    private fun userStats() {
        textTodaySpeak.text = "${statsPrefManager.todayRecorded}"
        textTodayListen.text = "${statsPrefManager.todayValidated}"

        textEverSpeak.text = "..."
        textEverListen.text = "..."

        dashboardViewModel.getUserClient().observe(viewLifecycleOwner, Observer { cl ->
            textEverSpeak.text = "${cl.clips_count}"
            textEverListen.text = "${cl.votes_count}"
        })
    }

}