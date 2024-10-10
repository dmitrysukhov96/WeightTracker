package com.dmitrysukhov.weighttracker

import android.content.Context

const val PREFS_NAME = "weight_prefs"
const val PREF_GOAL = "weight_goal"

data class WeightGoal(
    val currentWeight: Float,
    val goalWeight: Float,
    val startDate: Long,
    val targetDate: Long
)

fun saveWeightGoal(context: Context, weightGoal: WeightGoal) {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val goalString = "${weightGoal.currentWeight},${weightGoal.goalWeight},${weightGoal.startDate},${weightGoal.targetDate}"
    editor.putString(PREF_GOAL, goalString)
    editor.apply()
}

fun loadWeightGoal(context: Context): WeightGoal? {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val goalString = sharedPreferences.getString(PREF_GOAL, null) ?: return null

    val data = goalString.split(",")
    return WeightGoal(
        currentWeight = data[0].toFloat(),
        goalWeight = data[1].toFloat(),
        startDate = data[2].toLong(),
        targetDate = data[3].toLong()
    )
}

fun deleteWeightGoal(context: Context) {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPreferences.edit().remove(PREF_GOAL).apply()
}
