package org.commonvoice.saverio

class BadgeLevelDailyGoal {
    private var recordingsToday: Int = 0
    private var validationsToday: Int = 0
    private var dailyGoal: Int = 0

    constructor(recordings: Int, validations: Int) {
        this.recordingsToday = recordings
        this.validationsToday = validations
    }

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