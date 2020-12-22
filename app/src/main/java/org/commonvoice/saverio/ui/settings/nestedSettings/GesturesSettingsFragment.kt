package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.work.ExistingWorkPolicy
import kotlinx.android.synthetic.main.fragment_gestures_settings.*
import org.commonvoice.saverio.databinding.FragmentGesturesSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
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

        buttonBackSettingsSubSectionGestures.setOnClickListener {
            activity?.onBackPressed()
        }

        withBinding {
            switchSettingsSubSectionGestures.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areGesturesEnabled = isChecked
            }
            switchSettingsSubSectionGestures.isChecked = mainPrefManager.areGesturesEnabled
        }
    }
}