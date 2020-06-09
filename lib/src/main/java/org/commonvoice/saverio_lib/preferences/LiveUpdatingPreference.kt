package org.commonvoice.saverio_lib.preferences

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import org.commonvoice.saverio_lib.dataClasses.DailyGoal

abstract class LivePreference<T>(
    private val sharedPreferences: SharedPreferences,
    private val preferenceKey: String? = null,
    private val preferenceKeyList: List<String>? = null
): LiveData<T>() {

    abstract fun getSavedValue(key: String? = null, keyList: List<String>? = null): T

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (preferenceKey != null && key == preferenceKey) {
            value = getSavedValue(key = preferenceKey)
        } else if (preferenceKeyList != null && preferenceKeyList.contains(key)) {
            value = getSavedValue(keyList = preferenceKeyList)
        }
    }

    override fun onActive() {
        super.onActive()

        value = getSavedValue(key = preferenceKey, keyList = preferenceKeyList)

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onInactive() {
        super.onInactive()

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

}

/**
 * Order:
 * -TodayRecorded
 * -TodayValidated
 * -DailyObjective
 */
class DailyGoalLivePreference(
    private val sharedPreferences: SharedPreferences,
    todayRecorded: String,
    todayValidated: String,
    dailyObjective: String
): LivePreference<DailyGoal>(sharedPreferences, preferenceKeyList = listOf(todayRecorded, todayValidated, dailyObjective)) {

    override fun getSavedValue(key: String?, keyList: List<String>?): DailyGoal {
        return keyList!!.let { list ->
            val todayRecorded = sharedPreferences.getInt(list[0], 0)
            val todayValidated = sharedPreferences.getInt(list[1], 0)
            val dailyGoalObjective = sharedPreferences.getInt(list[2], 0)

            DailyGoal(todayRecorded, todayValidated, dailyGoalObjective)
        }
    }

}