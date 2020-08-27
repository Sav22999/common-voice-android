package org.commonvoice.saverio.ui.recyclerview.badges

import androidx.annotation.DrawableRes
import org.commonvoice.saverio.R

sealed class Badge {

    data class Level(

        val levelNumber: Int

    ): Badge()

    data class Achievement(

        val achievementText: String,

        @DrawableRes val achievementImage: Int

    ): Badge()

    companion object {

        private val textValue = listOf(
            "5",
            "50",
            "100",
            "500",
            "1K",
            "5K",
            "10K",
            "50K",
            "100K",
            "200K",
        )

        private fun switchLevel(savedLevel: Int) = when(savedLevel) {
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
            in 10000..49999 -> 7
            in 50000..99999 -> 8
            in 100000..199999 -> 9
            in 200000..1000000000 -> 10
            else -> 0
        }

        fun generateBadgeData(
            savedLevel: Int,
            recordingsNumber: Int,
            validationsNumber: Int
        ): List<Badge> {
            val baseList = mutableListOf<Badge>()

            val levelNum = switchLevel(savedLevel)
            val recordingsIndex = getTextArrayIndex(recordingsNumber)
            val validationsIndex = getTextArrayIndex(validationsNumber)

            for(x in 1 .. levelNum) {
                baseList.add(
                    Level(x)
                )
            }

            for (x in 0 until recordingsIndex) {
                baseList.add(
                    Achievement(
                        textValue[x],
                        R.drawable.speak_cv
                    )
                )
            }

            for (x in 0 until validationsIndex) {
                baseList.add(
                    Achievement(
                        textValue[x],
                        R.drawable.listen_cv
                    )
                )
            }

            return baseList
        }

    }

}