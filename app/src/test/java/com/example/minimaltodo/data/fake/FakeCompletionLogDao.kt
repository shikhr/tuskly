package com.example.minimaltodo.data.fake

import com.example.minimaltodo.data.dao.CompletionLogDao
import com.example.minimaltodo.data.entity.CompletionLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCompletionLogDao : CompletionLogDao {

    private val logs = MutableStateFlow<List<CompletionLog>>(emptyList())
    private var nextId = 1L

    override suspend fun getLog(goalId: Long, date: String): CompletionLog? =
        logs.value.find { it.goalId == goalId && it.date == date }

    override fun observeLogsForDate(date: String): Flow<List<CompletionLog>> =
        logs.map { list -> list.filter { it.date == date } }

    override suspend fun getLogsForDate(date: String): List<CompletionLog> =
        logs.value.filter { it.date == date }

    override fun observeLogsForGoal(goalId: Long): Flow<List<CompletionLog>> =
        logs.map { list -> list.filter { it.goalId == goalId }.sortedByDescending { it.date } }

    override suspend fun upsert(log: CompletionLog): Long {
        val id = if (log.id == 0L) nextId++ else log.id
        val newLog = log.copy(id = id)
        logs.update { list ->
            list.filter { !(it.goalId == newLog.goalId && it.date == newLog.date) } + newLog
        }
        return id
    }

    override suspend fun deleteLog(goalId: Long, date: String) {
        logs.update { list ->
            list.filter { !(it.goalId == goalId && it.date == date) }
        }
    }
}
