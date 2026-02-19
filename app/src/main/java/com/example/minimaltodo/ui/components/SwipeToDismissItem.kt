package com.example.minimaltodo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissItem(
    state: SwipeToDismissBoxState,
    modifier: Modifier = Modifier,
    startToEndIcon: ImageVector? = null,
    startToEndColor: Color = MaterialTheme.colorScheme.secondary,
    endToStartIcon: ImageVector? = null,
    endToStartColor: Color = MaterialTheme.colorScheme.error,
    content: @Composable () -> Unit,
) {
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        backgroundContent = {
            val direction = state.dismissDirection
            val color by animateColorAsState(
                targetValue = when (state.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    SwipeToDismissBoxValue.StartToEnd -> startToEndColor
                    SwipeToDismissBoxValue.EndToStart -> endToStartColor
                },
                label = "swipe_bg_color",
            )
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> startToEndIcon
                SwipeToDismissBoxValue.EndToStart -> endToStartIcon
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        },
        content = { content() },
    )
}
