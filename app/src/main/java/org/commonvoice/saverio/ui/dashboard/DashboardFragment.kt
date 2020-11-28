package org.commonvoice.saverio.ui.dashboard

import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.MessageDialog
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentDashboardBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class DashboardFragment : ViewBoundFragment<FragmentDashboardBinding>() {

    private val connectionManager: ConnectionManager by inject()

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDashboardBinding {
        return FragmentDashboardBinding.inflate(layoutInflater, container, false)
    }

    private val dashboardViewModel: DashboardViewModel by sharedViewModel()

    private val statsPrefManager: StatsPrefManager by inject()
    private val mainPrefManager: MainPrefManager by inject()

    private val tabTextColors by lazy {
        if (theme.isDark) {
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
        if (theme.isDark) {
            Pair(
                ContextCompat.getColorStateList(requireContext(), R.color.colorLightGray),
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorTabBackgroundInactiveDT
                )
            )
        } else {
            Pair(
                ContextCompat.getColorStateList(requireContext(), R.color.colorBlack),
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorTabBackgroundInactive
                )
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

        networkConnectivityCheck()

        withBinding {
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
                if (mainPrefManager.sessIdCookie != null) {
                    try {
                        val metrics = DisplayMetrics()
                        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
                        val width = metrics.widthPixels
                        val height = metrics.heightPixels
                        val message = MessageDialog(
                            requireContext(),
                            activity as MainActivity,
                            1,
                            value = statsPrefManager.dailyGoalObjective,
                            width = width,
                            height = height
                        )
                        message.show()
                    } catch (exception: Exception) {
                        println("!!-- Exception: MainActivity - OPEN DAILY GOAL DIALOG: " + exception.toString() + " --!!")
                    }
                } else {
                    showMessageDialog(
                        "",
                        getString(R.string.toastNoLoginNoDailyGoal)
                    )
                }
            }

            buttonRecordingsTopContributorsDashboard.onClick {
                dashboardViewModel.contributorsIsInSpeak.postValue(true)
                dashboardViewModel.updateStats()
            }

            buttonValidationsTopContributorsDashboard.onClick {
                dashboardViewModel.contributorsIsInSpeak.postValue(false)
                dashboardViewModel.updateStats()
            }
        }
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

    private fun networkConnectivityCheck() {
        connectionManager.liveInternetAvailability.observe(
            viewLifecycleOwner,
            { isInternetPresent ->
                withBinding {
                    dashboardSectionStatistics.children.filterIsInstance<ConstraintLayout>()
                        .forEach {
                            it.isVisible = isInternetPresent
                        }
                    dashboardSectionVoicesOnline.children.filterIsInstance<ConstraintLayout>()
                        .forEach {
                            it.isVisible = isInternetPresent
                        }
                    dashboardSectionAppStatistics.children.filterIsInstance<ConstraintLayout>()
                        .forEach {
                            it.isVisible = isInternetPresent
                        }
                    dashboardSectionTopContributors.children.filterIsInstance<ConstraintLayout>()
                        .forEach {
                            it.isVisible = isInternetPresent
                        }

                    dashboardTopContributorsNTh.isVisible = false
                    dashboardTopContributorsPoints.isVisible = false

                    labelDashboardDailyGoalValue.isVisible = isInternetPresent
                    buttonDashboardSetDailyGoal.isVisible = isInternetPresent

                    textDashboardNotAvailableOfflineStatistics.isGone = isInternetPresent
                    textDashboardNotAvailableOfflineVoicesOnline.isGone = isInternetPresent
                    textDashboardNotAvailableOfflineDailyGoal.isGone = isInternetPresent
                    textDashboardNotAvailableOfflineAppStatistics.isGone = isInternetPresent
                    textDashboardNotAvailableOfflineTopContributors.isGone = isInternetPresent
                }
                dashboardViewModel.updateStats()
            })
    }

    private fun loadDailyGoal() {
        if (statsPrefManager.dailyGoalObjective <= 0) {
            binding.labelDashboardDailyGoalValue.setText(R.string.daily_goal_is_not_set)
            binding.labelDashboardDailyGoalValue.typeface = Typeface.DEFAULT
        } else {
            binding.labelDashboardDailyGoalValue.text =
                statsPrefManager.dailyGoalObjective.toString()
            binding.labelDashboardDailyGoalValue.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.sourcecodepro)
        }
    }

    private fun loadEveryoneStats() {
        everyoneStats()

        isInUserStats = false

        tabTextColors.let { (selected, other) ->
            binding.buttonEveryoneStatisticsDashboard.setTextColor(selected)
            binding.buttonYouStatisticsDashboard.setTextColor(other)
        }
        tabBackgroundColors.let { (selected, other) ->
            binding.buttonEveryoneStatisticsDashboard.backgroundTintList = selected
            binding.buttonYouStatisticsDashboard.backgroundTintList = other
        }
    }

    private fun loadUserStats() {
        userStats()

        isInUserStats = true

        tabTextColors.let { (selected, other) ->
            binding.buttonYouStatisticsDashboard.setTextColor(selected)
            binding.buttonEveryoneStatisticsDashboard.setTextColor(other)
        }
        tabBackgroundColors.let { (selected, other) ->
            binding.buttonYouStatisticsDashboard.backgroundTintList = selected
            binding.buttonEveryoneStatisticsDashboard.backgroundTintList = other
        }
    }

    private fun voicesOnlineSection() {
        val localTimeNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
        val localTimeMinusOne = (localTimeNow.toIntOrNull()?.minus(1)?.takeUnless {
            it < 0
        } ?: 23).toString()

        binding.labelDashboardVoicesNow.text = "${getString(R.string.textHour)} $localTimeNow:00"
        binding.labelDashboardVoicesBefore.text =
            "${getString(R.string.textHour)} ${
                localTimeMinusOne.padStart(
                    2, '0'
                )
            }:00"

        dashboardViewModel.onlineVoices.observe(viewLifecycleOwner, Observer { list ->
            binding.textDashboardVoicesNow.setText(list.now.toString())
            binding.textDashboardVoicesBefore.setText(list.before.toString())
        })
    }

    fun setTheme() = withBinding {
        val context = requireContext()

        theme.setElements(context, layoutDashboard)

        theme.setElements(context, dashboardSectionStatistics)
        theme.setElements(context, dashboardSectionToday)
        theme.setElements(context, dashboardSectionEver)
        theme.setElements(context, dashboardSectionVoicesOnline)
        theme.setElements(context, dashboardSectionDailyGoal)
        theme.setElements(context, dashboardSectionAppStatistics)
        theme.setElements(context, dashboardSectionTopContributors)

        theme.setElements(context, dashboardVoicesOnlineNow)
        theme.setElements(context, dashboardVoicesOnlineBefore)

        theme.setElements(context, dashboardAppStatisticsCurrentLanguage)
        theme.setElements(context, dashboardAppStatisticsAllLanguages)

        theme.setElements(context, dashboardTopContributorsFirst)
        theme.setElements(context, dashboardTopContributorsSecond)
        theme.setElements(context, dashboardTopContributorsThird)
        theme.setElements(context, dashboardTopContributorsNTh)

        theme.setElement(context, 3, dashboardSectionStatistics)
        theme.setElement(context, 3, dashboardSectionVoicesOnline)
        theme.setElement(context, 3, dashboardSectionDailyGoal)
        theme.setElement(context, 3, dashboardSectionAppStatistics)
        theme.setElement(context, 3, dashboardSectionTopContributors)

        theme.setElement(
            context,
            3,
            dashboardSectionToday,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )
        theme.setElement(
            context,
            3,
            dashboardSectionEver,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )

        theme.setElement(
            context,
            3,
            dashboardVoicesOnlineNow,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )
        theme.setElement(
            context,
            3,
            dashboardVoicesOnlineBefore,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )

        theme.setElement(
            context,
            3,
            dashboardAppStatisticsCurrentLanguage,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )
        theme.setElement(
            context,
            3,
            dashboardAppStatisticsAllLanguages,
            R.color.colorTabBackgroundInactive,
            R.color.colorTabBackgroundInactiveDT
        )

        theme.setElement(context, buttonDashboardSetDailyGoal)

        theme.setTextView(context, textDashboardVoicesNow, border = false, intern = true)
        theme.setTextView(context, textDashboardVoicesBefore, border = false, intern = true)

        theme.setTextView(
            context,
            textDashboardAppStatisticsCurrentLanguage,
            border = false,
            intern = true
        )
        theme.setTextView(
            context,
            textDashboardAppStatisticsAllLanguages,
            border = false,
            intern = true
        )

        theme.setTextView(
            context,
            textDashboardTopContributorsNumberFirst,
            border = false,
            intern = true
        )
        theme.setTextView(
            context,
            textDashboardTopContributorsNumberSecond,
            border = false,
            intern = true
        )
        theme.setTextView(
            context,
            textDashboardTopContributorsNumberThird,
            border = false,
            intern = true
        )
        theme.setElement(context, labelTopContributorsPoints)
        theme.setTextView(
            context,
            textDashboardTopContributorsNumberNth,
            border = false,
            intern = true
        )

        resetTopContributor()
    }

    private fun resetTopContributor() {
        for (x in 0..3) {
            setYouTopContributor(
                getContributorSection(x),
                background = R.color.colorDarkWhite,
                backgroundDT = R.color.colorLightBlack
            )
        }
        setYouTopContributor(
            binding.dashboardTopContributorsNTh,
            background = R.color.colorDarkWhite,
            backgroundDT = R.color.colorLightBlack
        )
        binding.dashboardTopContributorsPoints.isGone = true
    }

    private fun setYouTopContributor(
        youTopContributors: ConstraintLayout,
        background: Int = R.color.colorYouTopContributors,
        backgroundDT: Int = R.color.colorYouTopContributorsDT
    ) {
        theme.setElement(
            requireContext(),
            3,
            youTopContributors,
            background = background,
            backgroundDT = backgroundDT
        )
    }

    private fun initLiveData() = withBinding {
        dashboardViewModel.stats.observe(viewLifecycleOwner, Observer {
            if (isInUserStats) {
                textTodaySpeak.text = "${it.userTodaySpeak}"
                textTodayListen.text = "${it.userTodayListen}"
                textEverSpeak.text = "${it.userEverSpeak}"
                textEverListen.text = "${it.userEverListen}"
            } else {
                textTodaySpeak.text = "${it.everyoneTodaySpeak}"
                textTodayListen.text = "${it.everyoneTodayListen}"
                textEverSpeak.text =
                    "${it.everyoneEverSpeak / 3600}${getString(R.string.textHoursAbbreviation)}"
                textEverListen.text =
                    "${it.everyoneEverListen / 3600}${getString(R.string.textHoursAbbreviation)}"
            }
        })

        dashboardViewModel.contributorsIsInSpeak.observe(viewLifecycleOwner, Observer {
            if (it) {
                tabTextColors.let { (selected, other) ->
                    buttonRecordingsTopContributorsDashboard.setTextColor(selected)
                    buttonValidationsTopContributorsDashboard.setTextColor(other)
                }
                tabBackgroundColors.let { (selected, other) ->
                    buttonRecordingsTopContributorsDashboard.backgroundTintList = selected
                    buttonValidationsTopContributorsDashboard.backgroundTintList = other
                }
            } else {
                tabTextColors.let { (selected, other) ->
                    buttonValidationsTopContributorsDashboard.setTextColor(selected)
                    buttonRecordingsTopContributorsDashboard.setTextColor(other)
                }
                tabBackgroundColors.let { (selected, other) ->
                    buttonValidationsTopContributorsDashboard.backgroundTintList = selected
                    buttonRecordingsTopContributorsDashboard.backgroundTintList = other
                }
            }
        })

        dashboardViewModel.contributors.observe(viewLifecycleOwner, Observer { pair ->
            dashboardTopContributorsNTh.isVisible = false

            resetTopContributor()

            if (pair.second) {
                pair.first.topContributorsSpeak.take(3)
                    .forEachIndexed { index, responseLeaderboardPosition ->
                        getContributorNameTextView(index).text =
                            responseLeaderboardPosition.username
                        getContributorNumberTextView(index).setText(responseLeaderboardPosition.total.toString())
                    }
                pair.first.topContributorsSpeak.find { it.isYou }?.let { you ->
                    if (pair.first.topContributorsSpeak.take(3).contains(you)) {
                        getContributorNameTextView(pair.first.topContributorsSpeak.indexOf(you))
                            .setText(R.string.dashboardTabYou)
                        setYouTopContributor(
                            getContributorSection(
                                pair.first.topContributorsSpeak.indexOf(
                                    you
                                )
                            )
                        )
                    } else {
                        dashboardTopContributorsNTh.isVisible =
                            connectionManager.isInternetAvailable

                        labelDashboardTopContributorsPositionNth.text = "${you.position + 1}"
                        textDashboardTopContributorsUsernameNth.setText(R.string.dashboardTabYou)
                        textDashboardTopContributorsNumberNth.setText("${you.total}")
                        setYouTopContributor(dashboardTopContributorsNTh)
                        if (you.total > 4) {
                            dashboardTopContributorsPoints.isGone =
                                !connectionManager.isInternetAvailable
                        }
                    }
                }
            } else {
                pair.first.topContributorsListen.take(3)
                    .forEachIndexed { index, responseLeaderboardPosition ->
                        getContributorNameTextView(index).text =
                            responseLeaderboardPosition.username
                        getContributorNumberTextView(index).setText(responseLeaderboardPosition.total.toString())
                    }
                pair.first.topContributorsListen.find { it.isYou }?.let { you ->
                    if (pair.first.topContributorsListen.take(3).contains(you)) {
                        getContributorNameTextView(pair.first.topContributorsListen.indexOf(you))
                            .setText(R.string.dashboardTabYou)
                        setYouTopContributor(
                            getContributorSection(
                                pair.first.topContributorsListen.indexOf(
                                    you
                                )
                            )
                        )
                    } else {
                        dashboardTopContributorsNTh.isVisible =
                            connectionManager.isInternetAvailable

                        labelDashboardTopContributorsPositionNth.text = "${you.position + 1}"
                        textDashboardTopContributorsUsernameNth.setText(R.string.dashboardTabYou)
                        textDashboardTopContributorsNumberNth.setText("${you.total}")
                        setYouTopContributor(dashboardTopContributorsNTh)
                        if (you.total > 4) {
                            dashboardTopContributorsPoints.isGone =
                                !connectionManager.isInternetAvailable
                        }
                    }
                }
            }
        })

        dashboardViewModel.usage.observe(viewLifecycleOwner, Observer {
            textDashboardAppStatisticsCurrentLanguage.setText("${it.languageUsage}")
            textDashboardAppStatisticsAllLanguages.setText("${it.totalUsage}")
        })
    }

    private fun getContributorNameTextView(index: Int) = when (index) {
        0 -> binding.textDashboardTopContributorsUsernameFirst
        1 -> binding.textDashboardTopContributorsUsernameSecond
        else -> binding.textDashboardTopContributorsUsernameThird
    }

    private fun getContributorNumberTextView(index: Int) = when (index) {
        0 -> binding.textDashboardTopContributorsNumberFirst
        1 -> binding.textDashboardTopContributorsNumberSecond
        else -> binding.textDashboardTopContributorsNumberThird
    }

    private fun getContributorSection(index: Int) = when (index) {
        0 -> binding.dashboardTopContributorsFirst
        1 -> binding.dashboardTopContributorsSecond
        else -> binding.dashboardTopContributorsThird
    }

    private fun everyoneStats() {
        withBinding {
            textTodaySpeak.text = "···"
            textTodayListen.text = "···"
            textEverSpeak.text = "···"
            textEverListen.text = "···"
        }

        dashboardViewModel.updateStats()
    }

    private fun userStats() {
        withBinding {
            textTodaySpeak.text = "···"
            textTodayListen.text = "···"
            textEverSpeak.text = "···"
            textEverListen.text = "···"
        }

        dashboardViewModel.updateStats()
    }

}