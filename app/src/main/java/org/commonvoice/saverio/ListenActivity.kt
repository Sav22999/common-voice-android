package org.commonvoice.saverio

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonvoice.saverio.databinding.ActivityListenBinding
import org.commonvoice.saverio.ui.dialogs.ListenReportDialogFragment
import org.commonvoice.saverio.ui.dialogs.NoClipsSentencesAvailableDialog
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel


class ListenActivity : ViewBoundActivity<ActivityListenBinding>(
    ActivityListenBinding::inflate
) {

    private val listenViewModel: ListenViewModel by stateViewModel()
    private val connectionManager: ConnectionManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()

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
                    showMessageDialog("", "", 10)
                }
            } else if (!settingsPrefManager.isOfflineMode) {
                showMessageDialog("", getString(R.string.offline_mode_is_not_enabled))
                onBackPressed()
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
                    listenViewModel.loadNewClip()
                }
                ListenViewModel.Companion.State.LISTENING -> {
                    loadUIStateListening()
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
                showMessageDialog(
                    "",
                    getString(R.string.daily_goal_achieved_message).replace(
                        "{{*{{n_clips}}*}}",
                        "${it.validations}"
                    ).replace(
                        "{{*{{n_sentences}}*}}",
                        "${it.recordings}"
                    ), type = 12
                )
            }
            animateProgressBar(
                dailyGoal = it.getDailyGoal(),
                currentRecordingsValidations = (it.validations + it.recordings)
            )
        })

        checkOfflineMode(connectionManager.isInternetAvailable)

        setupNestedScroll()

        setTheme()
    }

    fun shareCVAndroidDailyGoal() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "type/palin"
        val textToShare = getString(R.string.share_daily_goal_text_on_social).replace(
            "{{*{{link}}*}}",
            "https://bit.ly/2XhnO7h"
        )
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, textToShare)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_daily_goal_title)))
    }

    private fun showMessageDialog(title: String, text: String, type: Int = 0) {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        //val width = metrics.widthPixels
        val height = metrics.heightPixels
        val msg = MessageDialog(this, type, title, text, details = "", height = height)
        msg.setListenActivity(this)
        msg.show()
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

    private fun animateProgressBar(dailyGoal: Int = 0, currentRecordingsValidations: Int = 0) {
        val view: View = binding.progressBarListen
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        //val height = metrics.heightPixels
        var newValue = 0

        if (dailyGoal == 0 || currentRecordingsValidations >= dailyGoal) {
            newValue = width
        } else {
            //currentRecordingsValidations : dailyGoal = X : 1 ==> currentRecordingsValidations / dailyGoal
            newValue =
                ((currentRecordingsValidations.toFloat() / dailyGoal.toFloat()) * width).toInt()
        }

        if (mainPrefManager.areAnimationsEnabled) {
            animationProgressBar(view.width, newValue)
        } else {
            view.layoutParams.width = newValue
            view.requestLayout()
        }
    }

    private fun animationProgressBar(min: Int, max: Int) {
        val view: View = binding.progressBarListen
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

    fun setTheme() = withBinding {
        theme.setElement(layoutListen)
        theme.setElement(this@ListenActivity, 1, listenSectionBottom)
        theme.setElement(
            this@ListenActivity,
            textMessageAlertListen,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
        theme.setElement(this@ListenActivity, buttonReportListen, background = false)
        theme.setElement(this@ListenActivity, buttonSkipListen)

        theme.setElement(
            this@ListenActivity,
            progressBarListen,
            R.color.colorPrimaryDark,
            R.color.colorLightGray
        )
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

        if (!listenViewModel.showSentencesTextAtTheEnd()) {
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

        resizeSentence()

        if (settingsPrefManager.showReportIcon) {
            showImage(imageReportIconListen)
        } else {
            buttonReportListen.isGone = false
        }

        buttonStartStopListen.isEnabled = true
        buttonStartStopListen.onClick {
            listenViewModel.startListening()
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
            if (listenViewModel.autoPlay() && !noAutoPlay) {
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
                in 0..10 -> resources.getDimension(R.dimen.title_very_big)
                in 11..20 -> resources.getDimension(R.dimen.title_big)
                in 21..40 -> resources.getDimension(R.dimen.title_medium)
                in 41..70 -> resources.getDimension(R.dimen.title_normal)
                else -> resources.getDimension(R.dimen.title_small)
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

        var showListeningSentence = true
        if (listenViewModel.showSentencesTextAtTheEnd() && !listenViewModel.listenedOnce) {
            textMessageAlertListen.text = getString(R.string.txt_sentence_feature_enabled).replace(
                "{{*{{feature_name}}*}}",
                getString(R.string.txt_show_sentence_at_the_ending)
            ) + "\n" + getString(
                R.string.txt_press_icon_below_listen_2
            )
            textSentenceListen.setText(R.string.txt_listening_clip)
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
            showButton(buttonNoClip)
        }
        if (!listenViewModel.listenedOnce) buttonYesClip.isVisible = false
        listenViewModel.startedOnce = true
        buttonSkipListen.isEnabled = true

        buttonStartStopListen.setBackgroundResource(R.drawable.stop_cv)

        buttonNoClip.onClick {
            listenViewModel.validate(result = false)
            numberSentThisSession++
            hideButtons()
        }
        buttonStartStopListen.onClick {
            listenViewModel.stopListening()
        }
    }

    private fun loadUIStateListened() = withBinding {
        buttonNoClip.isVisible = true
        textSentenceListen.text = listenViewModel.getSentenceText()
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
        }
        buttonStartStopListen.onClick {
            listenViewModel.startListening()
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

        super.onBackPressed()
    }

    private fun hideButtons() {
        stopButtons()
        if (listenViewModel.startedOnce) hideButton(binding.buttonNoClip)
        if (listenViewModel.listenedOnce) hideButton(binding.buttonYesClip)
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

}