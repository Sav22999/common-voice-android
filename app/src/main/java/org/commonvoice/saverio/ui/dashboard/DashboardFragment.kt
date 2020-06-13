package org.commonvoice.saverio.ui.dashboard

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
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val dashboardViewModel: DashboardViewModel by sharedViewModel()

    private val statsPrefManager: StatsPrefManager by inject()
    private val mainPrefManager: MainPrefManager by inject()

    private val tabTextColors by lazy {
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

    private val tabBackgroundColors by lazy {
        val theme = DarkLightTheme()
        if (theme.getTheme(requireContext())) {
            Pair(
                ContextCompat.getColorStateList(requireContext(), R.color.colorLightGray),
                ContextCompat.getColorStateList(requireContext(), R.color.colorTabBackgroundInactiveDT)
            )
        } else {
            Pair(
                ContextCompat.getColorStateList(requireContext(), R.color.colorBlack),
                ContextCompat.getColorStateList(requireContext(), R.color.colorTabBackgroundInactive)
            )
        }
    }

    private var isInUserStats = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voicesOnlineSection()

        setTheme()

        loadDailyGoal()

        initLiveData()

        if (mainPrefManager.sessIdCookie != null) {
            loadUserStats()
        } else {
            buttonYouStatisticsDashboard.isVisible = false
            loadEveryoneStats()
        }

        buttonEveryoneStatisticsDashboard.onClick {
            if (isInUserStats) loadEveryoneStats()
        }

        buttonYouStatisticsDashboard.onClick {
            if (!isInUserStats) loadUserStats()
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

        isInUserStats = false

        tabTextColors.let { (selected, other) ->
            buttonEveryoneStatisticsDashboard.setTextColor(selected)
            buttonYouStatisticsDashboard.setTextColor(other)
        }
        tabBackgroundColors.let { (selected, other) ->
            buttonEveryoneStatisticsDashboard.backgroundTintList = selected
            buttonYouStatisticsDashboard.backgroundTintList = other
        }
    }

    private fun loadUserStats() {
        userStats()

        isInUserStats = true

        tabTextColors.let { (selected, other) ->
            buttonYouStatisticsDashboard.setTextColor(selected)
            buttonEveryoneStatisticsDashboard.setTextColor(other)
        }
        tabBackgroundColors.let { (selected, other) ->
            buttonYouStatisticsDashboard.backgroundTintList = selected
            buttonEveryoneStatisticsDashboard.backgroundTintList = other
        }
    }

    private fun voicesOnlineSection() {
        val localTimeNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
        val localTimeMinusOne = (localTimeNow.toIntOrNull()?.minus(1)?.takeUnless {
            it < 0
        } ?: 23).toString()

        labelDashboardVoicesNow.text = "${getString(R.string.textHour)} $localTimeNow:00"
        labelDashboardVoicesBefore.text =
            "${getString(R.string.textHour)} ${localTimeMinusOne.padStart(
                2 - localTimeMinusOne.length,
                '0'
            )}:00"

        dashboardViewModel.onlineVoices.observe(viewLifecycleOwner, Observer { list ->
            textDashboardVoicesNow.setText(list.now.toString())
            textDashboardVoicesBefore.setText(list.before.toString())
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
        theme.setElement(isDark, context, 3, dashboardSectionVoicesOnline)
        theme.setElement(isDark, context, 3, dashboardSectionDailyGoal)

        theme.setElement(
            isDark,
            context,
            3,
            dashboardSectionToday,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )
        theme.setElement(
            isDark,
            context,
            3,
            dashboardSectionEver,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )

        theme.setTextView(isDark, context, textDashboardVoicesNow)
        theme.setTextView(isDark, context, textDashboardVoicesBefore)
        theme.setElement(isDark, context, buttonDashboardSetDailyGoal)
    }

    private fun initLiveData() {
        dashboardViewModel.stats.observe(viewLifecycleOwner, Observer {
            if (isInUserStats) {
                textTodaySpeak.text = "${it.userTodaySpeak}"
                textTodayListen.text = "${it.userTodayListen}"
                textEverSpeak.text = "${it.userEverSpeak}"
                textEverListen.text = "${it.userEverListen}"
            } else {
                textTodaySpeak.text = "${it.everyoneTodaySpeak}"
                textTodayListen.text = "${it.everyoneTodayListen}"
                textEverSpeak.text = "${it.everyoneEverSpeak / 3600}${getString(R.string.textHoursAbbreviation)}"
                textEverListen.text = "${it.everyoneEverListen / 3600}${getString(R.string.textHoursAbbreviation)}"
            }
        })
    }

    private fun everyoneStats() {
        textTodaySpeak.text = "..."
        textTodayListen.text = "..."
        textEverSpeak.text = "..."
        textEverListen.text = "..."

        dashboardViewModel.updateStats()
    }

    private fun userStats() {
        textTodaySpeak.text = "..."
        textTodayListen.text = "..."
        textEverSpeak.text = "..."
        textEverListen.text = "..."

        dashboardViewModel.updateStats()
    }

}