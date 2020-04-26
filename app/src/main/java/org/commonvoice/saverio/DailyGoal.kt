package org.commonvoice.saverio

class DailyGoal(recordings: Int, validations: Int) {
    private var recordingsToday: Int = recordings
    private var validationsToday: Int = validations
    private var dailyGoal: Int = 0

    fun setRecordings(recordings: Int) {
        this.recordingsToday = recordings
    }

    fun setValidations(validations: Int) {
        this.validationsToday = validations
    }

    fun getDailyGoal(): Int {
        return this.dailyGoal
    }

    fun setDailyGoal(value: Int) {
        this.dailyGoal = value
    }

    fun checkDailyGoal(): Boolean {
        //println("Daily goal --->  Now: " + (this.recordingsToday + this.validationsToday) + " -- To achive: "+getDailyGoal())
        if (this.getDailyGoal() == (this.recordingsToday+this.validationsToday)) {
            return true
        }
        return false
    }
}