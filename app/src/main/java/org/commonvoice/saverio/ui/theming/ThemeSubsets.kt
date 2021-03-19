package org.commonvoice.saverio.ui.theming

import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import org.commonvoice.saverio.databinding.DialogIdentifymeBinding
import org.commonvoice.saverio.databinding.DialogInfoBinding
import org.commonvoice.saverio.databinding.DialogStandardBinding
import org.commonvoice.saverio.databinding.DialogWarningMessageBinding

val GenericDialogThemeHandler = SubsetThemeHandler(
    listOf(
        DialogWarningMessageBinding::class,
        DialogIdentifymeBinding::class,
        DialogStandardBinding::class,
        DialogInfoBinding::class,
    ),
    operation = { viewBinding, darkLightTheme ->
        (viewBinding.root as ViewGroup).children.forEach {
            val viewContext = it.context
            when(it) {
                is Button -> darkLightTheme.setElement(viewContext, it)
                is TextView -> darkLightTheme.setElement(viewContext, it)
                is ImageView -> darkLightTheme.setElementDialogIV(it)
                /*is ConstraintLayout -> {
                    it.children.forEach { view ->
                        when(view) {
                            is ImageView -> darkLightTheme.setElementDialogIV(viewContext, view)
                            is TextView ->  darkLightTheme.setElementDialogTV(viewContext, view, transformText = false)
                        }
                    }
                }*/
            }
        }
        darkLightTheme.setElementDialogCL(viewBinding.root.context, viewBinding.root as ConstraintLayout)
    }
)