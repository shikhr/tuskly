package com.example.minimaltodo.ui.deleted

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minimaltodo.R
import com.example.minimaltodo.ui.components.EmptyState
import com.example.minimaltodo.ui.components.SwipeToDismissItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyDeletedScreen(
    viewModel: DeletedViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEmpty = uiState.deletedGoals.isEmpty() && uiState.deletedTasks.isEmpty()

    if (isEmpty && !uiState.isLoading) {
        EmptyState(
            icon = Icons.Default.Delete,
            message = stringResource(R.string.empty_deleted),
            modifier = modifier.padding(32.dp),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            if (uiState.deletedGoals.isNotEmpty()) {
                item(key = "goals_header") {
                    SectionHeader(title = stringResource(R.string.nav_goals))
                }
                items(
                    items = uiState.deletedGoals,
                    key = { "goal_${it.id}" },
                ) { goal ->
                    DeletedItem(
                        text = goal.name,
                        onRestore = { viewModel.restoreGoal(goal) },
                        onPermanentDelete = { viewModel.permanentlyDeleteGoal(goal) },
                    )
                }
            }
            if (uiState.deletedTasks.isNotEmpty()) {
                item(key = "tasks_header") {
                    SectionHeader(title = stringResource(R.string.nav_tasks))
                }
                items(
                    items = uiState.deletedTasks,
                    key = { "task_${it.id}" },
                ) { task ->
                    DeletedItem(
                        text = task.title,
                        onRestore = { viewModel.restoreTask(task) },
                        onPermanentDelete = { viewModel.permanentlyDeleteTask(task) },
                    )
                }
            }
            if (!isEmpty) {
                item(key = "empty_all") {
                    TextButton(
                        onClick = viewModel::emptyAll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.action_empty_deleted),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeletedItem(
    text: String,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onRestore()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onPermanentDelete()
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.action_restore),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
