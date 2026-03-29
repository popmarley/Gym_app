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
            put("age", age)
            put("genderLabel", genderLabel)
            put("goalLabel", goalLabel)
            put("profileCompleted", profileCompleted)
            put("selectedProgramStyle", selectedProgramStyle)
            put("selectedProgramLevel", selectedProgramLevel)
            put("activeProgramSource", activeProgramSource)
            put("unlockedAchievementIds", JSONArray().apply {
                unlockedAchievementIds.forEach(::put)
            })
            put("totalExperience", totalExperience)
            put("lastOpenedOn", lastOpenedOn ?: JSONObject.NULL)
            put("lastSavedAt", lastSavedAt ?: JSONObject.NULL)
            put("customProgramDays", JSONArray().apply {
                customProgramDays.forEach { day -> put(day.toJson()) }
            })
            put("exerciseWeights", JSONObject().apply {
                exerciseWeights.forEach { (exerciseId, value) -> put(exerciseId, value) }
            })
            put("weeklyProgress", JSONObject().apply {
                weeklyProgress.forEach { (dayId, progress) ->
                    put(dayId, JSONObject().apply {
                        put("isCompleted", progress.isCompleted)
                        put("completedOn", progress.completedOn ?: JSONObject.NULL)
                        put("completedSets", JSONObject().apply {
                            progress.completedSets.forEach { (exerciseId, count) -> put(exerciseId, count) }
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
            put("experienceLog", JSONArray().apply {
                experienceLog.forEach { entry ->
                    put(JSONObject().apply {
                        put("recordedOn", entry.recordedOn)
                        put("amount", entry.amount)
                        put("reason", entry.reason)
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
            experienceLog = json.optJSONArray("experienceLog").toExperienceLog(),
            currentWeightKg = json.optDouble("currentWeightKg", 59.0),
            targetWeightKg = json.optDouble("targetWeightKg", 70.0),
            lowMediaMode = json.optBoolean("lowMediaMode", true),
            age = json.optInt("age", 26),
            genderLabel = json.optString("genderLabel", "Erkek"),
            goalLabel = json.optString("goalLabel", "Kas / kütle artışı"),
            profileCompleted = json.optBoolean("profileCompleted", false),
            selectedProgramStyle = json.optString("selectedProgramStyle", ProgramStyle.FULL_BODY.name),
            selectedProgramLevel = json.optString("selectedProgramLevel", ProgramLevel.BASLANGIC.name),
            activeProgramSource = json.optString("activeProgramSource", ProgramSource.ONERILEN.name),
            customProgramDays = json.optJSONArray("customProgramDays").toWorkoutDays(),
            unlockedAchievementIds = json.optJSONArray("unlockedAchievementIds").toStringList(),
            totalExperience = json.optInt("totalExperience", 0),
            lastOpenedOn = json.optString("lastOpenedOn", "").ifBlank { null },
            lastSavedAt = json.optString("lastSavedAt", "").ifBlank { null }
        )
    }

    private fun WorkoutDay.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("dayLabel", dayLabel)
            put("dayOfWeek", dayOfWeek.name)
            put("focus", focus)
            put("estimatedMinutes", estimatedMinutes)
            put("summary", summary)
            put("isMandatory", isMandatory)
            put("isRestDay", isRestDay)
            put("recoveryText", recoveryText ?: JSONObject.NULL)
            put("exercises", JSONArray().apply {
                exercises.forEach { exercise -> put(exercise.toJson()) }
            })
        }
    }

    private fun Exercise.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("targetArea", targetArea)
            put("equipment", equipment)
            put("setCount", setCount)
            put("repText", repText)
            put("referenceReps", referenceReps)
            put("restSeconds", restSeconds)
            put("quickNote", quickNote)
            put("steps", JSONArray().apply { steps.forEach(::put) })
            put("commonMistake", commonMistake)
            put("tip", tip)
            put("isBodyweightOnly", isBodyweightOnly)
            put("timerSuggestionSeconds", timerSuggestionSeconds ?: JSONObject.NULL)
            put("mediaCaption", mediaCaption)
        }
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

    private fun JSONArray?.toExperienceLog(): List<ExperienceEntry> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    ExperienceEntry(
                        recordedOn = item.optString("recordedOn"),
                        amount = item.optInt("amount", 0),
                        reason = item.optString("reason")
                    )
                )
            }
        }
    }

    private fun JSONArray?.toWorkoutDays(): List<WorkoutDay> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    WorkoutDay(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        dayLabel = item.optString("dayLabel"),
                        dayOfWeek = runCatching { java.time.DayOfWeek.valueOf(item.optString("dayOfWeek")) }.getOrDefault(java.time.DayOfWeek.MONDAY),
                        focus = item.optString("focus"),
                        estimatedMinutes = item.optInt("estimatedMinutes", 0),
                        summary = item.optString("summary"),
                        exercises = item.optJSONArray("exercises").toExercises(),
                        isMandatory = item.optBoolean("isMandatory", false),
                        isRestDay = item.optBoolean("isRestDay", false),
                        recoveryText = item.optString("recoveryText", "").ifBlank { null }
                    )
                )
            }
        }
    }

    private fun JSONArray?.toExercises(): List<Exercise> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    Exercise(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        targetArea = item.optString("targetArea"),
                        equipment = item.optString("equipment"),
                        setCount = item.optInt("setCount", 0),
                        repText = item.optString("repText"),
                        referenceReps = item.optInt("referenceReps", 0),
                        restSeconds = item.optInt("restSeconds", 0),
                        quickNote = item.optString("quickNote"),
                        steps = item.optJSONArray("steps").toStringList(),
                        commonMistake = item.optString("commonMistake"),
                        tip = item.optString("tip"),
                        isBodyweightOnly = item.optBoolean("isBodyweightOnly", false),
                        timerSuggestionSeconds = if (item.isNull("timerSuggestionSeconds")) null else item.optInt("timerSuggestionSeconds", 0),
                        mediaCaption = item.optString("mediaCaption", "")
                    )
                )
            }
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                add(optString(index))
            }
        }
    }

    private companion object {
        const val KEY_STATE = "persisted_state"
    }
}
