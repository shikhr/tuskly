package com.example.minimaltodo.ui.completed

import com.example.minimaltodo.data.entity.Task

data class CompletedUiState(
    val completedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
)
