package org.commonvoice.saverio.ui.settings.nestedSettings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import org.commonvoice.saverio.databinding.FragmentGesturesSettingsBinding
import org.commonvoice.saverio.ui.dialogs.CustomiseGesturesListenDialogFragment
import org.commonvoice.saverio.ui.dialogs.CustomiseGesturesSpeakDialogFragment
import org.commonvoice.saverio.ui.dialogs.ListenReportDialogFragment
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject

class GesturesSettingsFragment : ViewBoundFragment<FragmentGesturesSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGesturesSettingsBinding {
        return FragmentGesturesSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()

    override fun onStart() {
        super.onStart()

        withBinding {
            buttonBackSettingsSubSectionGestures.setOnClickListener {
                activity?.onBackPressed()
            }

            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsGestures.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            switchSettingsSubSectionGestures.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areGesturesEnabled = isChecked
                settingsSectionGesturesSubSpeak.isGone = !isChecked
                settingsSectionGesturesSubListen.isGone = !isChecked

                if (isChecked) {
                    nestedScrollSettingsGestures.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }
                } else {
                    nestedScrollSettingsGestures.setupOnSwipeRight(requireContext()) { }
                }
            }
            switchSettingsSubSectionGestures.isChecked = mainPrefManager.areGesturesEnabled
            settingsSectionGesturesSubSpeak.isGone = !mainPrefManager.areGesturesEnabled
            settingsSectionGesturesSubListen.isGone = !mainPrefManager.areGesturesEnabled

            buttonGesturesLearnMore.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/3phQ0lP")))
            }

            buttonGesturesSpeakSwipeRight.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_RIGHT"
                )
            }
            buttonGesturesSpeakSwipeLeft.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_LEFT"
                )
            }
            buttonGesturesSpeakSwipeUp.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_UP"
                )
            }
            buttonGesturesSpeakSwipeDown.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_DOWN"
                )
            }
            buttonGesturesSpeakLongPress.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_LONG_PRESS"
                )
            }
            buttonGesturesSpeakDoubleTap.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_DOUBLE_TAP"
                )
            }

            buttonGesturesListenSwipeRight.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_RIGHT"
                )
            }
            buttonGesturesListenSwipeLeft.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_LEFT"
                )
            }
            buttonGesturesListenSwipeUp.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_UP"
                )
            }
            buttonGesturesListenSwipeDown.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_DOWN"
                )
            }
            buttonGesturesListenLongPress.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_LONG_PRESS"
                )
            }
            buttonGesturesListenDoubleTap.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_DOUBLE_TAP"
                )
            }
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsGestures)

            theme.setElements(requireContext(), settingsSectionGesturesSubSpeak)
            theme.setElements(requireContext(), settingsSectionGesturesSubListen)
            theme.setElements(requireContext(), settingsSectionGestures)

            theme.setElement(requireContext(), 3, settingsSectionGesturesSubSpeak)
            theme.setElement(requireContext(), 3, settingsSectionGesturesSubListen)
            theme.setElement(requireContext(), 3, settingsSectionGestures)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionGestures, textSize = 20F)
        }
    }
}