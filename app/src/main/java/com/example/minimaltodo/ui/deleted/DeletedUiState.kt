package com.example.minimaltodo.ui.deleted

import com.example.minimaltodo.data.entity.Goal
import com.example.minimaltodo.data.entity.Task

data class DeletedUiState(
    val deletedGoals: List<Goal> = emptyList(),
    val deletedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
)
