package org.commonvoice.saverio.ui.recyclerview.badges

import org.commonvoice.saverio_lib.preferences.StatsPrefManager

sealed class Badge(val badgeValue: Int) {

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

        private fun inverseSwitch(level: Int, isLevel: Boolean) = when (level) {
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
            12 -> 500000
            13 -> 1000000
            14 -> 2000000
            15 -> 4000000
            else -> 0
        }

        fun generateBadgeData(
            savedLevel: Int, //56
            recordingsNumber: Int, //27
            validationsNumber: Int //29
        ): List<Badge> {
            val baseList = mutableListOf<Badge>()

            val recordingsIndex = StatsPrefManager.getStatsTextArrayIndex(recordingsNumber)
            val validationsIndex = StatsPrefManager.getStatsTextArrayIndex(validationsNumber)

            for (x in 1 .. savedLevel) {
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