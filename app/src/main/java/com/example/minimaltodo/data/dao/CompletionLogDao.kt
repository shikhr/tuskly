package com.example.minimaltodo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.minimaltodo.data.entity.CompletionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionLogDao {

    @Query("SELECT * FROM completion_logs WHERE goalId = :goalId AND date = :date LIMIT 1")
    suspend fun getLog(goalId: Long, date: String): CompletionLog?

    @Query("SELECT * FROM completion_logs WHERE date = :date")
    fun observeLogsForDate(date: String): Flow<List<CompletionLog>>

    @Query("SELECT * FROM completion_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<CompletionLog>

    @Query("SELECT * FROM completion_logs WHERE goalId = :goalId ORDER BY date DESC")
    fun observeLogsForGoal(goalId: Long): Flow<List<CompletionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: CompletionLog): Long

    @Query("DELETE FROM completion_logs WHERE goalId = :goalId AND date = :date")
    suspend fun deleteLog(goalId: Long, date: String)
}
