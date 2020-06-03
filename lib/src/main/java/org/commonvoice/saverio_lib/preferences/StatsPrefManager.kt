package org.commonvoice.saverio_lib.preferences

import android.content.Context
import org.commonvoice.saverio_lib.dataClasses.DailyGoal
import org.commonvoice.saverio_lib.utils.isOnADifferentDayFromToday
import java.util.*

class StatsPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("statsPreferences", Context.MODE_PRIVATE)

    val dailyGoal: DailyGoal
        get() {
            if (todayContributingDate.isOnADifferentDayFromToday()) {
                todayRecorded = 0
                todayValidated = 0
                todayContributingDate = Calendar.getInstance()
            }
            return DailyGoal(todayRecorded, todayValidated, dailyGoalObjective)
        }

    var dailyGoalObjective: Int
        get() = preferences.getInt(Keys.DAILY_GOAL_OBJECTIVE.name, 0)
        set(value) { preferences.edit().putInt(Keys.DAILY_GOAL_OBJECTIVE.name, value).apply() }

    var todayContributingDate: Calendar
        get() = Calendar.getInstance().also {
            it.timeInMillis = preferences.getLong(Keys.TODAY_CONTRIBUTING_DATE.name, Calendar.getInstance().timeInMillis)
        }
        set(value) { preferences.edit().putLong(Keys.TODAY_CONTRIBUTING_DATE.name, value.timeInMillis).apply() }

    var todayValidated: Int
        get() = preferences.getInt(Keys.TODAY_VALIDATED.name, 0)
        set(value) { preferences.edit().putInt(Keys.TODAY_VALIDATED.name, value).apply() }

    var todayRecorded: Int
        get() = preferences.getInt(Keys.TODAY_RECORDED.name, 0)
        set(value) { preferences.edit().putInt(Keys.TODAY_RECORDED.name, value).apply() }

    private enum class Keys {
        DAILY_GOAL_OBJECTIVE,

        TODAY_CONTRIBUTING_DATE,
        TODAY_VALIDATED,
        TODAY_RECORDED,
    }

}