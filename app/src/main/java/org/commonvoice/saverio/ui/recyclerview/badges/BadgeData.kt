package org.commonvoice.saverio.ui.recyclerview.badges

import androidx.annotation.DrawableRes
import org.commonvoice.saverio.R

sealed class Badge(value: Int) {

    data class Level(

        val value: Int,

        val levelNumber: Int,

    ): Badge(value)

    data class SpeakAchievement(

        val value: Int,

        val achievementText: String,

    ): Badge(value)

    data class ListenAchievement(

        val value: Int,

        val achievementText: String,

    ): Badge(value)

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

        private fun inverseSwitch(level: Int, isLevel: Boolean) = when(level) {
            1 -> 0
            2 -> if (isLevel) 10 else 5
            3 -> 50
            4 -> 100
            5 -> 500
            6 -> 1000
            7 -> 5000
            8 -> 10000
            9 -> 50000
            10 -> 100000
            11 -> 200000
            else -> 0
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

            val levelNum = /*switchLevel(savedLevel)*/ 10
            val recordingsIndex = /*getTextArrayIndex(recordingsNumber)*/ 10
            val validationsIndex = /*getTextArrayIndex(validationsNumber) */ 10

            for(x in 1 .. levelNum) {
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