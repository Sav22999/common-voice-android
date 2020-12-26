package org.commonvoice.saverio.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView

inline fun <T: View> T.onClick(crossinline action: () -> Unit) {
    this.setOnClickListener { action.invoke() }
}

inline fun <reified T> ViewGroup.inflateBinding(
    func: (LayoutInflater, ViewGroup?, Boolean) -> T
): T {
    return func(
        LayoutInflater.from(this.context),
        this,
        false
    )
}

fun NestedScrollView.setupOnSwipeRight(context: Context, action: () -> Unit) =
    setOnTouchListener(object : OnSwipeTouchListener(context) {
        override fun onSwipeRight() {
            action()
        }
    })