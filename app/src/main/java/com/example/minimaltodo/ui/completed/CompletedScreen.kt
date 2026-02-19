package com.example.minimaltodo.ui.completed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minimaltodo.R
import com.example.minimaltodo.data.entity.Task
import com.example.minimaltodo.ui.components.CheckRow
import com.example.minimaltodo.ui.components.EmptyState
import com.example.minimaltodo.ui.components.SwipeToDismissItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedScreen(
    viewModel: CompletedViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.completedTasks.isEmpty() && !uiState.isLoading) {
        EmptyState(
            icon = Icons.Default.Check,
            message = stringResource(R.string.empty_completed),
            modifier = modifier.padding(32.dp),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            items(
                items = uiState.completedTasks,
                key = { it.id },
            ) { task ->
                CompletedTaskItem(
                    task = task,
                    onUncomplete = { viewModel.uncompleteTask(task) },
                    onDelete = { viewModel.deleteTask(task) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletedTaskItem(
    task: Task,
    onUncomplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onUncomplete()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                else -> false
            }
        },
    )

    SwipeToDismissItem(
        state = dismissState,
        startToEndIcon = Icons.Default.Refresh,
        startToEndColor = MaterialTheme.colorScheme.tertiary,
        endToStartIcon = Icons.Default.Delete,
    ) {
        CheckRow(
            text = task.title,
            checked = true,
            onCheckedChange = { onUncomplete() },
        )
    }
}
