package com.example.minimaltodo.ui.deleted

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimaltodo.data.entity.Goal
import com.example.minimaltodo.data.entity.Task
import com.example.minimaltodo.data.repository.GoalRepository
import com.example.minimaltodo.data.repository.TaskRepository
import com.example.minimaltodo.widget.WidgetRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeletedViewModel @Inject constructor(
    application: Application,
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DeletedUiState())
    val uiState: StateFlow<DeletedUiState> = _uiState

    init {
        combine(
            goalRepository.observeDeletedGoals(),
            taskRepository.observeDeletedTasks(),
        ) { goals, tasks ->
            _uiState.update { state ->
                state.copy(
                    deletedGoals = goals,
                    deletedTasks = tasks,
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun restoreGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.restoreGoal(goal)
            WidgetRefresh.refreshGoals(getApplication())
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            taskRepository.restoreTask(task)
            WidgetRefresh.refreshTasks(getApplication())
        }
    }

    fun permanentlyDeleteGoal(goal: Goal) {
        viewModelScope.launch { goalRepository.permanentlyDeleteGoal(goal) }
    }

    fun permanentlyDeleteTask(task: Task) {
        viewModelScope.launch { taskRepository.permanentlyDeleteTask(task) }
    }

    fun emptyAll() {
        viewModelScope.launch {
            goalRepository.emptyDeletedGoals()
            taskRepository.emptyDeletedTasks()
        }
    }
}
