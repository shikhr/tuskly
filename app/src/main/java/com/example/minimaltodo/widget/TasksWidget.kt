package com.example.minimaltodo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.minimaltodo.MainActivity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val rows = parseTaskRows(prefs)
                TasksWidgetContent(rows)
            }
        }
    }

    companion object {
        val TASKS_DATA_KEY = stringPreferencesKey("tasks_data")

        suspend fun refreshAll(context: Context) {
            val data = WidgetDataHelper.buildTasksDataString(context)
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(TasksWidget::class.java)
            ids.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[TASKS_DATA_KEY] = data
                }
            }
            TasksWidget().updateAll(context)
        }

        suspend fun refresh(context: Context, glanceId: GlanceId) {
            val data = WidgetDataHelper.buildTasksDataString(context)
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[TASKS_DATA_KEY] = data
            }
            TasksWidget().update(context, glanceId)
        }
    }
}

// ---------- lightweight row model ----------

data class TaskWidgetRow(
    val id: Long,
    val title: String,
    val dueDate: Long?, // epoch millis, null = no due date
)

/**
 * Serialisation format (one row per line):
 * `id|title|dueDate`
 * where dueDate is epoch millis or empty string for null.
 */
private fun parseTaskRows(prefs: Preferences): List<TaskWidgetRow> {
    val raw = prefs[TasksWidget.TASKS_DATA_KEY] ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return raw.lines().mapNotNull { line ->
        val parts = line.split("|")
        if (parts.size < 3) return@mapNotNull null
        TaskWidgetRow(
            id = parts[0].toLongOrNull() ?: return@mapNotNull null,
            title = parts[1],
            dueDate = parts[2].toLongOrNull(), // empty string → null
        )
    }
}

// ---------- colours ----------

private val redColor = ColorProvider(
    day = androidx.compose.ui.graphics.Color(0xFFF44336),
    night = androidx.compose.ui.graphics.Color(0xFFEF9A9A),
)
private val taskDimColor = ColorProvider(
    day = androidx.compose.ui.graphics.Color(0xFF888888),
    night = androidx.compose.ui.graphics.Color(0xFFAAAAAA),
)
private val taskHeaderColor = ColorProvider(
    day = androidx.compose.ui.graphics.Color(0xFF000000),
    night = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
)
private val taskTransparentBg = ColorProvider(
    day = androidx.compose.ui.graphics.Color.Transparent,
    night = androidx.compose.ui.graphics.Color.Transparent,
)
private val DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d")

// ---------- composables ----------

@Composable
private fun TasksWidgetContent(tasks: List<TaskWidgetRow>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(taskTransparentBg)
            .padding(12.dp),
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity<MainActivity>())
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tasks",
                style = TextStyle(
                    color = taskHeaderColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${tasks.size}",
                style = TextStyle(color = taskDimColor, fontSize = 13.sp),
            )
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No tasks",
                    style = TextStyle(color = taskDimColor, fontSize = 14.sp),
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(tasks, itemId = { it.id }) { task ->
                    TaskRow(task)
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskWidgetRow) {
    val isOverdue = task.dueDate?.let { dueMs ->
        val dueDate = Instant.ofEpochMilli(dueMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        dueDate.isBefore(LocalDate.now())
    } ?: false

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                actionRunCallback<ToggleTaskAction>(
                    actionParametersOf(TaskIdKey to task.id),
                ),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\u25CB",
            style = TextStyle(
                color = taskDimColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = task.title,
            style = TextStyle(color = taskHeaderColor, fontSize = 14.sp),
            modifier = GlanceModifier.defaultWeight(),
        )
        if (task.dueDate != null) {
            val dateStr = Instant.ofEpochMilli(task.dueDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DUE_DATE_FORMAT)
            Text(
                text = dateStr,
                style = TextStyle(
                    color = if (isOverdue) redColor else taskDimColor,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

// ---------- action ----------

val TaskIdKey = ActionParameters.Key<Long>("task_id")

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[TaskIdKey] ?: return
        WidgetDataHelper.toggleTaskCompletion(context, taskId)
        TasksWidget.refresh(context, glanceId)
    }
}
