package org.commonvoice.saverio.ui.recyclerview.badges

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import org.commonvoice.saverio.R

data class BadgeData(

    val badgeValue: String,

    val descriptionText: String? = null,

    @ColorInt val backgroundTint: Int = Color.parseColor("#F0323232"),

    @DrawableRes val descriptionImage: Int? = null,

    val unlocked: Boolean

) {

    companion object {

        fun generateBadgeData(
            savedLevel: Int,
            recordingsNumber: Int,
            validationsNumber: Int
        ): List<BadgeData> {
            val baseList = mutableListOf<BadgeData>()

            val levelNum = getLevel(savedLevel)
            val recordingsIndex = getTextArrayIndex(recordingsNumber)
            val validationsIndex = getTextArrayIndex(validationsNumber)

            for(x in 1 .. 10) {
                baseList.add(
                    BadgeData(
                        "$x",
                        "level",
                        levelTints[x - 1],
                        unlocked = x <= levelNum
                    )
                )
            }

            for (x in 0 until 7) {
                baseList.add(
                    BadgeData(
                        textValue[x],
                        descriptionImage = R.drawable.speak_cv,
                        unlocked = x < recordingsIndex
                    )
                )
                baseList.add(
                    BadgeData(
                        textValue[x],
                        descriptionImage = R.drawable.listen_cv,
                        unlocked = x < validationsIndex
                    )
                )
            }

            return baseList
        }

    }

}

private val textValue = listOf(
    "5",
    "50",
    "100",
    "500",
    "1K",
    "5K",
    "10K"
)

private val levelTints = listOf<@ColorInt Int>(
    Color.parseColor("#F0FF0000"),
    Color.parseColor("#F0C80000"),
    Color.parseColor("#F06400FF"),
    Color.parseColor("#F0FF00FF"),
    Color.parseColor("#F00000FF"),
    Color.parseColor("#F0FF9600"),
    Color.parseColor("#F0CD7F32"),
    Color.parseColor("#F0C0C0C0"),
    Color.parseColor("#F0D4AF37"),
    Color.parseColor("#F0E5E4E2")
)

private fun getLevel(savedLevel: Int) = when(savedLevel) {
    in 0..9 -> 1
    in 10..49 -> 2
    in 50..99 -> 3
    in 100..499 -> 4
    in 500..999 -> 5
    in 1000..4999 -> 6
    in 5000..9999 -> 7
    in 10000..49999 -> 8
    in 50000..99999 -> 9
    in 100000..100000000 -> 10
    else -> 1
}

private fun getTextArrayIndex(number: Int) = when(number) {
    in 0..4 -> 0
    in 5..49 -> 1
    in 50..99 -> 2
    in 100..499 -> 3
    in 500..999 -> 4
    in 1000..4999 -> 5
    in 5000..9999 -> 6
    in 10000..100000000 -> 7
    else -> 0
}