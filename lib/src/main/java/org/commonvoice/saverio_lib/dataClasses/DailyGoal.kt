package org.commonvoice.saverio_lib.dataClasses

class DailyGoal(
    var recordings: Int,
    var validations: Int,
    var goal: Int = 0
) {

    fun checkDailyGoal(): Boolean {
        //println("Daily goal --->  Now: " + (this.recordingsToday + this.validationsToday) + " -- To achive: "+getDailyGoal())
        if (goal == (recordings + validations) && goal != 0) {
            return true
        }
        return false
    }

    fun getDailyGoal(): Int {
        return goal
    }
}