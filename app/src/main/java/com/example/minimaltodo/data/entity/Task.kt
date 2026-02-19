package com.example.minimaltodo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A one-time task in the inbox/to-do list.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)
