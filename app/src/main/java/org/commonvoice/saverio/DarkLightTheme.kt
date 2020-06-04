package org.commonvoice.saverio

import android.content.Context
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout

class DarkLightTheme {
    private var PRIVATE_MODE = 0
    private val DARK_THEME = "DARK_THEME"
    private var isDark = false
    private var colorBackground: Int = R.color.colorBackground
    private var colorBackgroundDT: Int = R.color.colorBackgroundDT
    private var colorText: Int = R.color.colorBlack
    private var colorTextDT: Int = R.color.colorWhite

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


    constructor() {

    }

    fun setColours(colorBackground: Int, colorBackgroundDT: Int, colorText: Int, colorTextDT: Int) {
        this.colorBackground = colorBackground
        this.colorBackgroundDT = colorBackgroundDT
        this.colorText = colorText
        this.colorTextDT = colorTextDT
    }

    fun setTheme(view: Context, status: Boolean) {
        view.getSharedPreferences(DARK_THEME, PRIVATE_MODE).edit()
            .putBoolean(DARK_THEME, status).apply()
        this.getTheme(view)
    }

    fun getTheme(view: Context): Boolean {
        return view.getSharedPreferences(DARK_THEME, PRIVATE_MODE).getBoolean(DARK_THEME, false)
    }

    fun setElements(view: Context, layout: ConstraintLayout) {
        this.isDark = this.getTheme(view)

        var count = 0
        var activity = layout
        this.setElement(this.isDark, activity)
        while (count < activity.childCount) {
            var el = activity.getChildAt(count)
            if (el is TextView) {
                this.setElement(this.isDark, view, el)
                //tln("TextView")
            } else if (el is Button) {
                this.setElement(this.isDark, view, el)
                //println("Button")
            } else if (el is Spinner) {
                //println("Spinner")
            } else if (el is ImageView) {
                //println("ImageView")
            } else if (el is View) {
                //println("View")
            } else if (el is Switch) {
                setElement(isDark, view, el as Switch)
                //println("Switch")//it doesn't found anything (?)
            } else {
                //println("Other found:" + el.id)
            }
            count++
        }
    }

    fun setElement(theme: Boolean, element: ConstraintLayout) {
        if (theme) {
            element.setBackgroundResource(this.colorBackgroundDT)
        } else {
            element.setBackgroundResource(this.colorBackground)
        }
    }

    fun setElement(theme: Boolean, view: Context, top_or_bottom: Int, element: ConstraintLayout) {
        if (theme) {
            //top_or_buttom = {1: (radius) just top | 2: just bottom | 3:both, top and bottom}
            if (top_or_bottom == 1) {
                element.setBackgroundResource(R.drawable.top_border_radius)
            } else if (top_or_bottom == 2) {
                element.setBackgroundResource(R.drawable.bottom_border_radius)
            } else {
                element.setBackgroundResource(R.drawable.top_bottom_border_radius)
            }
            element.backgroundTintList = ContextCompat.getColorStateList(view, R.color.colorBlack)
        } else {
            if (top_or_bottom == 1) {
                element.setBackgroundResource(R.drawable.top_border_radius)
            } else if (top_or_bottom == 2) {
                element.setBackgroundResource(R.drawable.bottom_border_radius)
            } else {
                element.setBackgroundResource(R.drawable.top_bottom_border_radius)
            }
            element.backgroundTintList = ContextCompat.getColorStateList(view, R.color.colorWhite)
        }
    }

    fun setElement(theme: Boolean, view: Context, element: TextView, background: Boolean = false) {
        if (theme) {
            if (background) {
                element.setBackgroundResource(R.color.colorBackgroundDT)
            } else {
                element.setBackgroundResource(R.color.colorTransparent)
            }
            element.setTextColor(ContextCompat.getColor(view, R.color.colorWhite))
        } else {
            if (background) {
                element.setBackgroundResource(R.color.colorBackground)
            } else {
                element.setBackgroundResource(R.color.colorTransparent)
            }
            element.setTextColor(ContextCompat.getColor(view, R.color.colorBlack))
        }
    }

    fun setElement(
        theme: Boolean,
        view: Context,
        element: TextView,
        color_light: Int,
        color_dark: Int
    ) {
        if (theme) {
            element.setBackgroundResource(R.color.colorBackgroundDT)
            element.setTextColor(ContextCompat.getColor(view, color_dark))
        } else {
            element.setBackgroundResource(R.color.colorBackground)
            element.setTextColor(ContextCompat.getColor(view, color_light))
        }
    }

    fun setTextView(theme: Boolean, view: Context, element: TextView) {
        if (theme) {
            element.setBackgroundResource(R.drawable.txt_rounded_darktheme_with_border)
            element.setTextColor(ContextCompat.getColor(view, R.color.colorWhite))
            element.setHintTextColor(ContextCompat.getColor(view, R.color.colorAccentDT))
        } else {
            element.setBackgroundResource(R.drawable.txt_rounded_with_border)
            element.setTextColor(ContextCompat.getColor(view, R.color.colorBlack))
            element.setHintTextColor(ContextCompat.getColor(view, R.color.colorAccent))
        }
    }

