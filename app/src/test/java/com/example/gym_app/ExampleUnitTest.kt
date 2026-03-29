package com.example.gym_app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun weekly_completion_ignores_rest_and_optional_days() {
        var state = PersistedState()
        state = state.completeWorkout(workoutById("upper_a"))
        state = state.completeWorkout(workoutById("lower_a"))
        assertEquals(50, weeklyCompletionPercent(state))
    }

    @Test
    fun streak_counts_consecutive_days() {
        val state = PersistedState(
            completedWorkouts = listOf(
                CompletedWorkout("upper_a", "Ust Vucut A", "2026-03-27", 1000),
                CompletedWorkout("lower_a", "Alt Vucut A + Karin", "2026-03-28", 1100),
                CompletedWorkout("upper_b", "Ust Vucut B", "2026-03-29", 1200)
            )
        )
        assertEquals(3, streakCount(state, java.time.LocalDate.parse("2026-03-29")))
    }

    @Test
    fun complete_workout_marks_day_finished_and_adds_history() {
        val updated = PersistedState().completeWorkout(workoutById("upper_a"))
        assertTrue(isWorkoutCompleted(updated, "upper_a"))
        assertEquals(1, totalCompletedWorkouts(updated))
    }
}
