package org.commonvoice.saverio_lib.dataClasses

class DailyGoal(
    var recordings: Int,
    var validations: Int,
    var goal: Int = 0
) {

    fun checkDailyGoal(): Boolean {
        //println("Daily goal --->  Now: " + (this.recordingsToday + this.validationsToday) + " -- To achive: "+getDailyGoal())
        if (goal == (recordings + validations)) {
            return true
        }
        return false
    }

}