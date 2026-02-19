package com.example.minimaltodo.data.repository

import com.example.minimaltodo.data.dao.TaskDao
import com.example.minimaltodo.data.entity.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
) {
    fun observeActiveTasks(): Flow<List<Task>> = taskDao.observeActiveTasks()

    fun observeCompletedTasks(): Flow<List<Task>> = taskDao.observeCompletedTasks()

    fun observeDeletedTasks(): Flow<List<Task>> = taskDao.observeDeletedTasks()

    suspend fun addTask(title: String, dueDate: Long? = null): Long {
        val task = Task(title = title, dueDate = dueDate)
        return taskDao.insert(task)
    }

    suspend fun completeTask(task: Task) {
        taskDao.update(
            task.copy(
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun uncompleteTask(task: Task) {
        taskDao.update(
            task.copy(
                isCompleted = false,
                completedAt = null,
            ),
        )
    }

    /** Soft-delete: marks the task as deleted without removing from DB. */
    suspend fun deleteTask(task: Task) = taskDao.softDelete(task.id)

    /** Restore a soft-deleted task. */
    suspend fun restoreTask(task: Task) = taskDao.restore(task.id)

    /** Permanently remove a single task from the database. */
    suspend fun permanentlyDeleteTask(task: Task) = taskDao.delete(task)

    /** Permanently remove all soft-deleted tasks. */
    suspend fun emptyDeletedTasks() = taskDao.permanentlyDeleteAll()

    suspend fun updateTask(task: Task) = taskDao.update(task)
}
