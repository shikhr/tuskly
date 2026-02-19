package com.example.minimaltodo.data.repository

import com.example.minimaltodo.data.entity.TargetType
import com.example.minimaltodo.data.fake.FakeCompletionLogDao
import com.example.minimaltodo.data.fake.FakeGoalDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GoalRepositoryTest {

    private lateinit var goalDao: FakeGoalDao
    private lateinit var completionLogDao: FakeCompletionLogDao
    private lateinit var repository: GoalRepository

    @Before
    fun setup() {
        goalDao = FakeGoalDao()
        completionLogDao = FakeCompletionLogDao()
        repository = GoalRepository(goalDao, completionLogDao)
    }

    @Test
    fun addGoal_appearsInActiveGoals() = runTest {
        repository.addGoal("Exercise", TargetType.BINARY, 1f, "")

        val goals = repository.observeActiveGoals().first()
        assertEquals(1, goals.size)
        assertEquals("Exercise", goals[0].name)
    }

    @Test
    fun deleteGoal_removesFromActiveGoals() = runTest {
        repository.addGoal("Exercise", TargetType.BINARY, 1f, "")
        val goal = repository.observeActiveGoals().first().first()

        repository.deleteGoal(goal)

        val goals = repository.observeActiveGoals().first()
        assertTrue(goals.isEmpty())
    }

    @Test
    fun toggleCompletion_completesGoal() = runTest {
        val goalId = repository.addGoal("Exercise", TargetType.BINARY, 1f, "")
        val date = "2026-01-01"

        repository.toggleCompletion(goalId, date, 0f, 1f)

        val logs = repository.observeCompletionLogsForDate(date).first()
        assertEquals(1, logs.size)
        assertTrue(logs[0].isCompleted)
    }

    @Test
    fun toggleCompletion_twice_uncompletes() = runTest {
        val goalId = repository.addGoal("Exercise", TargetType.BINARY, 1f, "")
        val date = "2026-01-01"

        repository.toggleCompletion(goalId, date, 0f, 1f)
        repository.toggleCompletion(goalId, date, 1f, 1f)

        val logs = repository.observeCompletionLogsForDate(date).first()
        assertTrue(logs.isEmpty())
    }

    @Test
    fun updateProgress_marksCompleteWhenTargetReached() = runTest {
        val goalId = repository.addGoal("Water", TargetType.QUANTITY, 3f, "L")
        val date = "2026-01-01"

        repository.updateProgress(goalId, date, 3f, 3f)

        val logs = repository.observeCompletionLogsForDate(date).first()
        assertEquals(1, logs.size)
        assertTrue(logs[0].isCompleted)
        assertEquals(3f, logs[0].value)
    }

    @Test
    fun updateProgress_notCompleteWhenBelowTarget() = runTest {
        val goalId = repository.addGoal("Water", TargetType.QUANTITY, 3f, "L")
        val date = "2026-01-01"

        repository.updateProgress(goalId, date, 2f, 3f)

        val logs = repository.observeCompletionLogsForDate(date).first()
        assertEquals(1, logs.size)
        assertFalse(logs[0].isCompleted)
    }
}
