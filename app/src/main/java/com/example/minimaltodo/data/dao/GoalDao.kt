package com.example.minimaltodo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.minimaltodo.data.entity.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE isArchived = 0 AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun observeActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE isArchived = 0 AND isDeleted = 0 ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getActiveGoals(): List<Goal>

    @Query("SELECT * FROM goals WHERE isDeleted = 0 ORDER BY sortOrder ASC")
    fun observeAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun observeDeletedGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("UPDATE goals SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE goals SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM goals WHERE isDeleted = 1")
    suspend fun permanentlyDeleteAll()
}
