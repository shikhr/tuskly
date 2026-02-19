package com.example.minimaltodo.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.minimaltodo.R
import com.example.minimaltodo.util.formatResetHour

@Composable
fun SettingsScreen(
    isDynamicColor: Boolean,
    onDynamicColorToggle: (Boolean) -> Unit,
    resetHour: Int,
    onResetHourChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHourPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        // ---- Appearance section ----
        SettingsItem(
            title = stringResource(R.string.settings_dynamic_color),
            subtitle = stringResource(R.string.settings_dynamic_color_desc),
        ) {
            Switch(
                checked = isDynamicColor,
                onCheckedChange = onDynamicColorToggle,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // ---- Goals section ----
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.settings_goals_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        SettingsClickableItem(
            title = stringResource(R.string.settings_reset_time),
            subtitle = stringResource(R.string.settings_reset_time_desc),
            value = formatResetHour(resetHour),
            onClick = { showHourPicker = true },
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // ---- About section ----
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.settings_about_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        SettingsInfoItem(
            title = stringResource(R.string.settings_version),
            value = stringResource(R.string.settings_version_value),
        )
        SettingsInfoItem(
            title = stringResource(R.string.settings_data_storage),
            value = stringResource(R.string.settings_data_storage_value),
        )
    }

    if (showHourPicker) {
        HourPickerDialog(
            selectedHour = resetHour,
            onHourSelected = { hour ->
                onResetHourChange(hour)
                showHourPicker = false
            },
            onDismiss = { showHourPicker = false },
        )
    }
}

@Composable
private fun HourPickerDialog(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val hours = (0..23).toList()
    val listState = rememberLazyListState()

    // Scroll to the currently selected hour on open
    LaunchedEffect(selectedHour) {
        listState.scrollToItem(maxOf(0, selectedHour - 2))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reset_time)) },
        text = {
            LazyColumn(
                state = listState,
                modifier = Modifier.height(300.dp),
            ) {
                itemsIndexed(hours) { _, hour ->
                    val isSelected = hour == selectedHour
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHourSelected(hour) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatResetHour(hour),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing()
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
