package org.commonvoice.saverio.ui.settings.nestedSettings

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.viewmodels.GenericViewModel
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentOfflineSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OfflineModeSettingsFragment : ViewBoundFragment<FragmentOfflineSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater, container: ViewGroup?
    ): FragmentOfflineSettingsBinding {
        return FragmentOfflineSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()
    private val listenPrefManager by inject<ListenPrefManager>()
    private val mainViewModel by viewModel<MainActivityViewModel>()
    private val workManager by inject<WorkManager>()
    private val listenViewModel: ListenViewModel by stateViewModel()
    private val speakViewModel: SpeakViewModel by stateViewModel()

    private var changedNumber = false

    private val minimumOfflineModeNumber = 10
    private val stepsOfflineMode = 10

    private var startup = true

    override fun onStart() {
        super.onStart()

        withBinding {
            val viewModel = activity?.run {
                ViewModelProviders.of(this).get(GenericViewModel::class.java)
            } ?: throw Exception("?? Invalid Activity ??")
            if (viewModel.fromFragment.value != "settings") activity?.onBackPressed()

            buttonBackSettingsSubSectionOfflineMode.setOnClickListener {
                activity?.onBackPressed()
            }

            if (mainPrefManager.areGesturesEnabled) nestedScrollSettingsOfflineMode.setupOnSwipeRight(
                requireContext()
            ) { activity?.onBackPressed() }


            val oldStatus = settingsPrefManager.isOfflineMode
            switchSettingsSubSectionOfflineMode.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.isOfflineMode = isChecked
                settingsSectionCustomiseOfflineMode.isGone = !isChecked
                if (oldStatus != isChecked) {
                    var count = 50
                    if (!settingsPrefManager.isOfflineMode) count = 3
                    listenPrefManager.requiredClipsCount = count
                    speakPrefManager.requiredSentencesCount = count
                }
                var count = if (listenPrefManager.requiredClipsCount >= minimumOfflineModeNumber) {
                    listenPrefManager.requiredClipsCount
                } else {
                    50
                }

                if (isChecked) {
                    listenPrefManager.requiredClipsCount = count
                    speakPrefManager.requiredSentencesCount = count
                    showCustomisationSection()
                } else {
                    count = 3
                    listenPrefManager.requiredClipsCount = count
                    speakPrefManager.requiredSentencesCount = count
                }

                showHideProgressBar(!isChecked)

                if (startup) startup = false
            }
            switchSettingsSubSectionOfflineMode.isChecked = settingsPrefManager.isOfflineMode

            buttonOfflineModeLearnMore.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.saveriomorelli.com/commonvoice/offline-mode/")
                    )
                )
            }


        }

        checkProgressBar()

        setTheme()
    }

    private fun showHideProgressBar(isGone: Boolean) {
        withBinding {
            progressBarOfflineModeListen.isGone = isGone
            progressBarOfflineModeSpeak.isGone = isGone
        }
    }

    private fun checkProgressBar() {
        withBinding {
            lifecycleScope.launch {
                /*animateProgressBar(
                    progressBarOfflineModeListen,
                    listenViewModel.getClipsCount(),
                    listenPrefManager.requiredClipsCount
                )
                animateProgressBar(
                    progressBarOfflineModeSpeak,
                    speakViewModel.getSentencesCount(),
                    speakPrefManager.requiredSentencesCount
                )
                 */


                listenViewModel.clipsRepository.getLiveClipsCount()
                    .observe(viewLifecycleOwner, Observer {
                        animateProgressBar(
                            progressBarOfflineModeListen,
                            it,
                            listenPrefManager.requiredClipsCount
                        )
                    })

                speakViewModel.sentencesRepository.getLiveSentenceCount()
                    .observe(viewLifecycleOwner, Observer {
                        animateProgressBar(
                            progressBarOfflineModeSpeak,
                            it,
                            speakPrefManager.requiredSentencesCount
                        )
                    })

                Handler().postDelayed({ checkProgressBar() }, 15000)
            }
        }
    }

    private fun showCustomisationSection() {
        withBinding {
            setSeekBar((speakPrefManager.requiredSentencesCount - minimumOfflineModeNumber).toFloat())
            seekOfflineModeValue.progress =
                speakPrefManager.requiredSentencesCount - minimumOfflineModeNumber
            seekOfflineModeValue.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar, progress: Int, fromUser: Boolean
                ) {
                    //onProgress
                    setSeekBar(seek.progress.toFloat())
                }

                override fun onStartTrackingTouch(seek: SeekBar) {
                    //onStart
                }

                override fun onStopTrackingTouch(seek: SeekBar) {
                    //onStop
                    setSeekBar(seek.progress.toFloat())
                }
            })
        }
    }

    private fun setSeekBar(value: Float) {
        val valueToUse = (value - value % stepsOfflineMode).toInt() + minimumOfflineModeNumber
        withBinding {
            labelOfflineModeValue.text = valueToUse.toString()
            listenPrefManager.requiredClipsCount = valueToUse
            speakPrefManager.requiredSentencesCount = valueToUse

            if (!startup) changedNumber = true
        }
        checkProgressBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (changedNumber) {
            mainViewModel.clearDB().invokeOnCompletion {
                SentencesDownloadWorker.attachOneTimeJobToWorkManager(
                    workManager, ExistingWorkPolicy.APPEND_OR_REPLACE
                )
                ClipsDownloadWorker.attachOneTimeJobToWorkManager(
                    workManager, ExistingWorkPolicy.APPEND_OR_REPLACE
                )
            }
        }
    }

    private fun animateProgressBar(
        progressBar: View, downloaded: Int, total: Int
    ) {
        val view: View = progressBar
        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        val width = metrics.widthPixels
        //val height = metrics.heightPixels

        if (!settingsPrefManager.isOfflineMode || downloaded == 0 || total == 0) {
            view.isGone = true
        } else {
            val widthToUse = (downloaded * width) / total
            animationProgressBar(view, view.x.toInt(), widthToUse)
            progressBar.isGone = false
        }
    }

    private fun animationProgressBar(
        progressBar: View,
        min: Int,
        max: Int
    ) {
        val view: View = progressBar
        val animation: ValueAnimator = ValueAnimator.ofInt(min, max)
        animation.duration = 1000
        animation.addUpdateListener { anim ->
            val value = anim.animatedValue as Int
            view.layoutParams.width = value
            view.requestLayout()
        }
        animation.start()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsOfflineMode)

            theme.setElements(requireContext(), settingsSectionLearnMoreOfflineMode)
            theme.setElements(requireContext(), settingsSectionCustomiseOfflineMode)

            theme.setElement(requireContext(), 3, settingsSectionLearnMoreOfflineMode)
            theme.setElement(requireContext(), 3, settingsSectionCustomiseOfflineMode)

            theme.setElement(requireContext(), seekOfflineModeValue)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionOfflineMode, textSize = 20F)

            theme.setElement(
                requireContext(),
                subtitleMotivationOfflineMode,
                R.color.colorGray,
                R.color.colorLightGray,
                textSize = 15f
            )

            theme.setElement(
                requireContext(),
                textMotivationOfflineMode,
                R.color.colorGray,
                R.color.colorLightGray,
                textSize = 15f
            )
        }
    }

}