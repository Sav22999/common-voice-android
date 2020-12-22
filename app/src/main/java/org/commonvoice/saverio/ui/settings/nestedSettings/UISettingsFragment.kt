package org.commonvoice.saverio.ui.settings.nestedSettings

import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.widget.CompoundButtonCompat
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_ui_settings.*
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.databinding.FragmentUiSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject

class UISettingsFragment : ViewBoundFragment<FragmentUiSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUiSettingsBinding {
        return FragmentUiSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()

    override fun onStart() {
        super.onStart()

        buttonBackSettingsSubSectionUI.setOnClickListener {
            activity?.onBackPressed()
        }

        addPaddingRadio(buttonThemeLight)
        addPaddingRadio(buttonThemeDark)
        addPaddingRadio(buttonThemeAuto)

        withBinding {
            buttonThemeLight.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mainPrefManager.themeType = "light"
                }
            }
            buttonThemeDark.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mainPrefManager.themeType = "dark"
                }
            }
            buttonThemeAuto.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mainPrefManager.themeType = "auto"
                }
            }
            if (mainPrefManager.themeType == "light") {
                buttonThemeLight.isChecked = true
            } else if (mainPrefManager.themeType == "dark") {
                buttonThemeDark.isChecked = true
            } else {
                buttonThemeAuto.isChecked = true
            }
            //TODO: set the new theme also in Settings

            switchAnimations.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areAnimationsEnabled = isChecked
            }
            switchAnimations.isChecked = mainPrefManager.areAnimationsEnabled

            switchShowLabels.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areLabelsBelowMenuIcons = isChecked
                if (isChecked) {
                    (activity as MainActivity).nav_view.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
                } else {
                    (activity as MainActivity).nav_view.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
                }
            }
            switchShowLabels.isChecked = mainPrefManager.areLabelsBelowMenuIcons
        }
    }

    private fun addPaddingRadio(radioButton: RadioButton) {
        val compoundButtonDrawable = CompoundButtonCompat.getButtonDrawable(radioButton)
        val insetDrawable = InsetDrawable(compoundButtonDrawable, 40, 0, 0, 0)
        radioButton.buttonDrawable = insetDrawable
    }
}