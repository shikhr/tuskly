package com.example.minimaltodo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a recurring daily goal (e.g. "Exercise 30 min", "Read 20 pages").
 * Goals reset each day via [CompletionLog] tracking.
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetType: TargetType = TargetType.BINARY,
    val targetValue: Float = 1f,
    val unit: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)

enum class TargetType {
    BINARY,
    QUANTITY,
    TIMER,
}
