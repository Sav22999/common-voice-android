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

    private var todayContributingDate: Calendar
        get() = Calendar.getInstance().also {
            it.timeInMillis = preferences.getLong(Keys.TODAY_CONTRIBUTING_DATE.name, Calendar.getInstance().timeInMillis)
        }
        set(value) { preferences.edit().putLong(Keys.TODAY_CONTRIBUTING_DATE.name, value.timeInMillis).apply() }

    var todayValidated: Int
        get() {
            updateDailyGoal()
            return preferences.getInt(Keys.TODAY_VALIDATED.name, 0)
        }
        set(value) {
            updateDailyGoal()
            preferences.edit().putInt(Keys.TODAY_VALIDATED.name, value).apply()
        }

    var todayRecorded: Int
        get() {
            updateDailyGoal()
            return preferences.getInt(Keys.TODAY_RECORDED.name, 0)
        }
        set(value) {
            updateDailyGoal()
            preferences.edit().putInt(Keys.TODAY_RECORDED.name, value).apply()
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

        TODAY_CONTRIBUTING_DATE,
        TODAY_VALIDATED,
        TODAY_RECORDED,
    }

}