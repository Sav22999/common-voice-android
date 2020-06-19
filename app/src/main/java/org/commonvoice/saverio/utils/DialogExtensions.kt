package org.commonvoice.saverio.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

@Suppress("FunctionName")
fun AlertDialog(
    ctx: Context,
    @LayoutRes layout: Int? = null,
    params: (AlertDialog.Builder.() -> Unit)? = null
): AlertDialog {
    return AlertDialog.Builder(ctx).apply {
        if (layout != null) setView(layout)
        if (params != null) params()
    }.show()
}

fun AlertDialog.makeRootViewTransparent() {
    this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

fun AlertDialog.setupViews(setup: (AlertDialog.() -> Unit)) {
    this.apply(setup)
}

fun AlertDialog.withButton(
    @IdRes id: Int,
    onClick: ((Button) -> Unit)? = null
): Button {
    return this.findViewById<Button>(id)!!.also {
        it.onClick { onClick?.invoke(it) }
    }
}

fun AlertDialog.withTextView(
    @IdRes id: Int,
    @StringRes textId: Int? = null,
    textLiteral: String? = null
): TextView {
    return this.findViewById<TextView>(id)!!.also {
        if (textId != null) it.setText(textId)
        if (textLiteral != null) it.text = textLiteral
    }
}

fun <T: View> AlertDialog.getView(
    @IdRes id: Int
): T = this.findViewById<T>(id)!!