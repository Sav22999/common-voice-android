package org.commonvoice.saverio.ui.settings.nestedSettings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentUsefulLinksSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject

class UsefulLinksFragment : ViewBoundFragment<FragmentUsefulLinksSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUsefulLinksSettingsBinding {
        return FragmentUsefulLinksSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()

    override fun onStart() {
        super.onStart()

        withBinding {
            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsUsefulLinks.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            buttonBackSettingsSubSectionUsefulLinks.setOnClickListener {
                activity?.onBackPressed()
            }

            buttonTranslateApp.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://crowdin.com/project/common-voice-android")
                    )
                )
            }

            buttonSeeAppStatistics.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.saveriomorelli.com/commonvoice/statistics/")
                    )
                )
            }

            buttonTelegramGroupCVAndroid.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/common_voice_android")
                    )
                )
            }

            buttonReadContributionCriteria.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://commonvoice.mozilla.org/criteria")))
            }

            buttonTermsOfService.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mzl.la/3b0dN3R")))
            }

            buttonContactDeveloperOnTelegram.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Sav22999")))
            }

            buttonProjectOnGitHub.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Sav22999/common-voice-android")
                    )
                )
            }

            buttonCommonVoicePlaybook.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://common-voice.github.io/community-playbook/")
                    )
                )
            }

            buttonSentenceCollector.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://commonvoice.mozilla.org/sentence-collector/")))
            }
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsUsefulLinks)

            theme.setElements(requireContext(), settingsSectionUsefulLinks)

            theme.setElement(requireContext(), 3, settingsSectionUsefulLinks)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionUsefulLinks, textSize = 20F)
        }
    }
}