package org.commonvoice.saverio

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
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
import org.commonvoice.saverio.ui.dialogs.specificDialogs.IdentifyMeDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.OfflineModeDialog
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_ads.AdLoader
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.dataClasses.BadgeDialogMediator
import org.commonvoice.saverio_lib.dataClasses.DailyGoal
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.utils.AudioConstants
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

    private var messageInfoToShow = ""

    var minHeightButton1 = 80
    var maxHeightButton1 = 100
    var minHeightButton2 = 100
    var maxHeightButton2 = 120
    var minHeightButtons = 50

    private var scrollingStatus = 0
    private var scrollingStatusBefore = 0
    private var scrollingToBefore = ""
    private var longPressEnabled = false
    private var enableGestureAt = 50

    private var dailyGoalAchievedAndNotShown = false
    private var dailyGoalAchievedAndNotShownIt: DailyGoal? = null
    private var noAutoPlayForced: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        setupUI()

        minHeightButton1 = binding.buttonStartStopListen.layoutParams.height
        maxHeightButton1 = binding.viewListenAnimateButton1.layoutParams.height
        minHeightButton2 = binding.viewListenAnimateButton1.layoutParams.height
        maxHeightButton2 = binding.viewListenAnimateButton2.layoutParams.height
        minHeightButtons = binding.viewListenAnimateButtonHidden.layoutParams.height

        binding.viewListenAnimateButton1.layoutParams.height = minHeightButtons
        binding.viewListenAnimateButton1.layoutParams.width = minHeightButtons
        binding.viewListenAnimateButton2.layoutParams.height = minHeightButtons
        binding.viewListenAnimateButton2.layoutParams.width = minHeightButtons
        binding.viewListenAnimateButton1.requestLayout()
        binding.viewListenAnimateButton2.requestLayout()
    }

    override fun onResume() {
        super.onResume()

        volumeControlStream = AudioConstants.VolumeControlStream
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
                NoClipsSentencesAvailableDialog(
                    this,
                    isSentencesDialog = false,
                    isOfflineModeDisabledDialog = true,
                    0,
                    theme
                ).show {
                    onBackPressed()
                }
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
            skipClip()
        }

        buttonYesClip.isGone = true
        buttonNoClip.isGone = true
    }

    private fun setupUI() {
        binding.imageOfflineModeListen.onClick {
            lifecycleScope.launch {
                val count = listenViewModel.getClipsCount()
                withContext(Dispatchers.Main) {
                    NoClipsSentencesAvailableDialog(
                        this@ListenActivity,
                        isSentencesDialog = false,
                        isOfflineModeDisabledDialog = false,
                        count,
                        theme
                    ).show()
                }
            }
        }

        connectionManager.liveInternetAvailability.observe(this, Observer { available ->
            checkOfflineMode(available)
        })

        listenViewModel.hasFinishedClips.observe(this, Observer {
            if (it && !connectionManager.isInternetAvailable) {
                NoClipsSentencesAvailableDialog(
                    this,
                    isSentencesDialog = false,
                    isOfflineModeDisabledDialog = false,
                    0,
                    theme
                ).show {
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
                //achieved
                setDailyGoalAchievedAndNotShown(it)
                if (listenViewModel.state.value == ListenViewModel.Companion.State.STANDBY) showDailyGoalAchievedMessage()
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

            if (it.recordings == 0 && it.validations > 0 && it.getDailyGoal() > 0) {
                binding.progressBarListenSpeak.isGone = true
            }
            if (it.validations == 0 && it.recordings > 0 && it.getDailyGoal() > 0) {
                binding.progressBarListenListen.isGone = true
            }
        })

        if (listenPrefManager.showSpeedControl) {
            binding.listenSectionSpeedButtons.isGone = false

            setSpeedControlButtons(listenPrefManager.audioSpeed, setup = true)
            binding.buttonSpeed10Listen.setOnClickListener {
                setSpeedControlButtons(1F)
            }
            binding.buttonSpeed15Listen.setOnClickListener {
                setSpeedControlButtons(1.5F)
            }
            binding.buttonSpeed20Listen.setOnClickListener {
                setSpeedControlButtons(2F)
            }
        }

        checkOfflineMode(connectionManager.isInternetAvailable)

        setupNestedScroll()

        setupBadgeDialog()

        setTheme(this)

        if (listenPrefManager.showAdBanner) {
            AdLoader.setupListenAdView(this, binding.adContainer)
        }
    }

    private fun setDailyGoalAchievedAndNotShown(dailyGoal: DailyGoal) {
        dailyGoalAchievedAndNotShown = true
        dailyGoalAchievedAndNotShownIt = dailyGoal
    }

    private fun showDailyGoalAchievedMessage() {
        if (dailyGoalAchievedAndNotShownIt != null) {
            dialogInflater.show(
                this,
                DailyGoalAchievedDialog(this, dailyGoalAchievedAndNotShownIt!!)
            )
            //stopAndRefresh()
            noAutoPlayForced = true
            dailyGoalAchievedAndNotShown = false
        }
    }

    private fun setSpeedControlButtons(speed: Float, setup: Boolean = false) {
        listenPrefManager.audioSpeed = speed
        val buttons = mapOf(
            1F to binding.buttonSpeed10Listen,
            1.5F to binding.buttonSpeed15Listen,
            2F to binding.buttonSpeed20Listen
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

        if (statsPrefManager.dailyGoal.value!!.recordings == 0 && statsPrefManager.dailyGoal.value!!.validations > 0 && statsPrefManager.dailyGoal.value!!.goal > 0) {
            binding.progressBarListenSpeak.isGone = true
        }
        if (statsPrefManager.dailyGoal.value!!.validations == 0 && statsPrefManager.dailyGoal.value!!.recordings > 0 && statsPrefManager.dailyGoal.value!!.goal > 0) {
            binding.progressBarListenListen.isGone = true
        }

        refreshAds()
        resizeSentence()
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

        binding.nestedScrollListen.setOnTouchListener(object :
            OnSwipeTouchListener(this@ListenActivity) {

            override fun onLongPress() {
                if (isAvailableGesture("longPress")) {
                    longPressEnabled = true
                    showFullScreenGesturesGuide(gesture = "long-press")
                }
            }

            override fun onScroll(scrollTo: String, widthOrHeight: Int) {
                binding.imageTopSideViewListen.isGone = true
                binding.imageBottomSideViewListen.isGone = true
                binding.imageRightSideViewListen.isGone = true
                binding.imageLeftSideViewListen.isGone = true

                if (scrollingToBefore == scrollTo && (scrollTo == "d" && (verticalScrollStatus == 0 || verticalScrollStatus == 2) || scrollTo == "u" && verticalScrollStatus == 2 || scrollTo == "l" || scrollTo == "r")) {
                    scrollingStatus = widthOrHeight - scrollingStatusBefore

                    scrollingToBefore = scrollTo
                    if (scrollingStatus >= 0) {

                        if (scrollingStatus >= 0 && scrollingStatus <= enableGestureAt) {
                            showGesturesGuide(scrollTo, scrollingStatus)
                        }
                        if (scrollingStatus >= enableGestureAt) {
                            showGesturesGuide(scrollTo, enableGestureAt)
                            showLeaveToEnable(scrollTo)
                        }
                    }
                } else {
                    hideGesturesGuide()
                    scrollingStatusBefore = 0
                    scrollingStatus = 1
                    scrollingToBefore = scrollTo
                    if (scrollTo == "d" && verticalScrollStatus == 1 || scrollTo == "u" && verticalScrollStatus == 1) {
                        //reset scrolling
                        scrollingStatusBefore = widthOrHeight
                    }
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
                        showFullScreenGesturesGuide(startAnimation = true)
                        longPressFunction()
                    }
                }
                return super.onTouch(v, event)
            }
        })
    }

    private fun isAvailableGesture(gesture: String): Boolean {
        return when (gesture) {
            "swipeRight" -> (listenPrefManager.gesturesSwipeRight != "")
            "swipeLeft" -> (listenPrefManager.gesturesSwipeLeft != "")
            "swipeTop" -> (listenPrefManager.gesturesSwipeTop != "")
            "swipeBottom" -> (listenPrefManager.gesturesSwipeBottom != "")
            "longPress" -> (listenPrefManager.gesturesLongPress != "")
            "doubleTap" -> (listenPrefManager.gesturesDoubleTap != "")
            else -> false
        }
    }

    fun showGesturesGuide(scrollTo: String, widthOrHeight: Int) {
        hideGesturesGuide(except = scrollTo)

        if (scrollTo == "r" && isAvailableGesture("swipeRight")) {
            binding.leftSideViewListen.isGone = false
            binding.leftSideViewListen.layoutParams.width = widthOrHeight
            binding.leftSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
            binding.leftSideViewListen.requestLayout()
        } else if (scrollTo == "l" && isAvailableGesture("swipeLeft")) {
            binding.rightSideViewListen.isGone = false
            binding.rightSideViewListen.layoutParams.width = widthOrHeight
            binding.rightSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
            binding.rightSideViewListen.requestLayout()
        } else if (scrollTo == "u" && isAvailableGesture("swipeTop")) {
            binding.bottomSideViewListen.isGone = false
            binding.bottomSideViewListen.layoutParams.height = widthOrHeight
            binding.bottomSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
            binding.bottomSideViewListen.requestLayout()
        } else if (scrollTo == "d" && isAvailableGesture("swipeBottom")) {
            binding.topSideViewListen.isGone = false
            binding.topSideViewListen.layoutParams.height = widthOrHeight
            binding.topSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
            binding.topSideViewListen.requestLayout()
        }
    }

    fun showLeaveToEnable(scrollTo: String) {
        if (scrollTo == "r" && isAvailableGesture("swipeRight")) {
            binding.leftSideViewListen.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageLeftSideViewListen.isGone = false
            binding.imageLeftSideViewListen.setImageResource(imageAllActions(listenPrefManager.gesturesSwipeRight))
        } else if (scrollTo == "l" && isAvailableGesture("swipeLeft")) {
            binding.rightSideViewListen.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageRightSideViewListen.setImageResource(imageAllActions(listenPrefManager.gesturesSwipeLeft))
            binding.imageRightSideViewListen.isGone = false
        } else if (scrollTo == "u" && isAvailableGesture("swipeTop")) {
            binding.bottomSideViewListen.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageBottomSideViewListen.isGone = false
            binding.imageBottomSideViewListen.setImageResource(imageAllActions(listenPrefManager.gesturesSwipeTop))
        } else if (scrollTo == "d" && isAvailableGesture("swipeBottom")) {
            binding.topSideViewListen.setBackgroundResource(R.color.colorGesturesGuideLeaveToEnable)
            binding.imageTopSideViewListen.isGone = false
            binding.imageTopSideViewListen.setImageResource(imageAllActions(listenPrefManager.gesturesSwipeBottom))
        }
    }

    fun hideGesturesGuide(except: String = "") {
        var widthOrHeight = 0
        try {
            widthOrHeight = binding.progressBarListenListen.layoutParams.height

            if (except != "r" && !binding.leftSideViewListen.isGone) {
                binding.leftSideViewListen.isGone = true
                binding.leftSideViewListen.layoutParams.width = widthOrHeight
                binding.leftSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
                binding.leftSideViewListen.requestLayout()
            }

            if (except != "l" && !binding.rightSideViewListen.isGone) {
                binding.rightSideViewListen.isGone = true
                binding.rightSideViewListen.layoutParams.width = widthOrHeight
                binding.rightSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
                binding.rightSideViewListen.requestLayout()
            }

            if (except != "u" && !binding.bottomSideViewListen.isGone) {
                binding.bottomSideViewListen.isGone = true
                binding.bottomSideViewListen.layoutParams.height = widthOrHeight
                binding.bottomSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
                binding.bottomSideViewListen.requestLayout()
            }

            if (except != "d" && !binding.topSideViewListen.isGone) {
                binding.topSideViewListen.isGone = true
                binding.topSideViewListen.layoutParams.height = widthOrHeight
                binding.topSideViewListen.setBackgroundResource(R.color.colorGesturesGuide)
                binding.topSideViewListen.requestLayout()
            }
        } catch (e: Exception) {
        }
    }

    fun showFullScreenGesturesGuide(startAnimation: Boolean = false, gesture: String = "") {
        if (isAvailableGesture("longPress") || isAvailableGesture("doubleTap")) {
            val action = if (gesture == "long-press") {
                listenPrefManager.gesturesLongPress
            } else {
                listenPrefManager.gesturesDoubleTap
            }
            binding.imageFullScreenViewListen.setImageResource(imageAllActions(action))
            binding.fullScreenViewListen.isGone = false
            if (startAnimation) {
                Handler().postDelayed(
                    {
                        binding.fullScreenViewListen.setBackgroundResource(R.color.colorGesturesGuide2)
                        Handler().postDelayed({
                            Handler().postDelayed({
                                binding.fullScreenViewListen.setBackgroundResource(R.color.colorGesturesGuide3)
                                Handler().postDelayed({
                                    binding.fullScreenViewListen.setBackgroundResource(R.color.colorGesturesGuide4)
                                    Handler().postDelayed({
                                        binding.fullScreenViewListen.setBackgroundResource(R.color.colorGesturesGuide5)
                                        Handler().postDelayed({
                                            binding.fullScreenViewListen.isGone = true
                                            binding.fullScreenViewListen.setBackgroundResource(R.color.colorGesturesGuide1)
                                            binding.imageFullScreenViewListen.isGone = true
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
        allActions(listenPrefManager.gesturesLongPress)
        binding.imageFullScreenViewListen.isGone = true
    }

    fun doubleTapFunction() {
        allActions(listenPrefManager.gesturesDoubleTap)
    }

    fun swipeTop() {
        allActions(listenPrefManager.gesturesSwipeTop)
        Handler().postDelayed({
            hideGesturesGuide()
            binding.imageBottomSideViewListen.isGone = true
        }, 100)
    }

    fun swipeBottom() {
        allActions(listenPrefManager.gesturesSwipeBottom)
        Handler().postDelayed({
            hideGesturesGuide()
            binding.imageTopSideViewListen.isGone = true
        }, 100)
    }

    fun swipeRight() {
        allActions(listenPrefManager.gesturesSwipeRight)
        Handler().postDelayed({
            hideGesturesGuide()
            binding.imageLeftSideViewListen.isGone = true
        }, 100)
    }

    fun swipeLeft() {
        allActions(listenPrefManager.gesturesSwipeLeft)
        Handler().postDelayed({
            hideGesturesGuide()
            binding.imageRightSideViewListen.isGone = true
        }, 100)
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
                skipClip()
            }
            "info" -> {
                showInformationAboutClip()
            }
            "animations" -> {
                mainPrefManager.areAnimationsEnabled = !mainPrefManager.areAnimationsEnabled
            }
            "speed-control" -> {
                listenPrefManager.showSpeedControl = !listenPrefManager.showSpeedControl
                if (listenPrefManager.showSpeedControl) {
                    binding.listenSectionSpeedButtons.isGone = false

                    setSpeedControlButtons(listenPrefManager.audioSpeed, setup = true)
                    binding.buttonSpeed10Listen.setOnClickListener {
                        setSpeedControlButtons(1F)
                    }
                    binding.buttonSpeed15Listen.setOnClickListener {
                        setSpeedControlButtons(1.5F)
                    }
                    binding.buttonSpeed20Listen.setOnClickListener {
                        setSpeedControlButtons(2F)
                    }
                } else {
                    listenPrefManager.audioSpeed = 1F
                    binding.listenSectionSpeedButtons.isGone = true
                }
            }
            "auto-play" -> {
                listenPrefManager.isAutoPlayClipEnabled = !listenPrefManager.isAutoPlayClipEnabled
            }
            "validate-yes" -> {
                validateYes()
            }
            "validate-no" -> {
                validateNo()
            }
            else -> {
                //nothing
            }
        }
    }

    private fun imageAllActions(action: String): Int {
        return when (action) {
            "back" -> {
                R.drawable.ic_back_gestures
            }
            "report" -> {
                R.drawable.ic_report_gestures
            }
            "skip" -> {
                R.drawable.ic_skip
            }
            "info" -> {
                R.drawable.ic_info_gestures
            }
            "animations" -> {
                R.drawable.ic_animations_white
            }
            "speed-control" -> {
                R.drawable.ic_speed_control_white
            }
            "auto-play" -> {
                R.drawable.ic_auto_play_white
            }
            "validate-yes" -> {
                R.drawable.ic_yes_thumb2
            }
            "validate-no" -> {
                R.drawable.ic_no_thumb2
            }
            else -> {
                R.drawable.ic_nothing
            }
        }
    }

    /*
    END | GESTURES
    */

    private fun skipClip() {
        listenViewModel.skipClip()
        if (dailyGoalAchievedAndNotShown) {
            showDailyGoalAchievedMessage()
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

    private fun setTheme(view: Context) = withBinding {
        theme.setElement(layoutListen)
        theme.setElement(view, 1, listenSectionBottom)
        theme.setElement(
            view,
            textMessageAlertListen,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT,
            textSize = 15F
        )
        theme.setElement(
            view,
            textMotivationalSentencesListen,
            R.color.colorAdviceLightTheme,
            R.color.colorAdviceDarkTheme,
            textSize = 15F
        )
        theme.setElement(view, buttonReportListen, background = false)
        theme.setElement(view, buttonSkipListen)

        setProgressBarColour(progressBarListenSpeak, false)
        setProgressBarColour(progressBarListenListen, false)

        setTextSentenceListen(view)
    }

    private fun setTextSentenceListen(view: Context) = withBinding {
        if (settingsPrefManager.isLightThemeSentenceBoxSpeakListen) {
            theme.setElement(
                view,
                textSentenceListen,
                color_dark = R.color.colorWhite,
                color_light = R.color.colorBlack,
                background_dark = R.color.colorBlack,
                background_light = R.color.colorWhite
            )
            if (!theme.isDark) {
                imageOfflineModeListen.setImageResource(R.drawable.ic_offline_mode_dark)
                imageReportIconListen.setImageResource(R.drawable.ic_report_dark)
                imageInfoListen.setImageResource(R.drawable.ic_info_dark)
            } else {
                imageOfflineModeListen.setImageResource(R.drawable.ic_offline_mode)
                imageReportIconListen.setImageResource(R.drawable.ic_report)
                imageInfoListen.setImageResource(R.drawable.ic_info_light)
            }
        }
        resizeSentence()
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
        setTextSentenceListen(this@ListenActivity)

        if (!listenViewModel.stopped) {
            textSentenceListen.text = "···"
            resizeSentence()
            textMessageAlertListen.setText(R.string.txt_loading_sentence)
            buttonStartStopListen.isEnabled = false
            if (settingsPrefManager.showReportIcon && !imageReportIconListen.isGone) {
                hideImage(imageReportIconListen)
            } else if (!settingsPrefManager.showReportIcon) {
                buttonReportListen.isGone = true
            }
            if (settingsPrefManager.showInfoIcon && !imageInfoListen.isGone) {
                hideImage(imageInfoListen)
            }
            if (!buttonYesClip.isGone) {
                hideButton(buttonYesClip)
            }
            if (!buttonNoClip.isGone) {
                hideButton(buttonNoClip)
            }
            buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
            hideListenAnimateButtons()

            val motivationSentences = arrayOf(
                resources.getQuantityString(
                    R.plurals.text_continue_to_validate_1,
                    numberSentThisSession,
                    numberSentThisSession
                ),
                resources.getQuantityString(
                    R.plurals.text_continue_to_validate_2,
                    numberSentThisSession,
                    numberSentThisSession
                ),
                resources.getQuantityString(
                    R.plurals.text_continue_to_validate_3,
                    numberSentThisSession,
                    numberSentThisSession
                ),
                resources.getQuantityString(
                    R.plurals.text_continue_to_validate_4,
                    numberSentThisSession,
                    numberSentThisSession
                )
            )

            if (textMotivationalSentencesListen.isGone && (numberSentThisSession == 5 || numberSentThisSession == 20 || numberSentThisSession == 40 || numberSentThisSession == 80 || numberSentThisSession == 120 || numberSentThisSession == 200 || numberSentThisSession == 300 || numberSentThisSession == 500)) {
                textMotivationalSentencesListen.isGone = false
                textMotivationalSentencesListen.text =
                    motivationSentences[(motivationSentences.indices).random()]
            } else {
                textMotivationalSentencesListen.isGone = true
            }

            var sum = 0
            val dailyGoal = statsPrefManager.dailyGoalObjective
            try {
                sum =
                    (statsPrefManager.dailyGoal.value!!.recordings + statsPrefManager.dailyGoal.value!!.validations) + 6
            } catch (e: Exception) {
                //println("Exception Listen Sum")
            }
            if (dailyGoal > 5 && (sum == dailyGoal) && numberSentThisSession > 0) {
                //if the dailygoal is not set and the dailygoal is almost achieved
                textMotivationalSentencesListen.isGone = false
                textMotivationalSentencesListen.text =
                    resources.getQuantityString(
                        R.plurals.text_almost_achieved_dailygoal_listen,
                        5,
                        5
                    ).replace(
                        "{{dailygoal}}",
                        dailyGoal.toString()
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
        setTextSentenceListen(this@ListenActivity)

        if (!listenViewModel.stopped) {
            textSentenceListen.text = "···"
            resizeSentence()
            textMessageAlertListen.setText(R.string.txt_common_voice_clips_finished)
            buttonStartStopListen.isEnabled = false
            if (settingsPrefManager.showReportIcon && !imageReportIconListen.isGone) {
                hideImage(imageReportIconListen)
            } else if (!settingsPrefManager.showReportIcon) {
                buttonReportListen.isGone = true
            }
            if (settingsPrefManager.showInfoIcon && !imageInfoListen.isGone) {
                hideImage(imageInfoListen)
            }
            if (!buttonYesClip.isGone) {
                hideButton(buttonYesClip)
            }
            if (!buttonNoClip.isGone) {
                hideButton(buttonNoClip)
            }
            buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
            hideListenAnimateButtons()
        }
        //buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
        if (!listenViewModel.opened) {
            listenViewModel.opened = true
            startAnimation(buttonStartStopListen, R.anim.zoom_in_speak_listen)
            startAnimation(buttonSkipListen, R.anim.zoom_in_speak_listen)
        }
    }

    private fun loadUIStateStandby(clip: Clip, noAutoPlay: Boolean = false) = withBinding {
        setTextSentenceListen(this@ListenActivity)

        if (listenViewModel.stopped) {
            //stopped recording
            buttonStartStopListen.setBackgroundResource(R.drawable.listen2_cv)
        } else {
            buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)

            hideButtons()

            listenViewModel.listenedOnce = false
            listenViewModel.startedOnce = false
        }

        if (listenViewModel.showSentencesTextAtTheEnd() && !listenViewModel.listenedOnce) {
            textMessageAlertListen.text = getString(R.string.txt_sentence_feature_enabled).replace(
                "{{feature_name}}",
                getString(R.string.txt_show_sentence_at_the_ending)
            ) + "\n" + getString(R.string.txt_press_icon_below_listen_1)

        } else textMessageAlertListen.setText(R.string.txt_clip_correct_or_wrong)

        if (listenViewModel.showSentencesTextAtTheEnd() && !listenViewModel.listenedOnce) {
            textSentenceListen.setText(R.string.txt_sentence_text_hidden)
            textSentenceListen.setTextColor(
                ContextCompat.getColor(
                    this@ListenActivity,
                    R.color.colorLightRed
                )
            )
        } else {
            textSentenceListen.text = clip.sentence.sentenceText
            setTextSentenceListen(this@ListenActivity)
        }
        messageInfoToShow =
            "clip-id: ${clip.id}\nclip-glob: ${clip.glob}\nsentence-id: ${clip.sentence.sentenceId}\nexpiry-date: ${clip.sentence.expiryDate}"

        hideListenAnimateButtons()

        resizeSentence()

        if (settingsPrefManager.showReportIcon && imageReportIconListen.isGone) {
            showImage(imageReportIconListen)
        } else if (!settingsPrefManager.showReportIcon) {
            buttonReportListen.isGone = false
        }
        if (settingsPrefManager.showInfoIcon && imageInfoListen.isGone) {
            showImage(imageInfoListen)
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

        if (!listenViewModel.startedOnce) {
            if (listenViewModel.autoPlay() && !noAutoPlayForced && !noAutoPlay && !(!settingsPrefManager.isOfflineMode && !connectionManager.isInternetAvailable)) {
                listenViewModel.startListening()
            }
        }
        noAutoPlayForced = false

        buttonReportListen.onClick {
            openReportDialog()
        }
        imageReportIconListen.onClick {
            openReportDialog()
        }
        imageInfoListen.onClick {
            showInformationAboutClip()
        }
    }

    private fun showInformationAboutClip() {
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
        withBinding {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            //val width = metrics.widthPixels
            val height = metrics.heightPixels
            val newMinHeight = if (height / 2 > 1500) 1000 else height / 3
            textSentenceListen.minHeight = newMinHeight
        }
    }

    private fun loadUIStateListening() = withBinding {
        stopButtons()

        setTextSentenceListen(this@ListenActivity)

        if (listenViewModel.showSentencesTextAtTheEnd() && !listenViewModel.listenedOnce) {
            textMessageAlertListen.text = getString(R.string.txt_sentence_feature_enabled).replace(
                "{{feature_name}}",
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
                if (listenViewModel.state.value == ListenViewModel.Companion.State.LISTENING)
                    showButton(buttonNoClip)
            }, 900)
        }

        if (!listenViewModel.listenedOnce) buttonYesClip.isVisible = false
        listenViewModel.startedOnce = true
        buttonSkipListen.isEnabled = true

        buttonStartStopListen.setBackgroundResource(R.drawable.stop_cv)

        buttonNoClip.onClick {
            validateNo()
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

        setTextSentenceListen(this@ListenActivity)
        if (!listenViewModel.listenedOnce) {
            showButton(buttonYesClip)
        }
        listenViewModel.listenedOnce = true

        textMessageAlertListen.setText(R.string.txt_clip_correct_or_wrong)

        buttonStartStopListen.setBackgroundResource(R.drawable.listen2_cv)

        buttonYesClip.onClick {
            validateYes()
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

    private fun validateYes() {
        if (!binding.buttonYesClip.isGone) {
            hideButtons()
            listenViewModel.validate(result = true)
            numberSentThisSession++
            if (numberSentThisSession % refreshAdsAfterListen == 0) {
                refreshAds()
            }
            if (dailyGoalAchievedAndNotShown) {
                showDailyGoalAchievedMessage()
            }
        }
    }

    private fun validateNo() {
        if (!binding.buttonNoClip.isGone) {
            hideButtons()
            listenViewModel.validate(result = false)
            numberSentThisSession++
            if (numberSentThisSession % refreshAdsAfterListen == 0) {
                refreshAds()
            }
            if (dailyGoalAchievedAndNotShown) {
                showDailyGoalAchievedMessage()
            }
        }
    }

    override fun onBackPressed() = withBinding {
        textMessageAlertListen.setText(R.string.txt_closing)
        buttonStartStopListen.setBackgroundResource(R.drawable.listen_cv)
        textSentenceListen.text = "···"
        resizeSentence()
        setTextSentenceListen(this@ListenActivity)
        if (settingsPrefManager.showReportIcon && !imageReportIconListen.isGone) {
            hideImage(imageReportIconListen)
        } else if (!settingsPrefManager.showReportIcon) {
            buttonReportListen.isGone = true
        }
        if (settingsPrefManager.showInfoIcon && !imageInfoListen.isGone) {
            hideImage(imageInfoListen)
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
                minHeightButton1,
                maxHeightButton1,
                this.animationsCount
            )
            animateListenAnimateButton(
                binding.viewListenAnimateButton2,
                minHeightButton2,
                maxHeightButton2,
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
                    minHeightButtons,
                    animationsCount
                )
                animateListenAnimateButton(
                    viewListenAnimateButton2,
                    viewListenAnimateButton2.height,
                    minHeightButtons,
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
        } else if (!this.isListenAnimateButtonVisible && view.height >= minHeightButton1) {
            animationListenAnimateButton(
                view,
                view.height,
                view.height,
                minHeightButtons,
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

        if (max == minHeightButtons) animation.duration = 300
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