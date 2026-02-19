package com.example.minimaltodo.data.fake

import com.example.minimaltodo.data.dao.TaskDao
import com.example.minimaltodo.data.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTaskDao : TaskDao {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    override fun observeActiveTasks(): Flow<List<Task>> =
        tasks.map { list -> list.filter { !it.isCompleted && !it.isDeleted }.sortedBy { it.sortOrder } }

    override suspend fun getActiveTasks(): List<Task> =
        tasks.value.filter { !it.isCompleted && !it.isDeleted }.sortedBy { it.sortOrder }

    override fun observeCompletedTasks(): Flow<List<Task>> =
        tasks.map { list ->
            list.filter { it.isCompleted && !it.isDeleted }.sortedByDescending { it.completedAt }
        }

    override fun observeAllTasks(): Flow<List<Task>> =
        tasks.map { list ->
            list.filter { !it.isDeleted }
                .sortedWith(compareBy<Task> { it.isCompleted }.thenBy { it.sortOrder })
        }

    override fun observeDeletedTasks(): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.isDeleted }.sortedByDescending { it.deletedAt } }

    override suspend fun getById(id: Long): Task? =
        tasks.value.find { it.id == id }

    override suspend fun insert(task: Task): Long {
        val id = if (task.id == 0L) nextId++ else task.id
        val newTask = task.copy(id = id)
        tasks.update { list ->
            list.filter { it.id != id } + newTask
        }
        return id
    }

    override suspend fun update(task: Task) {
        tasks.update { list ->
            list.map { if (it.id == task.id) task else it }
        }
    }

    override suspend fun delete(task: Task) {
        tasks.update { list -> list.filter { it.id != task.id } }
    }

    override suspend fun softDelete(id: Long, deletedAt: Long) {
        tasks.update { list ->
            list.map {
                if (it.id == id) it.copy(isDeleted = true, deletedAt = deletedAt) else it
            }
        }
    }

    override suspend fun restore(id: Long) {
        tasks.update { list ->
            list.map {
                if (it.id == id) it.copy(isDeleted = false, deletedAt = null) else it
            }
        }
    }

    override suspend fun permanentlyDeleteAll() {
        tasks.update { list -> list.filter { !it.isDeleted } }
    }
}
