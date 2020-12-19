package org.commonvoice.saverio.ui.settings.nestedSettings

import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.widget.CompoundButtonCompat
import org.commonvoice.saverio.databinding.FragmentUiSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class UISettingsFragment : ViewBoundFragment<FragmentUiSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUiSettingsBinding {
        return FragmentUiSettingsBinding.inflate(layoutInflater, container, false)
    }

    override fun onStart() {
        super.onStart()

        withBinding {
            addPaddingRadio(buttonThemeLight)
            addPaddingRadio(buttonThemeDark)
            addPaddingRadio(buttonThemeAuto)
        }
    }

    private fun addPaddingRadio(radioButton: RadioButton) {
        val compoundButtonDrawable = CompoundButtonCompat.getButtonDrawable(radioButton)
        val insetDrawable = InsetDrawable(compoundButtonDrawable, 40, 0, 0, 0)
        radioButton.buttonDrawable = insetDrawable
    }
}