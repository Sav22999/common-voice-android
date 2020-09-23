package org.commonvoice.saverio.ui.recyclerview.badges

import androidx.annotation.DrawableRes
import org.commonvoice.saverio.R

sealed class Badge(value: Int) {

    data class Level(

        val value: Int,

        val levelNumber: Int,

        ) : Badge(value)

    data class SpeakAchievement(

        val value: Int,

        val achievementText: String,

        ) : Badge(value)

    data class ListenAchievement(

        val value: Int,

        val achievementText: String,

        ) : Badge(value)

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
            "200K"
        )

        private fun switchLevel(savedLevel: Int) = when (savedLevel) {
            in 0..9 -> 1
            in 10..49 -> 2
            in 50..99 -> 3
            in 100..499 -> 4
            in 500..999 -> 5
            in 1000..4999 -> 6
            in 5000..9999 -> 7
            in 10000..49999 -> 8
            in 50000..99999 -> 9
            in 100000..199999 -> 10
            in 200000..399999 -> 11
            in 400000..999999 -> 12
            in 1000000..1999999 -> 13
            in 2000000..3999999 -> 14
            in 4000000..99999990000 -> 15
            else -> 1
        }

        private fun inverseSwitch(level: Int, isLevel: Boolean) = when (level) {
            1 -> 0
            2 -> if (isLevel) 10 else 5
            3 -> if (isLevel) 100 else 50
            4 -> if (isLevel) 200 else 100
            5 -> if (isLevel) 1000 else 500
            6 -> if (isLevel) 2000 else 1000
            7 -> if (isLevel) 10000 else 5000
            8 -> if (isLevel) 20000 else 10000
            9 -> if (isLevel) 100000 else 50000
            10 -> if (isLevel) 200000 else 100000
            11 -> if (isLevel) 400000 else 200000
            12 -> if (isLevel) 1000000 else 500000
            13 -> if (isLevel) 2000000 else 1000000
            14 -> if (isLevel) 4000000 else 2000000
            15 -> if (isLevel) 10000000 else 5000000
            else -> 0
        }

        private fun getTextArrayIndex(number: Int) = when (number) {
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
            in 200000..399999 -> 10
            /*
            in 400000..799999 -> 11
            in 800000..1999999 -> 12
            in 2000000..3999999 -> 13
            in 4000000..7999999 -> 14
            in 8000000..999999990000 -> 15
            */
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

            for (x in 1..levelNum) {
                baseList.add(
                    Level(inverseSwitch(x, true), x)
                )
            }

            for (x in 0 until recordingsIndex) {
                baseList.add(
                    SpeakAchievement(inverseSwitch(x + 2, false), textValue[x])
                )
            }

            for (x in 0 until validationsIndex) {
                baseList.add(
                    ListenAchievement(inverseSwitch(x + 2, false), textValue[x])
                )
            }

            return baseList
        }

    }

}