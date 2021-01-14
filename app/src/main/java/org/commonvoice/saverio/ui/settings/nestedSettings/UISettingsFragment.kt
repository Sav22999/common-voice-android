package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.android.synthetic.main.activity_main.*
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentUiSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.koin.android.ext.android.inject

class UISettingsFragment : ViewBoundFragment<FragmentUiSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUiSettingsBinding {
        return FragmentUiSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val settingsPrefManager by inject<SettingsPrefManager>()

    override fun onStart() {
        super.onStart()

        withBinding {
            buttonBackSettingsSubSectionUI.setOnClickListener {
                activity?.onBackPressed()
            }

            switchShowIconTopRightInsteadButton.text =
                getString(R.string.txt_show_report_icon_instead_of_button).replace(
                    "{{*{{listen_name}}*}}",
                    getString(R.string.settingsListen)
                ).replace("{{*{{speak_name}}*}}", getString(R.string.settingsSpeak))

            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsUI.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            radioGroupTheme.check(
                when(mainPrefManager.themeType) {
                    "dark" -> R.id.buttonThemeDark
                    "auto" -> R.id.buttonThemeAuto
                    else -> R.id.buttonThemeLight
                }
            )

            radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
                mainPrefManager.themeType = when(checkedId) {
                    R.id.buttonThemeDark -> "dark"
                    R.id.buttonThemeAuto -> "auto"
                    R.id.buttonThemeLight -> "light"
                    else -> ""
                }
                setTheme()
            }

            switchAnimations.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areAnimationsEnabled = isChecked
            }
            switchAnimations.isChecked = mainPrefManager.areAnimationsEnabled

            switchShowLabels.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areLabelsBelowMenuIcons = isChecked
                if (isChecked) {
                    (activity as MainActivity).nav_view.labelVisibilityMode =
                        LabelVisibilityMode.LABEL_VISIBILITY_LABELED
                } else {
                    (activity as MainActivity).nav_view.labelVisibilityMode =
                        LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
                }
            }
            switchShowLabels.isChecked = mainPrefManager.areLabelsBelowMenuIcons

            switchShowIconTopRightInsteadButton.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.showReportIcon = isChecked
            }
            switchShowIconTopRightInsteadButton.isChecked = settingsPrefManager.showReportIcon
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsUserInterface)

            theme.setElements(requireContext(), settingsSectionUIGeneric)
            theme.setElements(requireContext(), settingsSectionTheme)

            theme.setElement(requireContext(), 3, settingsSectionUIGeneric)
            theme.setElement(requireContext(), 3, settingsSectionTheme)

            theme.setElement(requireContext(), buttonThemeLight)
            theme.setElement(requireContext(), buttonThemeDark)
            theme.setElement(requireContext(), buttonThemeAuto)
        }
    }
}