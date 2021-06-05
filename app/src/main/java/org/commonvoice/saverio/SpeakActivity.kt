package org.commonvoice.saverio

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonvoice.saverio.databinding.ActivitySpeakBinding
import org.commonvoice.saverio.ui.dialogs.DialogInflater
import org.commonvoice.saverio.ui.dialogs.NoClipsSentencesAvailableDialog
import org.commonvoice.saverio.ui.dialogs.SpeakReportDialogFragment
import org.commonvoice.saverio.ui.dialogs.commonTypes.StandardDialog
import org.commonvoice.saverio.ui.dialogs.commonTypes.WarningDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.DailyGoalAchievedDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.IdentifyMeDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.OfflineModeDialog
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_ads.AdLoader
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.dataClasses.BadgeDialogMediator
import org.commonvoice.saverio_lib.dataClasses.DailyGoal
import org.commonvoice.saverio_lib.models.Sentence
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import java.util.*

class SpeakActivity : ViewBoundActivity<ActivitySpeakBinding>(
    ActivitySpeakBinding::inflate
) {

    companion object {
        private const val RECORD_REQUEST_CODE = 101
    }

    private val speakViewModel: SpeakViewModel by stateViewModel()

    private val connectionManager: ConnectionManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()
    private val dialogInflater by inject<DialogInflater>()

    private var numberSentThisSession: Int = 0
    private var verticalScrollStatus: Int = 2 //0 top, 1 middle, 2 end

    private var isAudioBarVisible: Boolean = false
    private var animationsCount: Int = 0

    private var refreshAdsAfterSpeak = 10
    private var recorded = false

    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()

    private var messageInfoToShow = ""

    var minHeight = 30
    var maxHeight = 350

    private var scrollingStatus = 0
    private var scrollingToBefore = ""
    private var longPressEnabled = false
    private var enableGestureAt = 50

    private var dailyGoalAchievedAndNotShown = false
    private lateinit var dailyGoalAchievedAndNotShownIt: DailyGoal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        setupUI()

        minHeight = binding.imageAudioBar1.layoutParams.height
        maxHeight = binding.speakSectionAudioBar.layoutParams.height
    }

    private fun checkOfflineMode(available: Boolean) {
        if (!speakViewModel.showingHidingOfflineIcon && (speakViewModel.offlineModeIconVisible == available)) {
            speakViewModel.showingHidingOfflineIcon = true
            if (!available && settingsPrefManager.isOfflineMode) {
                startAnimation(binding.imageOfflineModeSpeak, R.anim.zoom_in_speak_listen)
                speakViewModel.offlineModeIconVisible = true
                if (mainPrefManager.showOfflineModeMessage) {
                    dialogInflater.show(this, OfflineModeDialog(mainPrefManager))
                }
            } else if (!settingsPrefManager.isOfflineMode) {
                NoClipsSentencesAvailableDialog(
                    this,
                    isSentencesDialog = true,
                    isOfflineModeDisabledDialog = true,
                    0,
                    theme
                ).show {
                    onBackPressed()
                }
            } else {
                startAnimation(binding.imageOfflineModeSpeak, R.anim.zoom_out_speak_listen)
                speakViewModel.offlineModeIconVisible = false
            }
            speakViewModel.showingHidingOfflineIcon = false
            binding.imageOfflineModeSpeak.isGone = available
        }
    }

    override fun onBackPressed() = withBinding {
        if (!recorded) {
            onBackPressedCustom()
            super.onBackPressed()
        } else {
            dialogInflater.show(this@SpeakActivity,
                StandardDialog(
                    messageRes = R.string.text_are_you_sure_go_back_and_lose_the_recording,
                    buttonTextRes = R.string.button_yes_sure,
                    onButtonClick = {
                        this@SpeakActivity.recorded = false

                        this@SpeakActivity.onBackPressedCustom()
                        super.onBackPressed()
                    },
                    button2TextRes = R.string.button_cancel,
                    onButton2Click = {}
                ))
        }
    }

    private fun onBackPressedCustom() = withBinding {
        textMessageAlertSpeak.setText(R.string.txt_closing)
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)
        textSentenceSpeak.text = "···"
        textSentenceSpeak.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.title_very_big)
        )
        buttonRecordOrListenAgain.isGone = true
        if (settingsPrefManager.showReportIcon && !imageReportIconSpeak.isGone) {
            hideImage(imageReportIconSpeak)
        } else {
            buttonReportSpeak.isGone = true
        }
        if (settingsPrefManager.showInfoIcon && !imageInfoSpeak.isGone) {
            hideImage(imageInfoSpeak)
        }
        buttonSkipSpeak.isEnabled = false
        buttonStartStopSpeak.isEnabled = false
        buttonSendSpeak.isGone = true
        speakSectionAudioBar.isGone = true

        speakViewModel.stop(true)

        hideAudioBar()
    }

    private fun setupUI() {
        binding.imageOfflineModeSpeak.onClick {
            lifecycleScope.launch {
                val count = speakViewModel.getSentencesCount()
                withContext(Dispatchers.Main) {
                    NoClipsSentencesAvailableDialog(
                        this@SpeakActivity,
                        isSentencesDialog = true,
                        isOfflineModeDisabledDialog = false,
                        count,
                        theme
                    ).show()
                }
            }
        }

        connectionManager.liveInternetAvailability.observe(this, { available ->
            checkOfflineMode(available)
        })

        speakViewModel.hasFinishedSentences.observe(this, {
            if (it && !connectionManager.isInternetAvailable) {
                NoClipsSentencesAvailableDialog(
                    this,
                    isSentencesDialog = true,
                    isOfflineModeDisabledDialog = false,
                    0,
                    theme
                ).show {
                    onBackPressed()
                }
            }
        })

        speakViewModel.currentSentence.observe(this, { sentence ->
            setupUIStateStandby(sentence)
        })

        if (mainPrefManager.areGesturesEnabled) {
            setupGestures()
        }

        speakViewModel.state.observe(this, {
            checkState(it)
        })

        statsPrefManager.dailyGoal.observe(this, {
            if ((numberSentThisSession > 0) && it.checkDailyGoal()) {
                //achieved
                setDailyGoalAchievedAndNotShown(it)
                if (speakViewModel.state.value == SpeakViewModel.Companion.State.STANDBY) showDailyGoalAchievedMessage()
            }

            animateProgressBar(
                binding.progressBarSpeakSpeak,
                sum = it.recordings + it.validations,
                dailyGoal = it.getDailyGoal(),
                currentContributions = it.recordings,
                color = R.color.colorSpeak
            )
            animateProgressBar(
                binding.progressBarSpeakListen,
                sum = it.recordings + it.validations,
                dailyGoal = it.getDailyGoal(),
                currentContributions = it.validations,
                color = R.color.colorListen
            )

            if (it.recordings == 0 && it.validations > 0 && it.getDailyGoal() > 0) {
                binding.progressBarSpeakSpeak.isGone = true
            }
            if (it.validations == 0 && it.recordings > 0 && it.getDailyGoal() > 0) {
                binding.progressBarSpeakListen.isGone = true
            }
        })

        if (speakPrefManager.showSpeedControl) {
            binding.speakSectionSpeedButtons.isGone = false

            setSpeedControlButtons(speakPrefManager.audioSpeed, setup = true)
            binding.buttonSpeed10Speak.setOnClickListener {
                setSpeedControlButtons(1F)
            }
            binding.buttonSpeed15Speak.setOnClickListener {
                setSpeedControlButtons(1.5F)
            }
            binding.buttonSpeed20Speak.setOnClickListener {
                setSpeedControlButtons(2F)
            }
        }

        checkPermission()

        setupNestedScroll()

        setTheme(this)

        setupBadgeDialog()

        if (speakPrefManager.showAdBanner) {
            AdLoader.setupSpeakAdView(this, binding.adContainer)
        }
    }

    private fun setDailyGoalAchievedAndNotShown(dailyGoal: DailyGoal) {
        dailyGoalAchievedAndNotShown = true
        dailyGoalAchievedAndNotShownIt = dailyGoal
    }

    private fun showDailyGoalAchievedMessage() {
        if (dailyGoalAchievedAndNotShownIt != null) {
            dailyGoalAchievedAndNotShown = false
            stopAndRefresh()
            dialogInflater.show(this, DailyGoalAchievedDialog(this, dailyGoalAchievedAndNotShownIt))
        }
    }

    private fun setSpeedControlButtons(speed: Float, setup: Boolean = false) {
        speakPrefManager.audioSpeed = speed
        val buttons = mapOf(
            1F to binding.buttonSpeed10Speak,
            1.5F to binding.buttonSpeed15Speak,
            2F to binding.buttonSpeed20Speak
        )

        for ((key, button) in buttons) {
            theme.setElement(
                this,
                button,
                R.color.colorSpeedButtonText,
                R.color.colorSpeedButtonText,
                R.color.colorSpeedButtonBackground,
                R.color.colorSpeedButtonBackground,
                12F,
                scale = false
            )
        }
        theme.setElement(
            this,
            buttons[speed],
            R.color.colorSpeedButtonTextSelected,
            R.color.colorSpeedButtonTextSelected,
            R.color.colorSpeedButtonBackgroundSelected,
            R.color.colorSpeedButtonBackgroundSelected,
            12F,
            scale = false
        )

        if (!setup) {
            Toast.makeText(
                this,
                getString(R.string.toast_speed_set_successfully).replace(
                    "{{speed_value}}",
                    speed.toString()
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun refreshAds() {
        if (speakPrefManager.showAdBanner) {
            if (numberSentThisSession == 20) {
                refreshAdsAfterSpeak = 5
            } else if (numberSentThisSession >= 40) {
                refreshAdsAfterSpeak = 2
            }
            AdLoader.setupSpeakAdView(this, binding.adContainer)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        animateProgressBar(
            binding.progressBarSpeakSpeak,
            sum = statsPrefManager.dailyGoal.value!!.recordings + statsPrefManager.dailyGoal.value!!.validations,
            dailyGoal = statsPrefManager.dailyGoal.value!!.goal,
            currentContributions = statsPrefManager.dailyGoal.value!!.recordings,
            color = R.color.colorSpeak
        )
        animateProgressBar(
            binding.progressBarSpeakListen,
            sum = statsPrefManager.dailyGoal.value!!.recordings + statsPrefManager.dailyGoal.value!!.validations,
            dailyGoal = statsPrefManager.dailyGoal.value!!.goal,
            currentContributions = statsPrefManager.dailyGoal.value!!.validations,
            color = R.color.colorListen
        )

        if (statsPrefManager.dailyGoal.value!!.recordings == 0 && statsPrefManager.dailyGoal.value!!.validations > 0 && statsPrefManager.dailyGoal.value!!.goal > 0) {
            binding.progressBarSpeakSpeak.isGone = true
        }
        if (statsPrefManager.dailyGoal.value!!.validations == 0 && statsPrefManager.dailyGoal.value!!.recordings > 0 && statsPrefManager.dailyGoal.value!!.goal > 0) {
            binding.progressBarSpeakListen.isGone = true
        }

        refreshAds()
        resizeSentence()
    }

    override fun onPause() {
        AdLoader.cleanupLayout(binding.adContainer)

        super.onPause()
    }

    private fun checkState(status: SpeakViewModel.Companion.State?) {
        when (status) {
            SpeakViewModel.Companion.State.STANDBY -> {
                loadUIStateLoading()
                speakViewModel.loadNewSentence()
            }
            SpeakViewModel.Companion.State.NO_MORE_SENTENCES -> {
                loadUIStateNoMoreSentences()
                //speakViewModel.loadNewSentence()
            }
            SpeakViewModel.Companion.State.RECORDING -> {
                loadUIStateRecording()
                isAudioBarVisible = true
                animateAudioBar()
            }
            SpeakViewModel.Companion.State.RECORDED -> {
                loadUIStateRecorded()
            }
            SpeakViewModel.Companion.State.LISTENING -> {
                loadUIStateListening()
            }
            SpeakViewModel.Companion.State.LISTENED -> {
                loadUIStateListened()
            }
            SpeakViewModel.Companion.State.RECORDING_ERROR -> {
                this.stopAndRefresh()
                dialogInflater.show(
                    this,
                    WarningDialog(messageRes = R.string.messageDialogGenericError)
                )
                speakViewModel.currentSentence.value?.let { sentence ->
                    setupUIStateStandby(sentence)
                }
                this@SpeakActivity.recorded = false
            }
            SpeakViewModel.Companion.State.RECORDING_TOO_SHORT -> {
                dialogInflater.show(
                    this,
                    WarningDialog(messageRes = R.string.txt_recording_too_short)
                )
                speakViewModel.currentSentence.value?.let { sentence ->
                    setupUIStateStandby(sentence)
                }
                this@SpeakActivity.recorded = false
            }
            SpeakViewModel.Companion.State.RECORDING_TOO_LONG -> {
                dialogInflater.show(
                    this,
                    WarningDialog(messageRes = R.string.txt_recording_too_long)
                )
                speakViewModel.currentSentence.value?.let { sentence ->
                    setupUIStateStandby(sentence)
                }
                this@SpeakActivity.recorded = false
            }
        }
    }

    private fun setupNestedScroll() {
        binding.nestedScrollSpeak.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { nestedScrollView, _, scrollY, _, oldScrollY ->
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


    /*
    GESTURES
    */

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val valueToUse = if (width < height) {
            width
        } else {
            height
        }

        //set the width/height when gestures have to be enabled
        enableGestureAt = valueToUse / mainPrefManager.gestureSwipeSize

        binding.nestedScrollSpeak.setOnTouchListener(object :
            OnSwipeTouchListener(this@SpeakActivity) {

            override fun onLongPress() {
                if (isAvailableGesture("longPress")) {
                    longPressEnabled = true
                    showFullScreenGesturesGuide(gesture = "long-press")
                }
            }

            override fun onScroll(scrollTo: String, widthOrHeight: Int) {
                val currentOrientation = resources.configuration.orientation
                if (scrollingToBefore == scrollTo && (currentOrientation == Configuration.ORIENTATION_LANDSCAPE && (scrollTo == "d" && verticalScrollStatus == 0 || scrollTo == "u" && verticalScrollStatus == 2 || scrollTo == "l" || scrollTo == "r") || currentOrientation == Configuration.ORIENTATION_PORTRAIT)) {
                    scrollingStatus = widthOrHeight

                    scrollingToBefore = scrollTo
                    if (scrollingStatus >= 0) {
                        binding.imageTopSideViewSpeak.isGone = true
                        binding.imageBottomSideViewSpeak.isGone = true
                        binding.imageRightSideViewSpeak.isGone = true
                        binding.imageLeftSideViewSpeak.isGone = true

                        if (scrollingStatus >= 0 && scrollingStatus <= enableGestureAt) {
                            showGesturesGuide(scrollTo, widthOrHeight)
                        }
                        if (scrollingStatus >= enableGestureAt) {
                            showGesturesGuide(scrollTo, enableGestureAt)
                            showLeaveToEnable(scrollTo)
                        }
                    }
                } else {
                    hideGesturesGuide()
                    scrollingStatus = 1
                    scrollingToBefore = scrollTo
                }
            }

            override fun onDoubleTap() {
                if (isAvailableGesture("doubleTap")) {
                    showFullScreenGesturesGuide(startAnimation = true)
                    doubleTapFunction()
                }
            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == 1) {
                    //ACTION_UP
                    if (scrollingToBefore != "" && scrollingStatus > 0 && isAvailableGesture("swipeTop") || isAvailableGesture(
                            "swipeBottom"
                        ) || isAvailableGesture("swipeLeft") || isAvailableGesture("swipeRight")
                    ) {
                        Handler().postDelayed({
                            hideGesturesGuide()
                        }, 100)
                        if (scrollingStatus >= enableGestureAt) {
                            //swipe top/bottom/right/left
                            if (scrollingToBefore == "r" && isAvailableGesture("swipeRight")) swipeRight()
                            else if (scrollingToBefore == "l" && isAvailableGesture("swipeLeft")) swipeLeft()
                            else if (scrollingToBefore == "u" && isAvailableGesture("swipeTop")) swipeTop()
                            else if (scrollingToBefore == "d" && isAvailableGesture("swipeBottom")) swipeBottom()
                        }
                        scrollingStatus = 0
                        scrollingToBefore = ""
                    }
                    if (longPressEnabled && isAvailableGesture("longPress")) {
                        //longPress
                        longPressEnabled = false
                        showFullScreenGesturesGuide(startAnimation = true, gesture = "long-press")
                        longPressFunction()
                    }
                }
                return super.onTouch(v, event)
            }
        })
    }

    private fun isAvailableGesture(gesture: String): Boolean {
        return when (gesture) {
            "swipeRight" -> (speakPrefManager.gesturesSwipeRight != "")
            "swipeLeft" -> (speakPrefManager.gesturesSwipeLeft != "")
            "swipeTop" -> (speakPrefManager.gesturesSwipeTop != "")
            "swipeBottom" -> (speakPrefManager.gesturesSwipeBottom != "")
            "longPress" -> (speakPrefManager.gesturesLongPress != "")
            "doubleTap" -> (speakPrefManager.gesturesDoubleTap != "")
            else -> false
        }
    }

    fun showGesturesGuide(scrollTo: String, widthOrHeight: Int) {
        hideGesturesGuide(except = scrollTo)

        if (scrollTo == "r" && isAvailableGesture("swipeRight")) {
            binding.leftSideViewSpeak.isGone = false
            binding.leftSideViewSpeak.layoutParams.width = widthOrHeight
            binding.leftSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.leftSideViewSpeak.requestLayout()
        } else if (scrollTo == "l" && isAvailableGesture("swipeLeft")) {
            binding.rightSideViewSpeak.isGone = false
            binding.rightSideViewSpeak.layoutParams.width = widthOrHeight
            binding.rightSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.rightSideViewSpeak.requestLayout()
        } else if (scrollTo == "u" && isAvailableGesture("swipeTop")) {
            binding.bottomSideViewSpeak.isGone = false
            binding.bottomSideViewSpeak.layoutParams.height = widthOrHeight
            binding.bottomSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.bottomSideViewSpeak.requestLayout()
        } else if (scrollTo == "d" && isAvailableGesture("swipeBottom")) {
            binding.topSideViewSpeak.isGone = false
            binding.topSideViewSpeak.layoutParams.height = widthOrHeight
            binding.topSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.topSideViewSpeak.requestLayout()
        }
    }

    fun showLeaveToEnable(scrollTo: String) {
        if (scrollTo == "r" && isAvailableGesture("swipeRight")) {
            binding.leftSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageLeftSideViewSpeak.isGone = false
            binding.imageLeftSideViewSpeak.setImageResource(imageAllActions(speakPrefManager.gesturesSwipeRight))
        } else if (scrollTo == "l" && isAvailableGesture("swipeLeft")) {
            binding.rightSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageRightSideViewSpeak.setImageResource(imageAllActions(speakPrefManager.gesturesSwipeLeft))
            binding.imageRightSideViewSpeak.isGone = false
        } else if (scrollTo == "u" && isAvailableGesture("swipeTop")) {
            binding.bottomSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageBottomSideViewSpeak.isGone = false
            binding.imageBottomSideViewSpeak.setImageResource(imageAllActions(speakPrefManager.gesturesSwipeTop))
        } else if (scrollTo == "d" && isAvailableGesture("swipeBottom")) {
            binding.topSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageTopSideViewSpeak.isGone = false
            binding.imageTopSideViewSpeak.setImageResource(imageAllActions(speakPrefManager.gesturesSwipeBottom))
        }
    }

    fun hideGesturesGuide(except: String = "") {
        val widthOrHeight = binding.progressBarSpeakListen.layoutParams.height
        if (except != "r" && !binding.leftSideViewSpeak.isGone) {
            binding.leftSideViewSpeak.isGone = true
            binding.leftSideViewSpeak.layoutParams.width = widthOrHeight
            binding.leftSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.leftSideViewSpeak.requestLayout()
        }

        if (except != "l" && !binding.rightSideViewSpeak.isGone) {
            binding.rightSideViewSpeak.isGone = true
            binding.rightSideViewSpeak.layoutParams.width = widthOrHeight
            binding.rightSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.rightSideViewSpeak.requestLayout()
        }

        if (except != "u" && !binding.bottomSideViewSpeak.isGone) {
            binding.bottomSideViewSpeak.isGone = true
            binding.bottomSideViewSpeak.layoutParams.height = widthOrHeight
            binding.bottomSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.bottomSideViewSpeak.requestLayout()
        }

        if (except != "d" && !binding.topSideViewSpeak.isGone) {
            binding.topSideViewSpeak.isGone = true
            binding.topSideViewSpeak.layoutParams.height = widthOrHeight
            binding.topSideViewSpeak.setBackgroundResource(R.color.colorGesturesGuide)
            binding.topSideViewSpeak.requestLayout()
        }
    }

    fun showFullScreenGesturesGuide(startAnimation: Boolean = false, gesture: String = "") {
        if (isAvailableGesture("longPress") || isAvailableGesture("doubleTap")) {
            val action = if (gesture == "long-press") {
                speakPrefManager.gesturesLongPress
            } else {
                speakPrefManager.gesturesDoubleTap
            }
            binding.imageFullScreenViewSpeak.setImageResource(imageAllActions(action))
            binding.imageFullScreenViewSpeak.isGone = false
            binding.fullScreenViewSpeak.isGone = false
            if (startAnimation) {
                Handler().postDelayed(
                    {
                        binding.fullScreenViewSpeak.setBackgroundResource(R.color.colorGesturesGuide2)
                        Handler().postDelayed({
                            Handler().postDelayed({
                                binding.fullScreenViewSpeak.setBackgroundResource(R.color.colorGesturesGuide3)
                                Handler().postDelayed({
                                    binding.fullScreenViewSpeak.setBackgroundResource(R.color.colorGesturesGuide4)
                                    Handler().postDelayed({
                                        binding.fullScreenViewSpeak.setBackgroundResource(R.color.colorGesturesGuide5)
                                        Handler().postDelayed({
                                            binding.fullScreenViewSpeak.isGone = true
                                            binding.fullScreenViewSpeak.setBackgroundResource(R.color.colorGesturesGuide1)
                                            binding.imageFullScreenViewSpeak.isGone = true
                                        }, 50)
                                    }, 50)
                                }, 50)
                            }, 50)
                        }, 50)
                    }, 50
                )
            }
        }
    }

    fun longPressFunction() {
        allActions(speakPrefManager.gesturesLongPress)
        binding.imageFullScreenViewSpeak.isGone = true
    }

    fun doubleTapFunction() {
        allActions(speakPrefManager.gesturesDoubleTap)
    }

    fun swipeTop() {
        allActions(speakPrefManager.gesturesSwipeTop)
        Handler().postDelayed({
            hideGesturesGuide()
        }, 100)
        binding.imageBottomSideViewSpeak.isGone = true
    }

    fun swipeBottom() {
        allActions(speakPrefManager.gesturesSwipeBottom)
        Handler().postDelayed({
            hideGesturesGuide()
        }, 100)
        binding.imageTopSideViewSpeak.isGone = true
    }

    fun swipeRight() {
        allActions(speakPrefManager.gesturesSwipeRight)
        Handler().postDelayed({
            hideGesturesGuide()
        }, 100)
        binding.imageLeftSideViewSpeak.isGone = true
    }

    fun swipeLeft() {
        allActions(speakPrefManager.gesturesSwipeLeft)
        Handler().postDelayed({
            hideGesturesGuide()
        }, 100)
        binding.imageRightSideViewSpeak.isGone = true
    }

    fun allActions(action: String) {
        when (action) {
            "back" -> {
                onBackPressed()
            }
            "report" -> {
                openReportDialog()
            }
            "skip" -> {
                skipSentence()
            }
            "info" -> {
                showInformationAboutSentence()
            }
            "animations" -> {
                mainPrefManager.areAnimationsEnabled = !mainPrefManager.areAnimationsEnabled
            }
            "speed-control" -> {
                speakPrefManager.showSpeedControl = !speakPrefManager.showSpeedControl
                if (speakPrefManager.showSpeedControl) {
                    binding.speakSectionSpeedButtons.isGone = false

                    setSpeedControlButtons(speakPrefManager.audioSpeed, setup = true)
                    binding.buttonSpeed10Speak.setOnClickListener {
                        setSpeedControlButtons(1F)
                    }
                    binding.buttonSpeed15Speak.setOnClickListener {
                        setSpeedControlButtons(1.5F)
                    }
                    binding.buttonSpeed20Speak.setOnClickListener {
                        setSpeedControlButtons(2F)
                    }
                } else {
                    speakPrefManager.audioSpeed = 1F
                    binding.speakSectionSpeedButtons.isGone = true
                }
            }
            "save-recordings" -> {
                speakPrefManager.saveRecordingsOnDevice = !speakPrefManager.saveRecordingsOnDevice
            }
            "skip-confirmation" -> {
                speakPrefManager.skipRecordingConfirmation =
                    !speakPrefManager.skipRecordingConfirmation
            }
            "indicator-sound" -> {
                speakPrefManager.playRecordingSoundIndicator =
                    !speakPrefManager.playRecordingSoundIndicator
            }
            else -> {
                //nothing
            }
        }
    }

    fun imageAllActions(action: String): Int {
        return when (action) {
            "back" -> {
                R.drawable.ic_back_dark
            }
            "report" -> {
                R.drawable.ic_report
            }
            "skip" -> {
                R.drawable.ic_skip
            }
            "info" -> {
                R.drawable.ic_info_light
            }
            "animations" -> {
                R.drawable.ic_animations_white
            }
            "speed-control" -> {
                R.drawable.ic_speed_control_white
            }
            "save-recordings" -> {
                R.drawable.ic_save_white
            }
            "skip-confirmation" -> {
                R.drawable.ic_skip_confirmation_white
            }
            "indicator-sound" -> {
                R.drawable.ic_indicator_sound_white
            }
            else -> {
                R.drawable.ic_nothing
            }
        }
    }

    /*
    END | GESTURES
    */

    private fun skipSentence(forced: Boolean = false) {
        if (!recorded || forced) {
            speakViewModel.skipSentence()
            if (dailyGoalAchievedAndNotShown) {
                showDailyGoalAchievedMessage()
            }
        } else {
            dialogInflater.show(this@SpeakActivity,
                StandardDialog(
                    messageRes = R.string.text_are_you_sure_skip_and_lose_the_recording,
                    buttonTextRes = R.string.button_yes_sure,
                    onButtonClick = {
                        this@SpeakActivity.recorded = false
                        skipSentence(forced = true)
                    },
                    button2TextRes = R.string.button_cancel,
                    onButton2Click = {}
                ))
        }
    }

    fun setTheme(view: Context) = withBinding {
        theme.setElement(layoutSpeak)
        theme.setElement(view, buttonSendSpeak)
        theme.setElement(view, 1, speakSectionBottom)
        theme.setElement(
            view,
            textMessageAlertSpeak,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT,
            textSize = 15F
        )
        theme.setElement(
            view,
            textMotivationSentencesSpeak,
            R.color.colorAdviceLightTheme,
            R.color.colorAdviceDarkTheme,
            textSize = 15F
        )
        theme.setElement(view, buttonReportSpeak, background = false)
        theme.setElement(view, buttonSkipSpeak)

        setProgressBarColour(progressBarSpeakSpeak, false)
        setProgressBarColour(progressBarSpeakListen, false)

        if (settingsPrefManager.isLightThemeSentenceBoxSpeakListen) {
            theme.setElement(
                view,
                textSentenceSpeak,
                color_dark = R.color.colorWhite,
                color_light = R.color.colorBlack,
                background_dark = R.color.colorBlack,
                background_light = R.color.colorWhite
            )
            if (!theme.isDark) {
                imageOfflineModeSpeak.setImageResource(R.drawable.ic_offline_mode_dark)
                imageReportIconSpeak.setImageResource(R.drawable.ic_report_dark)
                imageInfoSpeak.setImageResource(R.drawable.ic_info_dark)
            } else {
                imageOfflineModeSpeak.setImageResource(R.drawable.ic_offline_mode)
                imageReportIconSpeak.setImageResource(R.drawable.ic_report)
                imageInfoSpeak.setImageResource(R.drawable.ic_info_light)
            }
        }
    }

    private fun openReportDialog(forced: Boolean = false) {
        if (!recorded || forced) {
            if (!binding.buttonReportSpeak.isGone || !binding.imageReportIconSpeak.isGone) {
                if (speakViewModel.state.value == SpeakViewModel.Companion.State.RECORDING) {
                    speakViewModel.stopRecording()
                }

                SpeakReportDialogFragment().show(supportFragmentManager, "SPEAK_REPORT")
            }
        } else {
            dialogInflater.show(this@SpeakActivity,
                StandardDialog(
                    messageRes = R.string.text_are_you_sure_continue_and_lose_the_recording,
                    buttonTextRes = R.string.button_yes_sure,
                    onButtonClick = {
                        this@SpeakActivity.recorded = false
                        openReportDialog(forced = true)
                    },
                    button2TextRes = R.string.button_cancel,
                    onButton2Click = {}
                ))
        }
    }

    private fun stopAndRefresh() {
        speakViewModel.stop()
        speakViewModel.currentSentence.value?.let { sentence ->
            setupUIStateStandby(sentence)
        }
        hideAudioBar()
    }

    private fun setupInitialUIState() = withBinding {
        buttonSkipSpeak.onClick {
            skipSentence()
        }

        buttonReportSpeak.onClick {
            openReportDialog()
        }

        imageReportIconSpeak.onClick {
            openReportDialog()
        }

        imageInfoSpeak.onClick {
            showInformationAboutSentence()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.startListening()
        }

        buttonSendSpeak.onClick {
            speakViewModel.sendRecording()
            numberSentThisSession++
            if (numberSentThisSession % refreshAdsAfterSpeak == 0) {
                refreshAds()
            }
            recorded = false
            if (dailyGoalAchievedAndNotShown) {
                showDailyGoalAchievedMessage()
            }
        }

        startAnimation(buttonStartStopSpeak, R.anim.zoom_in_speak_listen)
        startAnimation(buttonSkipSpeak, R.anim.zoom_in_speak_listen)
    }

    private fun showInformationAboutSentence() {
        dialogInflater.show(
            this,
            IdentifyMeDialog(messageInfoToShow, onCopyClick = {
                this
                    .getSystemService<ClipboardManager>()
                    ?.setPrimaryClip(ClipData.newPlainText("", messageInfoToShow))
                Toast.makeText(
                    this,
                    getString(R.string.copied_string),
                    Toast.LENGTH_LONG
                ).show()
            })
        )
    }

    private fun loadUIStateLoading() = withBinding {
        textMessageAlertSpeak.setText(R.string.txt_loading_sentence)
        textSentenceSpeak.text = "···"

        resizeSentence()

        buttonRecordOrListenAgain.isGone = true
        if (settingsPrefManager.showReportIcon && !imageReportIconSpeak.isGone) {
            hideImage(imageReportIconSpeak)
        } else {
            buttonReportSpeak.isGone = true
        }
        if (settingsPrefManager.showInfoIcon && !imageInfoSpeak.isGone) {
            hideImage(imageInfoSpeak)
        }
        buttonSendSpeak.isGone = true
        buttonSkipSpeak.isEnabled = false
        buttonStartStopSpeak.isEnabled = false

        val motivationSentences = arrayOf(
            resources.getQuantityString(
                R.plurals.text_continue_to_send_1,
                numberSentThisSession,
                numberSentThisSession
            ),
            resources.getQuantityString(
                R.plurals.text_continue_to_send_2,
                numberSentThisSession,
                numberSentThisSession
            ),
            resources.getQuantityString(
                R.plurals.text_continue_to_send_3,
                numberSentThisSession,
                numberSentThisSession
            ),
            resources.getQuantityString(
                R.plurals.text_continue_to_send_4,
                numberSentThisSession,
                numberSentThisSession
            )
        )
        if (textMotivationSentencesSpeak.isGone && (numberSentThisSession == 5 || numberSentThisSession == 20 || numberSentThisSession == 40 || numberSentThisSession == 80 || numberSentThisSession == 120 || numberSentThisSession == 200 || numberSentThisSession == 300 || numberSentThisSession == 500)) {
            textMotivationSentencesSpeak.isGone = false
            textMotivationSentencesSpeak.text =
                motivationSentences[(motivationSentences.indices).random()]
        } else {
            textMotivationSentencesSpeak.isGone = true
        }

        var sum = 0
        val dailyGoal = statsPrefManager.dailyGoalObjective
        try {
            sum =
                (statsPrefManager.dailyGoal.value!!.recordings + statsPrefManager.dailyGoal.value!!.validations) + 6
        } catch (e: Exception) {
            //println("Exception Speak Sum")
        }
        if (dailyGoal > 5 && (sum == dailyGoal) && numberSentThisSession > 0) {
            //if the dailygoal is set and it is almost achieved
            textMotivationSentencesSpeak.isGone = false
            textMotivationSentencesSpeak.text =
                resources.getQuantityString(
                    R.plurals.text_almost_achieved_dailygoal_speak,
                    5,
                    5
                ).replace(
                    "{{dailygoal}}",
                    dailyGoal.toString()
                )
        }

        recorded = false
    }

    private fun loadUIStateNoMoreSentences() = withBinding {
        textMessageAlertSpeak.setText(R.string.txt_common_voice_sentences_finished)
        textSentenceSpeak.text = "···"

        resizeSentence()

        buttonRecordOrListenAgain.isGone = true
        if (settingsPrefManager.showReportIcon && !imageReportIconSpeak.isGone) {
            hideImage(imageReportIconSpeak)
        } else {
            buttonReportSpeak.isGone = true
        }
        if (settingsPrefManager.showInfoIcon && !imageInfoSpeak.isGone) {
            hideImage(imageInfoSpeak)
        }
        buttonSendSpeak.isGone = true
        buttonSkipSpeak.isEnabled = false
        buttonStartStopSpeak.isEnabled = false
    }

    private fun setupUIStateStandby(sentence: Sentence) = withBinding {
        buttonSkipSpeak.isEnabled = true
        buttonStartStopSpeak.isEnabled = true

        if (settingsPrefManager.showReportIcon && imageReportIconSpeak.isGone) {
            showImage(imageReportIconSpeak)
        } else {
            buttonReportSpeak.isGone = false
        }
        if (settingsPrefManager.showInfoIcon && imageInfoSpeak.isGone) {
            showImage(imageInfoSpeak)
        }

        buttonSendSpeak.isGone = true

        buttonRecordOrListenAgain.isGone = true
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak_cv)

        hideAudioBar()

        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_speak_1)
        textSentenceSpeak.text = sentence.sentenceText
        messageInfoToShow =
            "sentence-id: ${sentence.sentenceId}\nexpiry-date: ${sentence.expiryDate}"


        resizeSentence()

        buttonStartStopSpeak.onClick {
            checkPermission()
            speakViewModel.startRecording()
        }
    }

    private fun resizeSentence() {
        binding.textSentenceSpeak.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            when (binding.textSentenceSpeak.text.length) {
                in 0..10 -> resources.getDimension(R.dimen.title_very_big) * mainPrefManager.textSize
                in 11..20 -> resources.getDimension(R.dimen.title_big) * mainPrefManager.textSize
                in 21..40 -> resources.getDimension(R.dimen.title_medium) * mainPrefManager.textSize
                in 41..70 -> resources.getDimension(R.dimen.title_normal) * mainPrefManager.textSize
                else -> resources.getDimension(R.dimen.title_small) * mainPrefManager.textSize
            }
        )
        withBinding {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            //val width = metrics.widthPixels
            val height = metrics.heightPixels
            val newMinHeight = if (height / 2 > 1500) 1000 else height / 3
            textSentenceSpeak.minHeight = newMinHeight
        }
    }

    private fun loadUIStateRecording() = withBinding {
        recorded = true
        buttonRecordOrListenAgain.isGone = true
        buttonStartStopSpeak.setBackgroundResource(R.drawable.stop_cv)

        buttonSendSpeak.isGone = true
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_speak_2)
        speakViewModel.isFirstTimeListening = true

        buttonStartStopSpeak.onClick {
            checkPermission()
            speakViewModel.stopRecording()
        }
    }

    private fun loadUIStateRecorded() = withBinding {
        recorded = true
        buttonRecordOrListenAgain.isGone = false
        startAnimation(buttonRecordOrListenAgain, R.anim.zoom_in_speak_listen)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.speak2_cv)

        buttonStartStopSpeak.setBackgroundResource(R.drawable.listen2_cv)
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_listen_1)

        buttonStartStopSpeak.onClick {
            speakViewModel.startListening()
        }

        buttonRecordOrListenAgain.onClick {
            checkPermission()
            speakViewModel.redoRecording()
        }
    }

    private fun loadUIStateListening() = withBinding {
        buttonRecordOrListenAgain.isGone = true
        buttonStartStopSpeak.setBackgroundResource(R.drawable.stop_cv)
        textMessageAlertSpeak.setText(R.string.txt_press_icon_below_listen_2)

        buttonStartStopSpeak.onClick {
            speakViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() = withBinding {
        buttonSendSpeak.isGone = false

        if (speakViewModel.isFirstTimeListening) {
            startAnimation(buttonSendSpeak, R.anim.zoom_in_speak_listen)
            speakViewModel.isFirstTimeListening = false
        }

        textMessageAlertSpeak.setText(R.string.txt_recorded_correct_or_wrong)
        buttonRecordOrListenAgain.isGone = false
        startAnimation(buttonRecordOrListenAgain, R.anim.zoom_in_speak_listen)
        buttonRecordOrListenAgain.setBackgroundResource(R.drawable.listen2_cv)
        buttonStartStopSpeak.setBackgroundResource(R.drawable.speak2_cv)


        buttonStartStopSpeak.onClick {
            checkPermission()
            speakViewModel.redoRecording()
        }

        buttonRecordOrListenAgain.onClick {
            speakViewModel.startListening()
        }
    }

    private fun setupBadgeDialog(): Any = if (mainPrefManager.isLoggedIn) {
        lifecycleScope.launch {
            statsPrefManager.badgeLiveData.collect {
                if (it is BadgeDialogMediator.Speak || it is BadgeDialogMediator.Level) {
                    dialogInflater.show(
                        this@SpeakActivity,
                        StandardDialog(
                            message = getString(R.string.new_badge_earnt_message)
                                .replace(
                                    "{{profile}}",
                                    getString(R.string.button_home_profile)
                                )
                                .replace(
                                    "{{all_badges}}",
                                    getString(R.string.btn_badges_loggedin)
                                )
                        )
                    )
                }
            }
        }
    } else Unit

    private fun checkPermission() {
        var conditions = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
        var permissionsArray = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (speakPrefManager.saveRecordingsOnDevice) {
            conditions = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            permissionsArray = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        if (conditions) {
            ActivityCompat.requestPermissions(
                this,
                permissionsArray,
                RECORD_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        var conditions =
            grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED
        if (speakPrefManager.saveRecordingsOnDevice) conditions =
            grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (conditions) {
                    onBackPressed()
                }
            }
        }
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

        if (dailyGoal > 0 && sum == 0) {
            //daily goal set, but no contribution have been inserted yet
            newValue = 1
            setProgressBarColour(progressBar, forced = true)
            progressBar.isGone = true
        } else if (dailyGoal == 0) {
            //daily goal not set
            newValue = width / 2
            setProgressBarColour(progressBar, color = R.color.colorBlack)
            progressBar.isGone = false
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

        if (!view.isGone) {
            if (mainPrefManager.areAnimationsEnabled) {
                animationProgressBar(progressBar, view.width, newValue)
            } else {
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

    private fun animateAudioBar() {
        if (mainPrefManager.areAnimationsEnabled) {
            this.animationsCount++
            binding.speakSectionAudioBar.children.forEach {
                animateAudioBar(it, animationsCount)
            }
        }
    }

    private fun hideAudioBar() {
        if (mainPrefManager.areAnimationsEnabled) {
            if (binding.imageAudioBarCenter.isVisible && isAudioBarVisible) {
                isAudioBarVisible = false
                binding.speakSectionAudioBar.children.forEach {
                    animateAudioBar(it, animationsCount)
                }
            }
        }
    }

    private fun animateAudioBar(view: View, animationsCountTemp: Int) {
        if (speakViewModel.state.value == SpeakViewModel.Companion.State.RECORDING && this.isAudioBarVisible) {
            animationAudioBar(
                view,
                view.height,
                (minHeight..maxHeight).random(),
                animationsCountTemp
            )
            view.isVisible = true
        } else if (this.isAudioBarVisible && view.height >= minHeight) {
            animationAudioBar(view, view.height, 2, animationsCountTemp, forced = true)
            view.isVisible = true
        } else {
            view.isVisible = false
        }
    }

    private fun animationAudioBar(
        view: View,
        min: Int,
        max: Int,
        animationsCountTemp: Int,
        forced: Boolean = false
    ) {
        val animation: ValueAnimator =
            ValueAnimator.ofInt(min, max)
        animation.duration = 300
        animation.addUpdateListener { anim ->
            val value = anim.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        animation.doOnEnd {
            if (this.animationsCount == animationsCountTemp && forced && max == 2) {
                view.isVisible = false
            }
            if (this.isAudioBarVisible && view.isVisible && !forced && this.animationsCount == animationsCountTemp) {
                animateAudioBar(view, animationsCountTemp)
            }
        }
        animation.start()
    }

    private fun showImage(image: ImageView) {
        if (!image.isVisible) {
            image.isGone = false
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
        image.isGone = true
    }

    private fun stopImage(image: ImageView) {
        stopAnimation(image)
    }

}
