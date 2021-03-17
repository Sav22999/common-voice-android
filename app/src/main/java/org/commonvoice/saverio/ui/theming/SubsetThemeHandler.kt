package org.commonvoice.saverio.ui.theming

import androidx.viewbinding.ViewBinding
import org.commonvoice.saverio.DarkLightTheme
import kotlin.reflect.KClass
import kotlin.reflect.KType

data class SubsetThemeHandler(
    val vbApplicableSubset: List<KClass<*>>,
    val operation: ((ViewBinding, DarkLightTheme) -> Unit)
)