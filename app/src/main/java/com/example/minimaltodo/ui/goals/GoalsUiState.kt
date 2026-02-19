package com.example.minimaltodo.ui.goals

import com.example.minimaltodo.data.entity.CompletionLog
import com.example.minimaltodo.data.entity.Goal

data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val completionLogs: Map<Long, CompletionLog> = emptyMap(),
    val isLoading: Boolean = true,
)
