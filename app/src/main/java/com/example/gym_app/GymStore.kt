package com.example.gym_app

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class GymStore(context: Context) {
    private val preferences = context.getSharedPreferences("gym_flow_store", Context.MODE_PRIVATE)

    fun loadState(): PersistedState {
        val raw = preferences.getString(KEY_STATE, null) ?: return PersistedState()
        return runCatching { raw.toState() }.getOrElse { PersistedState() }
    }

    fun saveState(state: PersistedState) {
        preferences.edit {
            putString(KEY_STATE, state.toJson().toString())
        }
    }

    private fun PersistedState.toJson(): JSONObject {
        return JSONObject().apply {
            put("weekKey", weekKey)
            put("currentWeightKg", currentWeightKg)
            put("targetWeightKg", targetWeightKg)
            put("lowMediaMode", lowMediaMode)
            put("exerciseWeights", JSONObject().apply {
                exerciseWeights.forEach { (exerciseId, value) ->
                    put(exerciseId, value)
                }
            })
            put("weeklyProgress", JSONObject().apply {
                weeklyProgress.forEach { (dayId, progress) ->
                    put(dayId, JSONObject().apply {
                        put("isCompleted", progress.isCompleted)
                        put("completedOn", progress.completedOn ?: JSONObject.NULL)
                        put("completedSets", JSONObject().apply {
                            progress.completedSets.forEach { (exerciseId, count) ->
                                put(exerciseId, count)
                            }
                        })
                    })
                }
            })
            put("completedWorkouts", JSONArray().apply {
                completedWorkouts.forEach { completed ->
                    put(JSONObject().apply {
                        put("dayId", completed.dayId)
                        put("workoutTitle", completed.workoutTitle)
                        put("completedOn", completed.completedOn)
                        put("estimatedVolumeKg", completed.estimatedVolumeKg)
                    })
                }
            })
        }
    }

    private fun String.toState(): PersistedState {
        val json = JSONObject(this)
        return PersistedState(
            weekKey = json.optString("weekKey", currentWeekKey()),
            weeklyProgress = json.optJSONObject("weeklyProgress").toWeeklyProgress(),
            exerciseWeights = json.optJSONObject("exerciseWeights").toDoubleMap(),
            completedWorkouts = json.optJSONArray("completedWorkouts").toCompletedWorkouts(),
            currentWeightKg = json.optDouble("currentWeightKg", 59.0),
            targetWeightKg = json.optDouble("targetWeightKg", 70.0),
            lowMediaMode = json.optBoolean("lowMediaMode", true)
        )
    }

    private fun JSONObject?.toDoubleMap(): Map<String, Double> {
        if (this == null) return emptyMap()
        val result = linkedMapOf<String, Double>()
        val keys = keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = optDouble(key, 0.0)
        }
        return result
    }

    private fun JSONObject?.toWeeklyProgress(): Map<String, DayProgress> {
        if (this == null) return emptyMap()
        val result = linkedMapOf<String, DayProgress>()
        val keys = keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val dayJson = optJSONObject(key) ?: continue
            val completedSetsJson = dayJson.optJSONObject("completedSets")
            val completedSets = linkedMapOf<String, Int>()
            if (completedSetsJson != null) {
                val setKeys = completedSetsJson.keys()
                while (setKeys.hasNext()) {
                    val exerciseId = setKeys.next()
                    completedSets[exerciseId] = completedSetsJson.optInt(exerciseId, 0)
                }
            }
            result[key] = DayProgress(
                completedSets = completedSets,
                isCompleted = dayJson.optBoolean("isCompleted", false),
                completedOn = dayJson.optString("completedOn", "").ifBlank { null }
            )
        }
        return result
    }

    private fun JSONArray?.toCompletedWorkouts(): List<CompletedWorkout> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    CompletedWorkout(
                        dayId = item.optString("dayId"),
                        workoutTitle = item.optString("workoutTitle"),
                        completedOn = item.optString("completedOn"),
                        estimatedVolumeKg = item.optInt("estimatedVolumeKg", 0)
                    )
                )
            }
        }
    }

    private companion object {
        const val KEY_STATE = "persisted_state"
    }
}
