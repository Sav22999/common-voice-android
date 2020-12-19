package org.commonvoice.saverio.ui.settings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import org.commonvoice.saverio.BuildConfig
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentNewSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewSettingsFragment : ViewBoundFragment<FragmentNewSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNewSettingsBinding {
        return FragmentNewSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager: MainPrefManager by inject()
    private val mainViewModel by viewModel<MainActivityViewModel>()
    private val workManager by inject<WorkManager>()
    private val dashboardViewModel by sharedViewModel<DashboardViewModel>()

    private val languagesListShort by lazy {
        resources.getStringArray(R.array.languages_short)
    }

    override fun onStart() {
        super.onStart()

        withBinding {
            buttonSettingsGoToAdvanced.onClick {
                findNavController().navigate(R.id.advancedSettingsFragment)
            }

            buttonSettingsGoToOther.onClick {
                findNavController().navigate(R.id.otherSettingsFragment)
            }

            buttonSettingsGoToExperimentalFeatures.onClick {
                findNavController().navigate(R.id.experimentalSettingsFragment)
            }

            buttonSettingsGoToGestures.onClick {
                findNavController().navigate(R.id.gesturesSettingsFragment)
            }

            buttonSettingsGoToListen.onClick {
                findNavController().navigate(R.id.listenSettingsFragment)
            }

            buttonSettingsGoToSpeak.onClick {
                findNavController().navigate(R.id.speakSettingsFragment)
            }

            buttonSettingsGoToUserInterface.onClick {
                findNavController().navigate(R.id.UISettingsFragment)
            }

            buttonSettingsGoToOfflineMode.onClick {
                findNavController().navigate(R.id.offlineModeSettingsFragment)
            }

            buttonSettingsGoToUsefulLinks.onClick {
                findNavController().navigate(R.id.usefulLinksFragment)
            }
        }

        setupLanguageSpinner()

        setupButtons()

        binding.textRelease.text = "${BuildConfig.VERSION_NAME} (build#${BuildConfig.VERSION_CODE})"

        binding.textDevelopedBy.setText(R.string.txt_developed_by)
    }

    private fun setupButtons() = withBinding {
        buttonBuyMeACoffee.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3aJnnq7")))
        }
    }

    private fun setupLanguageSpinner() {
        binding.languageList.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.languages)
        )

        binding.languageList.setSelection(languagesListShort.indexOf(mainPrefManager.language))

        binding.languageList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLanguage = languagesListShort[position]

                if (selectedLanguage != mainPrefManager.language) {
                    mainPrefManager.language = selectedLanguage

                    mainViewModel.clearDB().invokeOnCompletion {
                        SentencesDownloadWorker.attachOneTimeJobToWorkManager(
                            workManager,
                            ExistingWorkPolicy.REPLACE
                        )
                        ClipsDownloadWorker.attachOneTimeJobToWorkManager(
                            workManager,
                            ExistingWorkPolicy.REPLACE
                        )

                        mainPrefManager.hasLanguageChanged = true

                        (activity as? MainActivity)?.setLanguageUI("restart")
                    }
                    dashboardViewModel.lastStatsUpdate = 0
                }
            }
        }
    }

}