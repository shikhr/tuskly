package com.example.minimaltodo.widget

import android.content.Context
import android.util.Log
import com.example.minimaltodo.data.db.AppDatabase
import com.example.minimaltodo.data.entity.CompletionLog
import com.example.minimaltodo.data.entity.TargetType
import com.example.minimaltodo.data.repository.SettingsRepository
import com.example.minimaltodo.util.logicalDateString

/**
 * Provides Room DB access for widgets using the same shared
 * [AppDatabase] singleton that Hilt provides to the app.
 */
object WidgetDataHelper {

    private fun getDatabase(context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    // ---------- serialisation helpers ----------

    /**
     * Build a pipe-delimited string of goal rows for Glance state.
     * Format per line: `id|name|isBinary|targetValue|currentValue|isCompleted`
     */
    suspend fun buildGoalsDataString(context: Context): String {
        val db = getDatabase(context)
        val goals = db.goalDao().getActiveGoals()
        val today = logicalDateString(SettingsRepository.getResetHour(context))
        val logs = db.completionLogDao().getLogsForDate(today)
        val logsByGoalId = logs.associateBy { it.goalId }

        return goals.joinToString("\n") { goal ->
            val log = logsByGoalId[goal.id]
            val isBinary = goal.targetType == TargetType.BINARY
            val currentValue = log?.value ?: 0f
            val isCompleted = log?.isCompleted == true
            "${goal.id}|${goal.name}|$isBinary|${goal.targetValue}|$currentValue|$isCompleted"
        }
    }

    /**
     * Build a pipe-delimited string of task rows for Glance state.
     * Format per line: `id|title|dueDate`
     * dueDate is epoch millis or empty string for null.
     */
    suspend fun buildTasksDataString(context: Context): String {
        val db = getDatabase(context)
        val tasks = db.taskDao().getActiveTasks()
        return tasks.joinToString("\n") { task ->
            "${task.id}|${task.title}|${task.dueDate ?: ""}"
        }
    }

    // ---------- mutation helpers ----------

    /**
     * Cycle a goal's progress for today.
     * - Binary goals: toggle between unchecked and checked.
     * - Quantity goals: increment value by 1 each tap, cycling back to 0 after reaching target.
     */
    suspend fun cycleGoalProgress(context: Context, goalId: Long) {
        val db = getDatabase(context)
        val logDao = db.completionLogDao()
        val goalDao = db.goalDao()
        val goal = goalDao.getById(goalId)
        if (goal == null) {
            Log.e("WidgetData", "Goal $goalId not found")
            return
        }
        val today = logicalDateString(SettingsRepository.getResetHour(context))
        val existing = logDao.getLog(goalId, today)
        val currentValue = existing?.value ?: 0f
        val target = goal.targetValue

        if (goal.targetType == TargetType.BINARY) {
            if (existing?.isCompleted == true) {
                logDao.deleteLog(goalId, today)
            } else {
                logDao.upsert(
                    CompletionLog(
                        id = existing?.id ?: 0,
                        goalId = goalId,
                        date = today,
                        value = 1f,
                        isCompleted = true,
                    ),
                )
            }
        } else {
            val nextValue = if (currentValue >= target) 0f else currentValue + 1f
            if (nextValue == 0f) {
                logDao.deleteLog(goalId, today)
            } else {
                logDao.upsert(
                    CompletionLog(
                        id = existing?.id ?: 0,
                        goalId = goalId,
                        date = today,
                        value = nextValue,
                        isCompleted = nextValue >= target,
                    ),
                )
            }
        }
    }

    /** Mark a task as completed. */
    suspend fun toggleTaskCompletion(context: Context, taskId: Long) {
        val dao = getDatabase(context).taskDao()
        val task = dao.getById(taskId) ?: return
        dao.update(
            task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null,
            ),
        )
    }
}
