package com.example.minimaltodo.ui.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimaltodo.data.entity.Goal
import com.example.minimaltodo.data.entity.TargetType
import com.example.minimaltodo.data.repository.GoalRepository
import com.example.minimaltodo.data.repository.SettingsRepository
import com.example.minimaltodo.util.logicalDateFlow
import com.example.minimaltodo.util.logicalDateString
import com.example.minimaltodo.widget.WidgetRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    application: Application,
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState

    private val logicalDate = logicalDateFlow(settingsRepository.resetHour)

    init {
        observeGoalsAndLogs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeGoalsAndLogs() {
        combine(
            goalRepository.observeActiveGoals(),
            logicalDate.flatMapLatest { date ->
                goalRepository.observeCompletionLogsForDate(date)
            },
        ) { goals, logs ->
            val logsByGoalId = logs.associateBy { it.goalId }
            _uiState.update { state ->
                state.copy(
                    goals = goals,
                    completionLogs = logsByGoalId,
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun addGoal(name: String, targetType: TargetType = TargetType.BINARY, targetValue: Float = 1f) {
        if (name.isBlank()) return
        viewModelScope.launch {
            goalRepository.addGoal(
                name = name,
                targetType = targetType,
                targetValue = targetValue,
                unit = "",
            )
            WidgetRefresh.refreshGoals(getApplication())
        }
    }

    fun toggleGoalCompletion(goal: Goal) {
        val today = logicalDateString(settingsRepository.getResetHour())
        val currentValue = _uiState.value.completionLogs[goal.id]?.value ?: 0f
        viewModelScope.launch {
            goalRepository.toggleCompletion(goal.id, today, currentValue, goal.targetValue)
            WidgetRefresh.refreshGoals(getApplication())
        }
    }

    /** Set progress to an exact value for quantity goals. Supports sliding in both directions. */
    fun setProgress(goal: Goal, value: Float) {
        val today = logicalDateString(settingsRepository.getResetHour())
        viewModelScope.launch {
            goalRepository.updateProgress(goal.id, today, value, goal.targetValue)
            WidgetRefresh.refreshGoals(getApplication())
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
            WidgetRefresh.refreshGoals(getApplication())
        }
    }
}
