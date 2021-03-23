package org.commonvoice.saverio.ui.theming

import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.*

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
            }
        }
        darkLightTheme.setElementDialogCL(viewBinding.root as ConstraintLayout)
    }
)

val DialyGoalDialogThemeHandler = SubsetThemeHandler(
    listOf(DialogDailyGoalBinding::class),
    operation = { viewBinding, darkLightTheme ->
        viewBinding as DialogDailyGoalBinding
        val context = viewBinding.root.context
        darkLightTheme.setElementDialogCL(viewBinding.root)

        darkLightTheme.setElement(
            context,
            viewBinding.labelTextAlertDailyGoalFeature,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT,
            textSize = 15f
        )

        darkLightTheme.setElement(
            context,
            viewBinding.subtitleMotivationDailyGoal,
            R.color.colorGray,
            R.color.colorLightGray,
            textSize = 15f
        )

        darkLightTheme.setElement(
            context,
            viewBinding.textMotivationDailyGoal,
            R.color.colorGray,
            R.color.colorLightGray,
            textSize = 15f
        )

        darkLightTheme.setElement(context, viewBinding.btnDailyGoalSave)
        darkLightTheme.setElement(context, viewBinding.btnDailyGoalCancel)
        darkLightTheme.setElement(context, viewBinding.btnDailyGoalDelete)

        darkLightTheme.setElement(context, viewBinding.labelTextDailyGoal)
        darkLightTheme.setElement(context, viewBinding.labelDailyGoalValue, transformText = false)

        darkLightTheme.setElement(context, viewBinding.seekDailyGoalValue)

        context.resources.displayMetrics.widthPixels.let {
            if (it < 1500) {
                viewBinding.btnDailyGoalDelete.isVisible = false
            }
        }
    }
)