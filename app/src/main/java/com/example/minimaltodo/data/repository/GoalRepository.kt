package com.example.minimaltodo.data.repository

import com.example.minimaltodo.data.dao.CompletionLogDao
import com.example.minimaltodo.data.dao.GoalDao
import com.example.minimaltodo.data.entity.CompletionLog
import com.example.minimaltodo.data.entity.Goal
import com.example.minimaltodo.data.entity.TargetType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val completionLogDao: CompletionLogDao,
) {
    fun observeActiveGoals(): Flow<List<Goal>> = goalDao.observeActiveGoals()

    fun observeDeletedGoals(): Flow<List<Goal>> = goalDao.observeDeletedGoals()

    fun observeCompletionLogsForDate(date: String): Flow<List<CompletionLog>> =
        completionLogDao.observeLogsForDate(date)

    suspend fun addGoal(
        name: String,
        targetType: TargetType,
        targetValue: Float,
        unit: String,
    ): Long {
        val goal = Goal(
            name = name,
            targetType = targetType,
            targetValue = targetValue,
            unit = unit,
        )
        return goalDao.insert(goal)
    }

    suspend fun updateGoal(goal: Goal) = goalDao.update(goal)

    /** Soft-delete: marks the goal as deleted without removing from DB. */
    suspend fun deleteGoal(goal: Goal) = goalDao.softDelete(goal.id)

    /** Restore a soft-deleted goal. */
    suspend fun restoreGoal(goal: Goal) = goalDao.restore(goal.id)

    /** Permanently remove a single goal from the database. */
    suspend fun permanentlyDeleteGoal(goal: Goal) = goalDao.delete(goal)

    /** Permanently remove all soft-deleted goals. */
    suspend fun emptyDeletedGoals() = goalDao.permanentlyDeleteAll()

    suspend fun toggleCompletion(goalId: Long, date: String, currentValue: Float, targetValue: Float) {
        val existing = completionLogDao.getLog(goalId, date)
        if (existing != null && existing.isCompleted) {
            completionLogDao.deleteLog(goalId, date)
        } else {
            completionLogDao.upsert(
                CompletionLog(
                    id = existing?.id ?: 0,
                    goalId = goalId,
                    date = date,
                    value = targetValue,
                    isCompleted = true,
                ),
            )
        }
    }

    suspend fun updateProgress(goalId: Long, date: String, value: Float, targetValue: Float) {
        val existing = completionLogDao.getLog(goalId, date)
        completionLogDao.upsert(
            CompletionLog(
                id = existing?.id ?: 0,
                goalId = goalId,
                date = date,
                value = value,
                isCompleted = value >= targetValue,
            ),
        )
    }
}
