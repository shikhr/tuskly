package com.example.minimaltodo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.minimaltodo.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun observeActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getActiveTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND isDeleted = 0 ORDER BY completedAt DESC")
    fun observeCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY isCompleted ASC, sortOrder ASC, createdAt ASC")
    fun observeAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun observeDeletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM tasks WHERE isDeleted = 1")
    suspend fun permanentlyDeleteAll()
}
