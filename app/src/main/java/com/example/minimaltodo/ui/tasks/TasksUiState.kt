package com.example.minimaltodo.ui.tasks

import com.example.minimaltodo.data.entity.Task

data class TasksUiState(
    val activeTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = true,
)
