package com.example.minimaltodo.ui.goals

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minimaltodo.R
import com.example.minimaltodo.data.entity.CompletionLog
import com.example.minimaltodo.data.entity.Goal
import com.example.minimaltodo.ui.components.EmptyState
import com.example.minimaltodo.ui.components.SwipeToDismissItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGoalsScreen(
    viewModel: GoalsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.goals.isEmpty() && !uiState.isLoading) {
        EmptyState(
            icon = Icons.Default.CheckCircle,
            message = stringResource(R.string.empty_goals),
            modifier = modifier.padding(32.dp),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            items(
                items = uiState.goals,
                key = { it.id },
            ) { goal ->
                GoalItem(
                    goal = goal,
                    completionLog = uiState.completionLogs[goal.id],
                    onToggle = { viewModel.toggleGoalCompletion(goal) },
                    onSetProgress = { value -> viewModel.setProgress(goal, value) },
                    onDelete = { viewModel.deleteGoal(goal) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalItem(
    goal: Goal,
    completionLog: CompletionLog?,
    onToggle: () -> Unit,
    onSetProgress: (Float) -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissItem(
        state = dismissState,
        endToStartIcon = Icons.Default.Delete,
    ) {
        GoalCard(
            goal = goal,
            completionLog = completionLog,
            onToggle = onToggle,
            onSetProgress = onSetProgress,
        )
    }
}
