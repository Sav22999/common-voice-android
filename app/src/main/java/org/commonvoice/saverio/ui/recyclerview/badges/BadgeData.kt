package org.commonvoice.saverio.ui.recyclerview.badges

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
            savedLevel: Int, //56
            recordingsNumber: Int, //27
            validationsNumber: Int //29
        ): List<Badge> {
            val baseList = mutableListOf<Badge>()

            val recordingsIndex = getTextArrayIndex(recordingsNumber) //1
            val validationsIndex = getTextArrayIndex(validationsNumber) //1

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