package com.example.gym_app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun weekly_completion_ignores_rest_and_optional_days() {
        val referenceDate = LocalDate.parse("2026-03-30")
        var state = PersistedState().selectProgramLevel(ProgramLevel.ORTA, referenceDate)
        val mandatoryDays = workoutPlan(state, referenceDate).filter { it.isMandatory && !it.isRestDay }
        state = state.completeWorkout(mandatoryDays[0], referenceDate)
        state = state.completeWorkout(mandatoryDays[1], referenceDate.plusDays(1))
        assertEquals(50, weeklyCompletionPercent(state, referenceDate))
    }

    @Test
    fun streak_counts_consecutive_days() {
        val state = PersistedState(
            completedWorkouts = listOf(
                CompletedWorkout("upper_a", "Üst Vücut A", "2026-03-27", 1000),
                CompletedWorkout("lower_a", "Alt Vücut A + Karın", "2026-03-28", 1100),
                CompletedWorkout("upper_b", "Üst Vücut B", "2026-03-29", 1200)
            )
        )
        assertEquals(3, streakCount(state, java.time.LocalDate.parse("2026-03-29")))
    }

    @Test
    fun complete_workout_marks_day_finished_and_adds_history() {
        val referenceDate = LocalDate.parse("2026-03-30")
        val state = PersistedState()
        val mondayWorkout = workoutPlan(state, referenceDate).first { it.dayOfWeek == DayOfWeek.MONDAY }
        val updated = state.completeWorkout(mondayWorkout, referenceDate)
        assertTrue(isWorkoutCompleted(updated, mondayWorkout, referenceDate))
        assertEquals(1, totalCompletedWorkouts(updated))
    }

    @Test
    fun undo_set_after_week_rollover_does_not_leak_previous_week_progress() {
        val firstWeekDate = LocalDate.parse("2026-03-30")
        val nextWeekDate = firstWeekDate.plusWeeks(1)
        val mondayWorkout = workoutPlan(PersistedState(), firstWeekDate).first { it.dayOfWeek == DayOfWeek.MONDAY }

        val firstWeekState = PersistedState().completeSet(mondayWorkout, mondayWorkout.exercises.first().id, firstWeekDate)
        val nextWeekState = firstWeekState.normalizeForCurrentWeek(nextWeekDate)

        assertEquals(0, completedSets(mondayWorkout, nextWeekState, nextWeekDate))
    }
}
