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

class GoalsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val rows = parseGoalRows(prefs)
                GoalsWidgetContent(rows)
            }
        }
    }

    companion object {
        /** Preference key storing serialized goal rows. */
        val GOALS_DATA_KEY = stringPreferencesKey("goals_data")

        /**
         * Push the latest goal data from Room into Glance state for all widget instances,
         * then trigger a UI recomposition.
         */
        suspend fun refreshAll(context: Context) {
            val data = WidgetDataHelper.buildGoalsDataString(context)
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(GoalsWidget::class.java)
            ids.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[GOALS_DATA_KEY] = data
                }
            }
            GoalsWidget().updateAll(context)
        }

        /** Refresh a single widget instance. */
        suspend fun refresh(context: Context, glanceId: GlanceId) {
            val data = WidgetDataHelper.buildGoalsDataString(context)
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[GOALS_DATA_KEY] = data
            }
            GoalsWidget().update(context, glanceId)
        }
    }
}

// ---------- lightweight row model for the widget ----------

/** Flat model holding only what the widget needs to render a single goal row. */
data class GoalWidgetRow(
    val id: Long,
    val name: String,
    val isBinary: Boolean,
    val targetValue: Float,
    val currentValue: Float,
    val isCompleted: Boolean,
)

/**
 * Serialisation format (one row per line):
 * `id|name|isBinary|targetValue|currentValue|isCompleted`
 *
 * The pipe character (`|`) is safe because goal names do not contain it
 * (and it keeps the logic dependency-free).
 */
private fun parseGoalRows(prefs: Preferences): List<GoalWidgetRow> {
    val raw = prefs[GoalsWidget.GOALS_DATA_KEY] ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return raw.lines().mapNotNull { line ->
        val parts = line.split("|")
        if (parts.size < 6) return@mapNotNull null
        GoalWidgetRow(
            id = parts[0].toLongOrNull() ?: return@mapNotNull null,
            name = parts[1],
            isBinary = parts[2].toBooleanStrictOrNull() ?: return@mapNotNull null,
            targetValue = parts[3].toFloatOrNull() ?: return@mapNotNull null,
            currentValue = parts[4].toFloatOrNull() ?: return@mapNotNull null,
            isCompleted = parts[5].toBooleanStrictOrNull() ?: return@mapNotNull null,
        )
    }
}

// ---------- colours ----------

private val greenColor = ColorProvider(
    day = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    night = androidx.compose.ui.graphics.Color(0xFF81C784),
)
private val dimColor = ColorProvider(
    day = androidx.compose.ui.graphics.Color(0xFF888888),
    night = androidx.compose.ui.graphics.Color(0xFFAAAAAA),
)
private val headerColor = ColorProvider(
    day = androidx.compose.ui.graphics.Color(0xFF000000),
    night = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
)
private val transparentBg = ColorProvider(
    day = androidx.compose.ui.graphics.Color.Transparent,
    night = androidx.compose.ui.graphics.Color.Transparent,
)

// ---------- composables ----------

@Composable
private fun GoalsWidgetContent(goals: List<GoalWidgetRow>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(transparentBg)
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
                text = "Daily Goals",
                style = TextStyle(
                    color = headerColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            val completedCount = goals.count { it.isCompleted }
            Text(
                text = "$completedCount/${goals.size}",
                style = TextStyle(color = dimColor, fontSize = 13.sp),
            )
        }

        if (goals.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No goals yet",
                    style = TextStyle(color = dimColor, fontSize = 14.sp),
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(goals, itemId = { it.id }) { goal ->
                    GoalRow(goal)
                }
            }
        }
    }
}

@Composable
private fun GoalRow(goal: GoalWidgetRow) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                actionRunCallback<ToggleGoalAction>(
                    actionParametersOf(GoalIdKey to goal.id),
                ),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (goal.isCompleted) "\u2713" else "\u25CB",
            style = TextStyle(
                color = if (goal.isCompleted) greenColor else dimColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = goal.name,
            style = TextStyle(
                color = if (goal.isCompleted) dimColor else headerColor,
                fontSize = 14.sp,
            ),
            modifier = GlanceModifier.defaultWeight(),
        )
        if (!goal.isBinary) {
            Text(
                text = "${goal.currentValue.toInt()}/${goal.targetValue.toInt()}",
                style = TextStyle(
                    color = if (goal.isCompleted) greenColor else dimColor,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

// ---------- action ----------

val GoalIdKey = ActionParameters.Key<Long>("goal_id")

class ToggleGoalAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val goalId = parameters[GoalIdKey] ?: return
        // 1. Mutate Room DB
        WidgetDataHelper.cycleGoalProgress(context, goalId)
        // 2. Push fresh snapshot into Glance state & recompose
        GoalsWidget.refresh(context, glanceId)
    }
}
