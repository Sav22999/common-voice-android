package org.commonvoice.saverio.ui.settings.nestedSettings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.fragment_offline_settings.*
import org.commonvoice.saverio.databinding.FragmentOfflineSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OfflineModeSettingsFragment : ViewBoundFragment<FragmentOfflineSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOfflineSettingsBinding {
        return FragmentOfflineSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()
    private val listenPrefManager by inject<ListenPrefManager>()
    private val mainViewModel by viewModel<MainActivityViewModel>()
    private val workManager by inject<WorkManager>()

    override fun onStart() {
        super.onStart()

        buttonBackSettingsSubSectionOfflineMode.setOnClickListener {
            activity?.onBackPressed()
        }

        withBinding {
            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsOfflineMode.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            val oldStatus = settingsPrefManager.isOfflineMode
            switchSettingsSubSectionOfflineMode.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.isOfflineMode = isChecked
                if (oldStatus != isChecked) {
                    var count = 50
                    if (!settingsPrefManager.isOfflineMode)
                        count = 3
                    listenPrefManager.requiredClipsCount = count
                    speakPrefManager.requiredSentencesCount = count
                    mainViewModel.clearDB().invokeOnCompletion {
                        SentencesDownloadWorker.attachOneTimeJobToWorkManager(
                            workManager,
                            ExistingWorkPolicy.REPLACE
                        )
                        ClipsDownloadWorker.attachOneTimeJobToWorkManager(
                            workManager,
                            ExistingWorkPolicy.REPLACE
                        )
                    }
                }
            }
            switchSettingsSubSectionOfflineMode.isChecked = settingsPrefManager.isOfflineMode

            buttonOfflineModeLearnMore.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/3nJwpuq")))
            }
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsOfflineMode)

            theme.setElements(requireContext(), settingsSectionOfflineMode)

            theme.setElement(requireContext(), 3, settingsSectionOfflineMode)
        }
    }

}