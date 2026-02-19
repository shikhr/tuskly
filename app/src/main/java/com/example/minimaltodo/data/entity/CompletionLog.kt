package com.example.minimaltodo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records daily progress for a [Goal].
 * One row per goal per day. The [date] field stores the date as "yyyy-MM-dd".
 */
@Entity(
    tableName = "completion_logs",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["goalId", "date"], unique = true),
    ],
)
data class CompletionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val date: String,
    val value: Float = 0f,
    val isCompleted: Boolean = false,
)
