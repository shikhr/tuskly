package com.example.minimaltodo.data.fake

import com.example.minimaltodo.data.dao.GoalDao
import com.example.minimaltodo.data.entity.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeGoalDao : GoalDao {

    private val goals = MutableStateFlow<List<Goal>>(emptyList())
    private var nextId = 1L

    override fun observeActiveGoals(): Flow<List<Goal>> =
        goals.map { list -> list.filter { !it.isArchived && !it.isDeleted }.sortedBy { it.sortOrder } }

    override suspend fun getActiveGoals(): List<Goal> =
        goals.value.filter { !it.isArchived && !it.isDeleted }.sortedBy { it.sortOrder }

    override fun observeAllGoals(): Flow<List<Goal>> =
        goals.map { list -> list.filter { !it.isDeleted }.sortedBy { it.sortOrder } }

    override fun observeDeletedGoals(): Flow<List<Goal>> =
        goals.map { list -> list.filter { it.isDeleted }.sortedByDescending { it.deletedAt } }

    override suspend fun getById(id: Long): Goal? =
        goals.value.find { it.id == id }

    override suspend fun insert(goal: Goal): Long {
        val id = if (goal.id == 0L) nextId++ else goal.id
        val newGoal = goal.copy(id = id)
        goals.update { list ->
            list.filter { it.id != id } + newGoal
        }
        return id
    }

    override suspend fun update(goal: Goal) {
        goals.update { list ->
            list.map { if (it.id == goal.id) goal else it }
        }
    }

    override suspend fun delete(goal: Goal) {
        goals.update { list -> list.filter { it.id != goal.id } }
    }

    override suspend fun softDelete(id: Long, deletedAt: Long) {
        goals.update { list ->
            list.map {
                if (it.id == id) it.copy(isDeleted = true, deletedAt = deletedAt) else it
            }
        }
    }

    override suspend fun restore(id: Long) {
        goals.update { list ->
            list.map {
                if (it.id == id) it.copy(isDeleted = false, deletedAt = null) else it
            }
        }
    }

    override suspend fun permanentlyDeleteAll() {
        goals.update { list -> list.filter { !it.isDeleted } }
    }
}
