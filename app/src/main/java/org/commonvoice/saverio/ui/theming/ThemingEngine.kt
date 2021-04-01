package org.commonvoice.saverio.ui.theming

import androidx.viewbinding.ViewBinding
import org.commonvoice.saverio.DarkLightTheme

class ThemingEngine(
    val darkLightTheme: DarkLightTheme
) {

    val themeHandlers: List<SubsetThemeHandler> = listOf(
        GenericDialogThemeHandler,
        DialyGoalDialogThemeHandler
    )

    inline fun <reified T: ViewBinding> applyTheme(view: T) {
        themeHandlers.forEach { handler ->
            if (view::class in handler.vbApplicableSubset) {
                handler.operation(view, darkLightTheme)
            }
        }
    }

}