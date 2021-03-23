package org.commonvoice.saverio.ui.theming

import androidx.viewbinding.ViewBinding
import org.commonvoice.saverio.DarkLightTheme
import timber.log.Timber

class ThemingEngine(
    val darkLightTheme: DarkLightTheme
) {

    val themeHandlers: List<SubsetThemeHandler> = listOf(
        GenericDialogThemeHandler,
        DialyGoalDialogThemeHandler
    )

    inline fun <reified T: ViewBinding> applyTheme(view: T) {
        themeHandlers.forEach { handler ->
            Timber.i("Class: ${view::class}, applicableSubsets: ${handler.vbApplicableSubset}")
            if (view::class in handler.vbApplicableSubset) {
                handler.operation(view, darkLightTheme)
            }
        }
    }

}