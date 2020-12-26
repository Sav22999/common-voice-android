package org.commonvoice.saverio_lib.preferences

import android.content.Context
import androidx.lifecycle.LiveData
import org.commonvoice.saverio_lib.dataClasses.DailyGoal
import org.commonvoice.saverio_lib.utils.isOnADifferentDayFromToday
import java.util.*

class StatsPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("statsPreferences", Context.MODE_PRIVATE)

    val dailyGoal: LiveData<DailyGoal> = DailyGoalLivePreference(
        preferences,
        todayRecorded = Keys.TODAY_RECORDED.name,
        todayValidated = Keys.TODAY_VALIDATED.name,
        dailyObjective = Keys.DAILY_GOAL_OBJECTIVE.name
    )

    var dailyGoalObjective: Int
        get() {
            updateDailyGoal()
            return preferences.getInt(Keys.DAILY_GOAL_OBJECTIVE.name, 0)
        }
        set(value) {
            updateDailyGoal()
            preferences.edit().putInt(Keys.DAILY_GOAL_OBJECTIVE.name, value).apply()
        }

    var reviewOnPlayStoreCounter: Int
        get() = preferences.getInt(Keys.REVIEW_ON_PLAYSTORE_COUNTER.name, 0)
        set(value) = preferences.edit().putInt(Keys.REVIEW_ON_PLAYSTORE_COUNTER.name, value).apply()

    private var todayContributingDate: Calendar
        get() = Calendar.getInstance().also {
            val currentMillis = Calendar.getInstance().timeInMillis
            it.timeInMillis = preferences.getLong(Keys.TODAY_CONTRIBUTING_DATE.name, currentMillis)
            if (it.timeInMillis == currentMillis) {
                preferences.edit().putLong(Keys.TODAY_CONTRIBUTING_DATE.name, currentMillis).apply()
            }
        }
        set(value) { preferences.edit().putLong(Keys.TODAY_CONTRIBUTING_DATE.name, value.timeInMillis).apply() }

    var todayValidated: Int
        get() {
            updateDailyGoal()
            return preferences.getInt(Keys.TODAY_VALIDATED.name, 0)
        }
        set(value) {
            preferences.edit().putInt(Keys.TODAY_VALIDATED.name, value).apply()
        }

    var todayRecorded: Int
        get() {
            updateDailyGoal()
            return preferences.getInt(Keys.TODAY_RECORDED.name, 0)
        }
        set(value) {
            preferences.edit().putInt(Keys.TODAY_RECORDED.name, value).apply()
        }

    var allTimeRecorded: Int
        get() = preferences.getInt(Keys.ALLTIME_RECORDED.name, 0)
        set(value) = preferences.edit().putInt(Keys.ALLTIME_RECORDED.name, value).apply()

    var allTimeValidated: Int
        get() = preferences.getInt(Keys.ALLTIME_VALIDATED.name, 0)
        set(value) = preferences.edit().putInt(Keys.ALLTIME_VALIDATED.name, value).apply()

    var allTimeLevel: Int
        get() = preferences.getInt(Keys.ALLTIME_LEVEL.name, 0)
        set(value) = preferences.edit().putInt(Keys.ALLTIME_LEVEL.name, value).apply()

    val parsedLevel: Int
        get() = when (allTimeLevel) {
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

    private fun updateDailyGoal() {
        if (todayContributingDate.isOnADifferentDayFromToday()) {
            todayRecorded = 0
            todayValidated = 0
            todayContributingDate = Calendar.getInstance()
        }
    }

    private enum class Keys {
        DAILY_GOAL_OBJECTIVE,

        REVIEW_ON_PLAYSTORE_COUNTER,

        TODAY_CONTRIBUTING_DATE,
        TODAY_VALIDATED,
        TODAY_RECORDED,

        ALLTIME_RECORDED,
        ALLTIME_VALIDATED,
        ALLTIME_LEVEL,
    }

}