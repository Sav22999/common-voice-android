package org.commonvoice.saverio

import android.content.Context
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.tabs.TabLayout
import org.commonvoice.saverio_lib.preferences.MainPrefManager

class DarkLightTheme(
    private val mainPrefManager: MainPrefManager
) {

    var isDark: Boolean
        get() = mainPrefManager.isDarkThemeEnabled
        set(value) {
            mainPrefManager.isDarkThemeEnabled = value
        }

    fun setElements(context: Context, layout: ConstraintLayout) {
        setElement(layout)

        layout.children.forEach { child ->
            when (child) {
                is TextView -> setElement(context, child)
                is Button -> setElement(context, child)
                is SwitchCompat -> setElement(context, child)
            }
        }
    }

    fun setElement(
        element: ConstraintLayout,
        invert: Boolean = false
    ) {
        if (isDark xor invert) {
            element.setBackgroundResource(colorBackgroundDT)
        } else {
            element.setBackgroundResource(colorBackground)
        }
    }

    fun setElement(
        view: Context,
        top_or_bottom: Int,
        element: ConstraintLayout,
        background: Int = R.color.colorWhite,
        backgroundDT: Int = R.color.colorBlack,
        invert: Boolean = false
    ) {
        if (isDark xor invert) {
            //top_or_buttom = (border-radius) {-1: both, top and bottom WITH border, 1: just top | 2: just bottom | 3:both, top and bottom}
            if (top_or_bottom != -1) {
                when (top_or_bottom) {
                    1 -> {
                        element.setBackgroundResource(R.drawable.top_border_radius)
                    }
                    2 -> {
                        element.setBackgroundResource(R.drawable.bottom_border_radius)
                    }
                    else -> {
                        element.setBackgroundResource(R.drawable.top_bottom_border_radius)
                    }
                }
                element.backgroundTintList =
                    ContextCompat.getColorStateList(view, backgroundDT)
            } else {
                element.setBackgroundResource(R.drawable.txt_rounded_darktheme_with_border)
            }
        } else {
            if (top_or_bottom != -1) {
                when (top_or_bottom) {
                    1 -> {
                        element.setBackgroundResource(R.drawable.top_border_radius)
                    }
                    2 -> {
                        element.setBackgroundResource(R.drawable.bottom_border_radius)
                    }
                    else -> {
                        element.setBackgroundResource(R.drawable.top_bottom_border_radius)
                    }
                }
                element.backgroundTintList =
                    ContextCompat.getColorStateList(view, background)
            } else {
                element.setBackgroundResource(R.drawable.txt_rounded_with_border)
            }
        }
    }

    fun setElement(
        context: Context,
        element: TextView,
        background: Boolean = false,
        invert: Boolean = false
    ) {
        if (isDark xor invert) {
            if (background) {
                element.setBackgroundResource(R.color.colorBackgroundDT)
            } else {
                element.setBackgroundResource(R.color.colorTransparent)
            }
            element.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        } else {
            if (background) {
                element.setBackgroundResource(R.color.colorBackground)
            } else {
                element.setBackgroundResource(R.color.colorTransparent)
            }
            element.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        }
    }

    fun setElement(
        view: Context,
        element: TextView,
        color_light: Int,
        color_dark: Int,
        background: Boolean = false,
        invert: Boolean = false
    ) {
        if (isDark xor invert) {
            if (background) {
                element.setBackgroundResource(R.color.colorBackgroundDT)
            } else {
                element.setBackgroundResource(R.color.colorTransparent)
            }
            element.setTextColor(ContextCompat.getColor(view, color_dark))
        } else {
            if (background) {
                element.setBackgroundResource(R.color.colorBackground)
            } else {
                element.setBackgroundResource(R.color.colorTransparent)
            }
            element.setTextColor(ContextCompat.getColor(view, color_light))
        }
    }

    fun setTextView(
        view: Context,
        element: TextView,
        border: Boolean = true,
        darkTeme: Boolean = isDark,
        intern: Boolean = false
    ) {
        if (darkTeme) {
            if (border) {
                element.setBackgroundResource(R.drawable.txt_rounded_darktheme_with_border)
            } else {
                if (intern) {
                    element.setBackgroundResource(R.drawable.txt_rounded_darktheme_dashboard_intern)
                } else {
                    element.setBackgroundResource(R.drawable.txt_rounded_darktheme)
                    element.backgroundTintList =
                        ContextCompat.getColorStateList(view, R.color.colorBlack)
                }
            }
            element.setTextColor(ContextCompat.getColor(view, R.color.colorWhite))
            element.setHintTextColor(ContextCompat.getColor(view, R.color.colorAccentDT))
        } else {
            if (border) {

                element.setBackgroundResource(R.drawable.txt_rounded_with_border)
            } else {
                if (intern) {
                    element.setBackgroundResource(R.drawable.txt_rounded_dashboard_intern)
                } else {
                    element.setBackgroundResource(R.drawable.txt_rounded)
                    element.backgroundTintList =
                        ContextCompat.getColorStateList(view, R.color.colorWhite)
                }
            }
            element.setTextColor(ContextCompat.getColor(view, R.color.colorBlack))
            element.setHintTextColor(ContextCompat.getColor(view, R.color.colorAccent))
        }
    }

    fun setElement(view: Context, element: Button, invert: Boolean = false) {
        if (isDark xor invert) {
            element.setBackgroundResource(R.drawable.btn_rounded_darktheme)
            element.setTextColor(ContextCompat.getColor(view, R.color.colorBlack))
        } else {
            element.setBackgroundResource(R.drawable.btn_rounded)
            element.setTextColor(ContextCompat.getColor(view, R.color.colorWhite))
        }
    }

    fun setElement(element: ImageView, source_light: Int, source_dark: Int) {
        if (isDark) {
            element.setImageResource(source_dark)
        } else {
            element.setImageResource(source_light)
        }
    }

    fun setElement(context: Context, element: CheckBox) {
        if (isDark) {
            element.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            element.buttonTintList = ContextCompat.getColorStateList(context, R.color.colorWhite)
        } else {
            element.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            element.buttonTintList = ContextCompat.getColorStateList(context, R.color.colorBlack)
        }
    }

    fun setElement(context: Context, element: SwitchCompat) {
        if (isDark) {
            element.setBackgroundResource(colorBackgroundDT)
            element.setTextColor(ContextCompat.getColor(context, colorTextDT))
        } else {
            element.setBackgroundResource(colorBackground)
            element.setTextColor(ContextCompat.getColor(context, colorText))
        }
    }

    fun setElement(context: Context, element: SeekBar) {
        if (isDark) {
            element.progressTintList =
                ContextCompat.getColorStateList(context, R.color.colorAccentDT)
            element.progressBackgroundTintList =
                ContextCompat.getColorStateList(context, R.color.colorAccentDT)
            element.thumbTintList = ContextCompat.getColorStateList(context, R.color.colorWhite)
        } else {
            element.progressTintList = ContextCompat.getColorStateList(context, R.color.colorAccent)
            element.progressBackgroundTintList =
                ContextCompat.getColorStateList(context, R.color.colorAccent)
            element.thumbTintList = ContextCompat.getColorStateList(context, R.color.colorBlack)
        }
    }

    fun setElement(
        context: Context,
        element: SeekBar,
        color_light: Int,
        color_dark: Int
    ) {
        val colorStateList = if (isDark) {
            ContextCompat.getColorStateList(context, color_dark)
        } else {
            ContextCompat.getColorStateList(context, color_light)
        }

        element.progressTintList = colorStateList
        element.progressBackgroundTintList = colorStateList
        element.backgroundTintList = colorStateList
        element.foregroundTintList = colorStateList
        element.indeterminateTintList = colorStateList
        element.secondaryProgressTintList = colorStateList
    }

    companion object {

        private const val DARK_THEME = "DARK_THEME"
        private const val colorBackground: Int = R.color.colorBackground
        private const val colorBackgroundDT: Int = R.color.colorBackgroundDT
        private const val colorText: Int = R.color.colorBlack
        private const val colorTextDT: Int = R.color.colorWhite

        /*
    //these are for a future "Theme" feature:
    //(there is another class which set these colours eventually)
    //there won't exist anymore only "Light" and "Dark" theme
    private var colourBackgroundPrimary: Int = R.color.colorBackground
    private var colourBackgroundSecondary: Int = R.color.colorWhite
    private var colourBackgroundTertiary: Int = ?

    private var colourTextPrimary: Int = R.color.colorBlack
    private var colourTextSecondary: Int = R.color.colorBlack
    private var colourTextTertiary: Int = ?
    */

    }

}