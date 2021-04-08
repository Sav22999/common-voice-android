package org.commonvoice.saverio

import android.animation.ValueAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonvoice.saverio.databinding.ActivityListenBinding
import org.commonvoice.saverio.ui.dialogs.DialogInflater
import org.commonvoice.saverio.ui.dialogs.ListenReportDialogFragment
import org.commonvoice.saverio.ui.dialogs.NoClipsSentencesAvailableDialog
import org.commonvoice.saverio.ui.dialogs.commonTypes.StandardDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.DailyGoalAchievedDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.OfflineModeDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.SpeakListenStandardDialog
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_ads.AdLoader
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.dataClasses.BadgeDialogMediator
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import java.util.*


class ListenActivity : ViewBoundActivity<ActivityListenBinding>(
    ActivityListenBinding::inflate
) {

    private val listenViewModel: ListenViewModel by stateViewModel()
    private val connectionManager: ConnectionManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()
    private val listenPrefManager: ListenPrefManager by inject()
    private val dialogInflater by inject<DialogInflater>()

    private var isListenAnimateButtonVisible: Boolean = false
    private var animationsCount: Int = 0

    private var refreshAdsAfterListen = 20

    private var numberSentThisSession: Int = 0
    private var verticalScrollStatus: Int = 2 //0 top, 1 middle, 2 end
    private val settingsPrefManager by inject<SettingsPrefManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        setupUI()
    }

    private fun checkOfflineMode(available: Boolean) {
        if (!listenViewModel.showingHidingOfflineIcon && (listenViewModel.offlineModeIconVisible == available)) {
            listenViewModel.showingHidingOfflineIcon = true
            if (!available && settingsPrefManager.isOfflineMode) {
                startAnimation(binding.imageOfflineModeListen, R.anim.zoom_in)
                listenViewModel.offlineModeIconVisible = true
                if (mainPrefManager.showOfflineModeMessage) {
                    dialogInflater.show(this, OfflineModeDialog(mainPrefManager))
                }
            } else if (!settingsPrefManager.isOfflineMode) {
                dialogInflater.show(
                    this,
                    SpeakListenStandardDialog(messageRes = R.string.offline_mode_is_not_enabled) {
                        onBackPressed()
                    })
            } else {
                startAnimation(binding.imageOfflineModeListen, R.anim.zoom_out_speak_listen)
                listenViewModel.offlineModeIconVisible = false
            }
            listenViewModel.showingHidingOfflineIcon = false
            binding.imageOfflineModeListen.isGone = available
        }
    }

    private fun setupInitialUIState() = withBinding {
        buttonSkipListen.onClick {
            listenViewModel.skipClip()
        }

        buttonYesClip.isGone = true
        buttonNoClip.isGone = true
    }

    private fun setupUI() {
        binding.imageOfflineModeListen.onClick {
            lifecycleScope.launch {
                val count = listenViewModel.getClipsCount()
                withContext(Dispatchers.Main) {
                    NoClipsSentencesAvailableDialog(this@ListenActivity, false, count, theme).show()
                }
            }
        }

        connectionManager.liveInternetAvailability.observe(this, Observer { available ->
            checkOfflineMode(available)
        })

        listenViewModel.hasFinishedClips.observe(this, Observer {
            if (it && !connectionManager.isInternetAvailable) {
                NoClipsSentencesAvailableDialog(this, false, 0, theme).show {
                    onBackPressed()
                }
            }
        })

        listenViewModel.currentClip.observe(this, Observer { clip ->
            loadUIStateStandby(clip)
        })

        listenViewModel.state.observe(this, Observer { state ->
            when (state) {
                ListenViewModel.Companion.State.STANDBY -> {
                    loadUIStateLoading()
                    listenViewModel.loadNewClip()
                }
                ListenViewModel.Companion.State.NO_MORE_CLIPS -> {
                    loadUIStateNoMoreClips()
                    //listenViewModel.loadNewClip()
                }
                ListenViewModel.Companion.State.LISTENING -> {
                    loadUIStateListening()
                    isListenAnimateButtonVisible = true
                    animateListenAnimateButtons()
                }
                ListenViewModel.Companion.State.LISTENED -> {
                    loadUIStateListened()
                }
                ListenViewModel.Companion.State.ERROR -> {
                    //TODO
                    loadUIStateListening()
                }
            }
        })

        if (mainPrefManager.areGesturesEnabled) {
            setupGestures()
        }

        statsPrefManager.dailyGoal.observe(this, Observer {
            if ((numberSentThisSession > 0) && it.checkDailyGoal()) {
                stopAndRefresh()
                dialogInflater.show(this, DailyGoalAchievedDialog(this, it))
            }

            animateProgressBar(
                binding.progressBarListenSpeak,
                sum = it.recordings + it.validations,
                dailyGoal = it.getDailyGoal(),
                currentContributions = it.recordings,
                color = R.color.colorSpeak
            )
            animateProgressBar(
                binding.progressBarListenListen,
                sum = it.recordings + it.validations,
                dailyGoal = it.getDailyGoal(),
                currentContributions = it.validations,
                color = R.color.colorListen
            )
        })

        checkOfflineMode(connectionManager.isInternetAvailable)

        setupNestedScroll()

        setupBadgeDialog()

        setTheme()

        if (listenPrefManager.showAdBanner) {
            AdLoader.setupListenAdView(this, binding.adContainer)
        }
    }

    private fun refreshAds() {
        if (listenPrefManager.showAdBanner) {
            if (numberSentThisSession == 20) {
                refreshAdsAfterListen = 10
            } else if (numberSentThisSession >= 50) {
                refreshAdsAfterListen = 5
            }
            AdLoader.setupListenAdView(this, binding.adContainer)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        animateProgressBar(
            binding.progressBarListenSpeak,
            sum = statsPrefManager.dailyGoal.value!!.recordings + statsPrefManager.dailyGoal.value!!.validations,
            dailyGoal = statsPrefManager.dailyGoal.value!!.goal,
            currentContributions = statsPrefManager.dailyGoal.value!!.recordings,
            color = R.color.colorSpeak
        )
        animateProgressBar(
            binding.progressBarListenListen,
            sum = statsPrefManager.dailyGoal.value!!.recordings + statsPrefManager.dailyGoal.value!!.validations,
            dailyGoal = statsPrefManager.dailyGoal.value!!.goal,
            currentContributions = statsPrefManager.dailyGoal.value!!.validations,
            color = R.color.colorListen
        )

        refreshAds()
    }

    private fun setupNestedScroll() {
        binding.nestedScrollListen.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { nestedScrollView, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                verticalScrollStatus = 1
            }
            if (scrollY < oldScrollY) {
                verticalScrollStatus = 1
            }
            if (scrollY == 0) {
                verticalScrollStatus = 0
            }
            if (nestedScrollView.measuredHeight == (nestedScrollView.getChildAt(0).measuredHeight - scrollY)) {
                verticalScrollStatus = 2
            }
        })
    }

    private fun setupGestures() {
        binding.nestedScrollListen.setOnTouchListener(object :
            OnSwipeTouchListener(this@ListenActivity) {
            override fun onSwipeLeft() {
                listenViewModel.skipClip()
            }

            override fun onSwipeRight() {
                onBackPressed()
            }

            override fun onSwipeTop() {
                if (verticalScrollStatus == 2) {
                    openReportDialog()
                }
            }
        })
    }

    private fun animateProgressBar(
        progressBar: View,
        sum: Int = 0,
        dailyGoal: Int = 0,
        currentContributions: Int = 0,
        color: Int = R.color.colorBlack
    ) {
        val view: View = progressBar
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        //val height = metrics.heightPixels
        var newValue = 0

        if (dailyGoal == 0 || sum == 0) {
            newValue = width / 2
            setProgressBarColour(progressBar, forced = true)
        } else if (sum >= dailyGoal) {
            val tempContributions =
                (currentContributions.toFloat() * dailyGoal.toFloat()) / sum.toFloat()
            newValue =
                ((tempContributions.toFloat() / dailyGoal.toFloat()) * width).toInt()
            setProgressBarColour(progressBar, forced = false, color = color)
            progressBar.isGone = false
        } else if (currentContributions == 0) {
            progressBar.isGone = true
            progressBar.layoutParams.width = 1
            progressBar.requestLayout()
        } else {
            //currentRecordingsValidations : dailyGoal = X : 1 ==> currentRecordingsValidations / dailyGoal
            newValue =
                ((currentContributions.toFloat() / dailyGoal.toFloat()) * width).toInt()
            setProgressBarColour(progressBar, forced = false, color = color)
            progressBar.isGone = false
        }

        if (mainPrefManager.areAnimationsEnabled) {
            animationProgressBar(progressBar, view.width, newValue)
        } else {
            if (!view.isGone) {
                view.layoutParams.width = newValue
                view.requestLayout()
            }
        }
    }

    private fun setProgressBarColour(
        progressBar: View,
        forced: Boolean = false,
        color: Int = R.color.colorBlack
    ) {
        if (!settingsPrefManager.isProgressBarColouredEnabled || forced) {
            theme.setElement(
                this,
                progressBar,
                R.color.colorPrimaryDark,
                R.color.colorLightGray
            )
        } else {
            //coloured
            theme.setElement(
                this,
                progressBar,
                color,
                color
            )
        }
    }

    private fun animationProgressBar(progressBar: View, min: Int, max: Int) {
        val view: View = progressBar
        val animation: ValueAnimator =
            ValueAnimator.ofInt(min, max)
        animation.duration = 1000
        animation.addUpdateListener { anim ->
            val value = anim.animatedValue as Int
            view.layoutParams.width = value
            view.requestLayout()
        }
        animation.start()
    }

    private fun setTheme() = withBinding {
        theme.setElement(layoutListen)
        theme.setElement(this@ListenActivity, 1, listenSectionBottom)
        theme.setElement(
            this@ListenActivity,
            textMessageAlertListen,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT,
            textSize = 15F
        )
        theme.setElement(
            this@ListenActivity,
            textMotivationalSentencesListen,
            R.color.colorAdviceLightTheme,
            R.color.colorAdviceDarkTheme,
            textSize = 15F
        )
        theme.setElement(this@ListenActivity, buttonReportListen, background = false)
        theme.setElement(this@ListenActivity, buttonSkipListen)

        setProgressBarColour(progressBarListenSpeak, false)
        setProgressBarColour(progressBarListenListen, false)
    }

    private fun openReportDialog() {
        if (!binding.buttonReportListen.isGone || !binding.imageReportIconListen.isGone) {
            stopAndRefresh()

            ListenReportDialogFragment().show(supportFragmentManager, "LISTEN_REPORT")
        }
    }

    private fun stopAndRefresh() {
        listenViewModel.stop()
        listenViewModel.currentClip.observe(this, Observer { clip ->
            loadUIStateStandby(clip, noAutoPlay = true)
        })
        hideListenAnimateButtons()
    }

    private fun loadUIStateLoading() = withBinding {
        textSentenceListen.setTextColor(
            ContextCompat.getColor(
                this@ListenActivity,
                R.color.colorWhite
            )
        )

        if (!listenViewModel.stopped) {
            textSentenceListen.text = "···"
            resizeSentence()
            textMessageAlertListen.setText(R.string.txt_loading_sentence)
            buttonStartStopListen.isEnabled = false
            if (settingsPrefManager.showReportIcon) {
                hideImage(imageReportIconListen)
            } else {
                buttonReportListen.isGone = true
            }

            val motivationSentences = arrayOf(
                getString(R.string.text_continue_to_validate_1),
                getString(R.string.text_continue_to_validate_2),
                getString(R.string.text_continue_to_validate_3),
                getString(R.string.text_continue_to_validate_4)
            )
            if (numberSentThisSession == 5 || numberSentThisSession == 20 || numberSentThisSession == 40 || numberSentThisSession == 80 || numberSentThisSession == 120 || numberSentThisSession == 200 || numberSentThisSession == 300 || numberSentThisSession == 500) {
                if (statsPrefManager.dailyGoalObjective <= 0 || (statsPrefManager.dailyGoalObjective > 0 && statsPrefManager.dailyGoalObjective > (numberSentThisSession + 10))) {
                    textMotivationalSentencesListen.isGone = false
                    textMotivationalSentencesListen.text =
                        motivationSentences[(motivationSentences.indices).random()].replace(
                            "{{*{{number}}*}}",
                            numberSentThisSession.toString()
                        )
                }
            } else {
                textMotivationalSentencesListen.isGone = true
            }

            if (statsPrefManager.dailyGoalObjective > 5 && ((numberSentThisSession + 5) == statsPrefManager.dailyGoalObjective) && numberSentThisSession > 5) {
                //if the dailygoal is not set and the dailygoal is almost achieved
                textMotivationalSentencesListen.isGone = false
                textMotivationalSentencesListen.text =
                    getString(R.string.text_almost_achieved_dailygoal_listen).replace(
                        "{{*{{number}}*}}",
                        5.toString()
                    ).replace(
                        "{{*{{dailygoal}}*}}",
                        statsPrefManager.dailyGoalObjective.toString()
                    )
            }
        }
        //buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
        if (!listenViewModel.opened) {
            listenViewModel.opened = true
            startAnimation(buttonStartStopListen, R.anim.zoom_in_speak_listen)
            startAnimation(buttonSkipListen, R.anim.zoom_in_speak_listen)
        }
    }

    private fun loadUIStateNoMoreClips() = withBinding {
        textSentenceListen.setTextColor(
            ContextCompat.getColor(
                this@ListenActivity,
                R.color.colorWhite
            )
        )

        if (!listenViewModel.stopped) {
            textSentenceListen.text = "···"
            resizeSentence()
            textMessageAlertListen.setText(R.string.txt_common_voice_clips_finished)
            buttonStartStopListen.isEnabled = false
            if (settingsPrefManager.showReportIcon) {
                hideImage(imageReportIconListen)
            } else {
                buttonReportListen.isGone = true
            }
        }
        //buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
        if (!listenViewModel.opened) {
            listenViewModel.opened = true
            startAnimation(buttonStartStopListen, R.anim.zoom_in_speak_listen)
            startAnimation(buttonSkipListen, R.anim.zoom_in_speak_listen)
        }
    }

    private fun loadUIStateStandby(clip: Clip, noAutoPlay: Boolean = false) = withBinding {
        textSentenceListen.setTextColor(
            ContextCompat.getColor(
                this@ListenActivity,
                R.color.colorWhite
            )
        )

        if (listenViewModel.showSentencesTextAtTheEnd() && !listenViewModel.listenedOnce) {
            textMessageAlertListen.text = getString(R.string.txt_sentence_feature_enabled).replace(
                "{{*{{feature_name}}*}}",
                getString(R.string.txt_show_sentence_at_the_ending)
            ) + "\n" + getString(R.string.txt_press_icon_below_listen_1)

        } else textMessageAlertListen.setText(R.string.txt_clip_correct_or_wrong)

        if (!listenViewModel.showSentencesTextAtTheEnd() || listenViewModel.listenedOnce) {
            textSentenceListen.text = clip.sentence.sentenceText
            textSentenceListen.setTextColor(
                ContextCompat.getColor(
                    this@ListenActivity,
                    R.color.colorWhite
                )
            )
        } else {
            textSentenceListen.setText(R.string.txt_sentence_text_hidden)
            textSentenceListen.setTextColor(
                ContextCompat.getColor(
                    this@ListenActivity,
                    R.color.colorLightRed
                )
            )
        }

        hideListenAnimateButtons()

        resizeSentence()

        if (settingsPrefManager.showReportIcon) {
            showImage(imageReportIconListen)
        } else {
            buttonReportListen.isGone = false
        }

        buttonStartStopListen.isEnabled = true
        buttonStartStopListen.onClick {
            listenViewModel.startListening()

            if (!listenViewModel.startedOnce || !buttonNoClip.isVisible) {
                Handler().postDelayed({
                    if (listenViewModel.state.value == ListenViewModel.Companion.State.LISTENING) showButton(
                        buttonNoClip
                    )
                }, 900)
            }
        }

        if (listenViewModel.stopped) {
            //stopped recording
            buttonStartStopListen.setBackgroundResource(R.drawable.listen2_cv)
        } else {
            buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)

            hideButtons()

            listenViewModel.listenedOnce = false
            listenViewModel.startedOnce = false
        }

        if (!listenViewModel.startedOnce) {
            if (listenViewModel.autoPlay() && !noAutoPlay && !(!settingsPrefManager.isOfflineMode && !connectionManager.isInternetAvailable)) {
                listenViewModel.startListening()
            }
        }

        buttonReportListen.onClick {
            openReportDialog()
        }
        imageReportIconListen.onClick {
            openReportDialog()
        }
    }

    private fun resizeSentence() {
        binding.textSentenceListen.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            when (binding.textSentenceListen.text.length) {
                in 0..10 -> resources.getDimension(R.dimen.title_very_big) * mainPrefManager.textSize
                in 11..20 -> resources.getDimension(R.dimen.title_big) * mainPrefManager.textSize
                in 21..40 -> resources.getDimension(R.dimen.title_medium) * mainPrefManager.textSize
                in 41..70 -> resources.getDimension(R.dimen.title_normal) * mainPrefManager.textSize
                else -> resources.getDimension(R.dimen.title_small) * mainPrefManager.textSize
            }
        )
    }

    private fun loadUIStateListening() = withBinding {
        stopButtons()

        textSentenceListen.setTextColor(
            ContextCompat.getColor(
                this@ListenActivity,
                R.color.colorWhite
            )
        )

        if (listenViewModel.showSentencesTextAtTheEnd() && !listenViewModel.listenedOnce) {
            textMessageAlertListen.text = getString(R.string.txt_sentence_feature_enabled).replace(
                "{{*{{feature_name}}*}}",
                getString(R.string.txt_show_sentence_at_the_ending)
            ) + "\n" + getString(
                R.string.txt_press_icon_below_listen_2
            )
            textSentenceListen.setText(R.string.txt_listening_clip)
            resizeSentence()
            textSentenceListen.setTextColor(
                ContextCompat.getColor(
                    this@ListenActivity,
                    R.color.colorLightRed
                )
            )
        } else {
            textMessageAlertListen.setText(R.string.txt_press_icon_below_listen_2)
            textSentenceListen.text = listenViewModel.getSentenceText()
        }


        if (!listenViewModel.startedOnce) {
            Handler().postDelayed({
                if (listenViewModel.state.value == ListenViewModel.Companion.State.LISTENING) showButton(
                    buttonNoClip
                )
            }, 900)
        }

        if (!listenViewModel.listenedOnce) buttonYesClip.isVisible = false
        listenViewModel.startedOnce = true
        buttonSkipListen.isEnabled = true

        buttonStartStopListen.setBackgroundResource(R.drawable.stop_cv)

        buttonNoClip.onClick {
            listenViewModel.validate(result = false)
            numberSentThisSession++
            hideButtons()
            if (numberSentThisSession % refreshAdsAfterListen == 0) {
                refreshAds()
            }
        }
        buttonStartStopListen.onClick {
            listenViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() = withBinding {
        buttonNoClip.isVisible = true
        textSentenceListen.text = listenViewModel.getSentenceText()
        resizeSentence()
        hideListenAnimateButtons()

        textSentenceListen.setTextColor(
            ContextCompat.getColor(
                this@ListenActivity,
                R.color.colorWhite
            )
        )
        if (!listenViewModel.listenedOnce) {
            showButton(buttonYesClip)
        }
        listenViewModel.listenedOnce = true

        textMessageAlertListen.setText(R.string.txt_clip_correct_or_wrong)

        buttonStartStopListen.setBackgroundResource(R.drawable.listen2_cv)

        buttonYesClip.onClick {
            hideButtons()
            listenViewModel.validate(result = true)
            numberSentThisSession++
            if (numberSentThisSession % refreshAdsAfterListen == 0) {
                refreshAds()
            }
        }
        buttonStartStopListen.onClick {
            listenViewModel.startListening()

            if (!listenViewModel.startedOnce || !buttonNoClip.isVisible) {
                Handler().postDelayed({
                    if (listenViewModel.state.value == ListenViewModel.Companion.State.LISTENING) showButton(
                        buttonNoClip
                    )
                }, 900)
            }
        }
    }

    override fun onBackPressed() = withBinding {
        textMessageAlertListen.setText(R.string.txt_closing)
        buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
        textSentenceListen.text = "···"
        resizeSentence()
        textSentenceListen.setTextColor(
            ContextCompat.getColor(
                this@ListenActivity,
                R.color.colorWhite
            )
        )
        if (settingsPrefManager.showReportIcon) {
            hideImage(imageReportIconListen)
        } else {
            buttonReportListen.isGone = true
        }
        buttonStartStopListen.isEnabled = false
        buttonSkipListen.isEnabled = false
        buttonYesClip.isGone = true
        buttonNoClip.isGone = true

        listenViewModel.stop()

        hideListenAnimateButtons()

        super.onBackPressed()
    }

    override fun onPause() {
        AdLoader.cleanupLayout(binding.adContainer)

        super.onPause()
    }

    private fun setupBadgeDialog(): Any = if (mainPrefManager.isLoggedIn) {
        lifecycleScope.launch {
            statsPrefManager.badgeLiveData.collect {
                if (it is BadgeDialogMediator.Listen || it is BadgeDialogMediator.Level) {
                    dialogInflater.show(
                        this@ListenActivity, StandardDialog(
                            message = getString(R.string.new_badge_earnt_message)
                                .replace(
                                    "{{*{{profile}}*}}",
                                    getString(R.string.button_home_profile)
                                )
                                .replace(
                                    "{{*{{all_badges}}*}}",
                                    getString(R.string.btn_badges_loggedin)
                                )
                        )
                    )
                }
            }
        }
    } else Unit

    private fun hideButtons() {
        stopButtons()
        if (listenViewModel.startedOnce) hideButton(binding.buttonNoClip)
        if (listenViewModel.listenedOnce) hideButton(binding.buttonYesClip)
        hideListenAnimateButtons()
    }

    private fun showButton(button: Button) {
        if (!button.isVisible) {
            button.isVisible = true
            button.isEnabled = true
            startAnimation(
                button,
                R.anim.zoom_in_speak_listen
            )
        }
    }

    private fun hideButton(button: Button) {
        button.isEnabled = false
        startAnimation(
            button,
            R.anim.zoom_out_speak_listen
        )
        button.isVisible = false
    }

    private fun stopButtons() {
        stopAnimation(binding.buttonNoClip)
        stopAnimation(binding.buttonYesClip)
    }

    private fun showImage(image: ImageView) {
        if (!image.isVisible) {
            image.isVisible = true
            image.isEnabled = true
            startAnimation(
                image,
                R.anim.zoom_in_speak_listen
            )
        }
    }

    private fun hideImage(image: ImageView, stop: Boolean = true) {
        if (stop) stopImage(image)
        image.isEnabled = false
        startAnimation(
            image,
            R.anim.zoom_out_speak_listen
        )
        image.isVisible = false
    }

    private fun stopImage(image: ImageView) {
        stopAnimation(image)
    }


    private fun animateListenAnimateButtons() {
        if (mainPrefManager.areAnimationsEnabled) {
            this.animationsCount++
            animateListenAnimateButton(
                binding.viewListenAnimateButton1,
                280,
                340,
                this.animationsCount
            )
            animateListenAnimateButton(
                binding.viewListenAnimateButton2,
                350,
                400,
                this.animationsCount
            )
        }
    }

    private fun hideListenAnimateButtons() = withBinding {
        if (mainPrefManager.areAnimationsEnabled) {
            if (viewListenAnimateButton1.isVisible && viewListenAnimateButton2.isVisible && isListenAnimateButtonVisible) {
                isListenAnimateButtonVisible = false
                animateListenAnimateButton(
                    viewListenAnimateButton1,
                    viewListenAnimateButton1.height,
                    200,
                    animationsCount
                )
                animateListenAnimateButton(
                    viewListenAnimateButton2,
                    viewListenAnimateButton2.height,
                    200,
                    animationsCount
                )
            }
        }
    }

    private fun animateListenAnimateButton(
        view: View,
        min: Int,
        max: Int,
        animationsCountTemp: Int
    ) {
        if (listenViewModel.state.value == ListenViewModel.Companion.State.LISTENING && this.isListenAnimateButtonVisible) {
            animationListenAnimateButton(view, view.height, min, max, animationsCountTemp)
            view.isVisible = true
        } else if (!this.isListenAnimateButtonVisible && view.height >= 280) {
            animationListenAnimateButton(
                view,
                view.height,
                view.height,
                200,
                animationsCountTemp,
                forced = true
            )
            view.isVisible = true
        } else {
            view.isVisible = false
        }
    }

    private fun animationListenAnimateButton(
        view: View,
        sizeNow: Int,
        min: Int,
        max: Int,
        animationsCountTemp: Int,
        forced: Boolean = false
    ) {
        val animation: ValueAnimator =
            ValueAnimator.ofInt(sizeNow, max)

        if (max == 50 || max == 200) animation.duration = 300
        else animation.duration = (800..1200).random().toLong()
        animation.addUpdateListener { anim ->
            val value = anim.animatedValue as Int
            view.layoutParams.height = value
            view.layoutParams.width = value
            view.requestLayout()
        }
        animation.doOnEnd {
            if (!this.isListenAnimateButtonVisible && forced) {
                view.isVisible = false
            }
            if (this.isListenAnimateButtonVisible && view.isVisible && !forced && this.animationsCount == animationsCountTemp) {
                animateListenAnimateButton(view, max, min, animationsCountTemp)
            }
        }
        animation.start()
    }
}