    fun setElement(theme: Boolean, view: Context, element: Button) {
        if (theme) {
            element.setBackgroundResource(R.drawable.btn_rounded_darktheme)
            element.setTextColor(ContextCompat.getColor(view, R.color.colorBlack))
        } else {
            element.setBackgroundResource(R.drawable.btn_rounded)
            element.setTextColor(ContextCompat.getColor(view, R.color.colorWhite))
        }
    }

    fun setElement(theme: Boolean, element: ImageView, source_light: Int, source_dark: Int) {
        if (theme) {
            element.setImageResource(source_dark)
        } else {
            element.setImageResource(source_light)
        }
    }

    fun setElement(theme: Boolean, element: Spinner) {
        if (theme) {
            //element.setBackgroundResource(R.drawable.txt_rounded_darktheme)
        } else {
            //element.setBackgroundResource(R.drawable.txt_rounded)
        }
    }

    fun setElement(theme: Boolean, view: Context, element: CheckBox) {
        if (theme) {
            element.setTextColor(ContextCompat.getColor(view, R.color.colorWhite))
            element.buttonTintList = ContextCompat.getColorStateList(view, R.color.colorWhite)
        } else {
            element.setTextColor(ContextCompat.getColor(view, R.color.colorBlack))
            element.buttonTintList = ContextCompat.getColorStateList(view, R.color.colorBlack)
        }
    }

    fun setElement(theme: Boolean, view: Context, element: Switch) {
        if (theme) {
            element.setBackgroundResource(this.colorBackgroundDT)
            element.setTextColor(ContextCompat.getColor(view, this.colorTextDT))
        } else {
            element.setBackgroundResource(this.colorBackground)
            element.setTextColor(ContextCompat.getColor(view, colorText))
        }
    }

    fun setElement(theme: Boolean, view: Context, element: SeekBar) {
        if (theme) {
            element.progressTintList = ContextCompat.getColorStateList(view, R.color.colorAccentDT)
            element.progressBackgroundTintList =
                ContextCompat.getColorStateList(view, R.color.colorAccentDT)
            element.thumbTintList = ContextCompat.getColorStateList(view, R.color.colorWhite)
        } else {
            element.progressTintList = ContextCompat.getColorStateList(view, R.color.colorAccent)
            element.progressBackgroundTintList =
                ContextCompat.getColorStateList(view, R.color.colorAccent)
            element.thumbTintList = ContextCompat.getColorStateList(view, R.color.colorBlack)
        }
    }

    fun setElement(
        theme: Boolean,
        view: Context,
        element: SeekBar,
        color_light: Int,
        color_dark: Int
    ) {
        if (theme) {
            element.progressTintList = ContextCompat.getColorStateList(view, color_dark)
            element.progressBackgroundTintList = ContextCompat.getColorStateList(view, color_dark)
            element.backgroundTintList = ContextCompat.getColorStateList(view, color_dark)
            element.foregroundTintList = ContextCompat.getColorStateList(view, color_dark)
            element.indeterminateTintList = ContextCompat.getColorStateList(view, color_dark)
            element.secondaryProgressTintList = ContextCompat.getColorStateList(view, color_dark)
        } else {
            element.progressTintList = ContextCompat.getColorStateList(view, color_light)
            element.progressBackgroundTintList = ContextCompat.getColorStateList(view, color_light)
            element.backgroundTintList = ContextCompat.getColorStateList(view, color_light)
            element.foregroundTintList = ContextCompat.getColorStateList(view, color_light)
            element.indeterminateTintList = ContextCompat.getColorStateList(view, color_light)
            element.secondaryProgressTintList = ContextCompat.getColorStateList(view, color_light)
        }
    }

    fun setTabLayout(theme: Boolean, view: Context, element: TabLayout) {
        if (theme) {
            element.setBackgroundResource(R.color.colorBlack)
            element.setTabTextColors(
                ContextCompat.getColor(view, R.color.colorWhite),
                ContextCompat.getColor(view, R.color.colorSelectedDT)
            )
            element.setSelectedTabIndicatorColor(
                ContextCompat.getColor(
                    view,
                    R.color.colorSelectedDT
                )
            )
        } else {
            element.setBackgroundResource(R.color.colorWhite)
            element.setTabTextColors(
                ContextCompat.getColor(view, R.color.colorBlack),
                ContextCompat.getColor(view, R.color.colorSelected)
            )
            element.setSelectedTabIndicatorColor(
                ContextCompat.getColor(
                    view,
                    R.color.colorSelected
                )
            )
        }
    }
}