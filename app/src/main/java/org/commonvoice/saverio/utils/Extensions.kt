package org.commonvoice.saverio.utils

import android.view.View

inline fun <T: View> T.onClick(crossinline action: () -> Unit) {
    this.setOnClickListener { action.invoke() }
}