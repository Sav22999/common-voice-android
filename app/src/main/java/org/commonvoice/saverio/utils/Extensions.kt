package org.commonvoice.saverio.utils

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun MaterialAlertDialog(ctx: Context, params: MaterialAlertDialogBuilder.() -> Unit): AlertDialog {
    return MaterialAlertDialogBuilder(ctx).apply(params).show()
}

inline fun <T: View> T.onClick(crossinline action: () -> Unit) {
    this.setOnClickListener { action.invoke() }
}