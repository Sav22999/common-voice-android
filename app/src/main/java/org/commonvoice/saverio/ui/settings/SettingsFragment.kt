package org.commonvoice.saverio.ui.settings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import org.commonvoice.saverio.BuildConfig
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.TranslationHandler
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : ViewBoundFragment<FragmentSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager: MainPrefManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()
    private val mainViewModel by viewModel<MainActivityViewModel>()
    private val workManager by inject<WorkManager>()
    private val dashboardViewModel by sharedViewModel<DashboardViewModel>()
    private val translationHandler by inject<TranslationHandler>()

    private var SOURCE_STORE: String = ""

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

            SOURCE_STORE = MainActivity.SOURCE_STORE
        }

        setupLanguageSpinner()

        setupButtons()

        binding.textRelease.text =
            "${BuildConfig.VERSION_NAME} (build#${BuildConfig.VERSION_CODE}::${MainActivity.SOURCE_STORE})"

        binding.textDevelopedBy.setText(R.string.txt_developed_by)

        //TODO improve this method
        (activity as MainActivity).resetStatusBarColor()

        setTheme()
    }

    private fun setupButtons() = withBinding {
        if (SOURCE_STORE == "GPS") {
            if (!mainPrefManager.isAlpha && !mainPrefManager.isBeta) {
                buttonReviewOnGooglePlay.isGone = false
                separator37.isGone = false
            }

        }
        if (SOURCE_STORE == "GPS" || SOURCE_STORE == "HAG") {
            //TODO: remove, when implemented In-app purchare:
            buttonBuyMeACoffee.isGone = true
            separator28.isGone = true
            if (statsPrefManager.buyMeACoffeeCounter >= 20) {
                buttonBuyMeACoffee.isGone = false
                separator28.isGone = false
            }
        }

        //In-App review
        //TODO: doesn't appear the flow "pop-up"
        /*val manager = ReviewManagerFactory.create(requireContext())*/
        buttonReviewOnGooglePlay.setOnClickListener {
            /*
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = request.result

                    val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                } else {*/
            //some errors, so the app can't open the in-app review pop-up
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=org.commonvoice.saverio")
                )
            )
            /*}
        }*/
        }

        //In-App purchase
        //TODO: remove as comments when it's implemented
        /*
        if (SOURCE_STORE == "GPS") {
            inAppPurchase()
        } else {*/
        buttonBuyMeACoffee.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3aJnnq7")))
        }
        /*}*/
    }

    private fun inAppPurchase() {
        //TODO In-App purchase
    }

    private fun setupLanguageSpinner() {
        binding.languageList.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            translationHandler.availableLanguageNames
        )

        binding.languageList.setSelection(
            translationHandler.availableLanguageCodes.indexOf(
                mainPrefManager.language
            )
        )

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
                val selectedLanguage = translationHandler.availableLanguageCodes[position]

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

                        mainPrefManager.hasLanguageChanged = false
                        mainPrefManager.hasLanguageChanged2 = true

                        (activity as? MainActivity)?.setLanguageUI("restart")
                    }
                    dashboardViewModel.lastStatsUpdate = 0
                }
            }
        }
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettings)

            theme.setElements(requireContext(), settingsSectionLanguage)
            theme.setElements(requireContext(), settingsSectionGeneral)
            theme.setElements(requireContext(), newSettingsSectionOther)
            theme.setElements(requireContext(), settingsSectionBottom)

            theme.setElement(requireContext(), 3, settingsSectionLanguage)
            theme.setElement(requireContext(), 3, settingsSectionGeneral)
            theme.setElement(requireContext(), 3, newSettingsSectionOther)
            theme.setElement(requireContext(), 1, settingsSectionBottom)

            theme.setElement(requireContext(), textDevelopedBy, textSize = 15F, background = false)
            theme.setElement(requireContext(), textDeveloper, textSize = 25F, background = false)
            theme.setElement(requireContext(), textRelease, textSize = 12F, background = false)
        }
    }
}