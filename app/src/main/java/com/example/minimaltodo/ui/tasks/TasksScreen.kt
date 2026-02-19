package com.example.minimaltodo.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minimaltodo.R
import com.example.minimaltodo.data.entity.Task
import com.example.minimaltodo.ui.components.CheckRow
import com.example.minimaltodo.ui.components.EmptyState
import com.example.minimaltodo.ui.components.SwipeToDismissItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.activeTasks.isEmpty() && uiState.completedTasks.isEmpty() && !uiState.isLoading) {
        EmptyState(
            icon = Icons.AutoMirrored.Filled.List,
            message = stringResource(R.string.empty_tasks),
            modifier = modifier.padding(32.dp),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            items(
                items = uiState.activeTasks,
                key = { it.id },
            ) { task ->
                TaskItem(
                    task = task,
                    onComplete = { viewModel.completeTask(task) },
                    onDelete = { viewModel.deleteTask(task) },
                )
            }
            if (uiState.completedTasks.isNotEmpty()) {
                item(key = "completed_header") {
                    CompletedHeader(
                        count = uiState.completedTasks.size,
                        expanded = uiState.showCompleted,
                        onToggle = viewModel::toggleShowCompleted,
                    )
                }
                if (uiState.showCompleted) {
                    items(
                        items = uiState.completedTasks,
                        key = { it.id },
                    ) { task ->
                        CheckRow(
                            text = task.title,
                            checked = true,
                            onCheckedChange = { viewModel.uncompleteTask(task) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedHeader(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.completed_count, count),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskItem(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onComplete()
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
        startToEndIcon = Icons.Default.Check,
        endToStartIcon = Icons.Default.Delete,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            CheckRow(
                text = task.title,
                checked = false,
                onCheckedChange = { onComplete() },
            )
            if (task.dueDate != null) {
                DueDateLabel(dueDate = task.dueDate)
            }
        }
    }
}

@Composable
private fun DueDateLabel(dueDate: Long) {
    val zoneId = remember { ZoneId.systemDefault() }
    val instant = remember(dueDate) { Instant.ofEpochMilli(dueDate) }
    val zonedDateTime = remember(instant, zoneId) { instant.atZone(zoneId) }
    val localDate = remember(zonedDateTime) { zonedDateTime.toLocalDate() }
    val localTime = remember(zonedDateTime) { zonedDateTime.toLocalTime() }
    val today = remember { LocalDate.now() }

    val dateText = remember(localDate, today) {
        localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    val hasTime = remember(localTime) {
        localTime.hour != 0 || localTime.minute != 0
    }
    val timeText = remember(localTime, hasTime) {
        if (hasTime) {
            localTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        } else {
            null
        }
    }

    val isOverdue = remember(localDate, today) { localDate.isBefore(today) }

    val displayText = if (timeText != null) "$dateText, $timeText" else dateText

    Text(
        text = "${stringResource(R.string.task_due_prefix)} $displayText",
        style = MaterialTheme.typography.labelMedium,
        color = if (isOverdue) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(start = 56.dp, bottom = 8.dp),
    )
}
