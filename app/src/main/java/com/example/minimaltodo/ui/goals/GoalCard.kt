package com.example.minimaltodo.ui.goals

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minimaltodo.R
import com.example.minimaltodo.data.entity.CompletionLog
import com.example.minimaltodo.data.entity.Goal
import com.example.minimaltodo.data.entity.TargetType
import kotlin.math.roundToInt

@Composable
fun GoalCard(
    goal: Goal,
    completionLog: CompletionLog?,
    onToggle: () -> Unit,
    onSetProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleted = completionLog?.isCompleted == true
    val currentValue = completionLog?.value ?: 0f

    val cardColor by animateColorAsState(
        targetValue = if (isCompleted) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "card_color",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        when (goal.targetType) {
            TargetType.BINARY -> BinaryGoalContent(
                name = goal.name,
                isCompleted = isCompleted,
                onToggle = onToggle,
            )
            TargetType.QUANTITY -> QuantityGoalContent(
                name = goal.name,
                currentValue = currentValue,
                targetValue = goal.targetValue,
                isCompleted = isCompleted,
                onValueChange = onSetProgress,
            )
            TargetType.TIMER -> BinaryGoalContent(
                name = goal.name,
                isCompleted = isCompleted,
                onToggle = onToggle,
            )
        }
    }
}

@Composable
private fun BinaryGoalContent(
    name: String,
    isCompleted: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompletionCircle(
            isCompleted = isCompleted,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
            color = if (isCompleted) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun QuantityGoalContent(
    name: String,
    currentValue: Float,
    targetValue: Float,
    isCompleted: Boolean,
    onValueChange: (Float) -> Unit,
) {
    val filledCount = remember(currentValue) { currentValue.toInt() }
    val totalCount = remember(targetValue) { targetValue.toInt() }

    // Local slider state for smooth dragging; commits on release
    var sliderPosition by remember(currentValue) { mutableFloatStateOf(currentValue) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = stringResource(R.string.goal_progress_format, sliderPosition.roundToInt(), totalCount),
                style = MaterialTheme.typography.labelLarge,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                val snapped = sliderPosition.roundToInt().toFloat()
                sliderPosition = snapped
                onValueChange(snapped)
            },
            valueRange = 0f..targetValue,
            steps = totalCount - 1,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = if (isCompleted) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                activeTrackColor = if (isCompleted) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
        )
    }
}

@Composable
private fun CompletionCircle(
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        },
        label = "circle_bg",
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
