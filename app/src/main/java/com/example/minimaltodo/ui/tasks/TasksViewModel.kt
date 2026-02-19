package com.example.minimaltodo.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimaltodo.data.entity.Task
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
class TasksViewModel @Inject constructor(
    application: Application,
    private val taskRepository: TaskRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState

    init {
        combine(
            taskRepository.observeActiveTasks(),
            taskRepository.observeCompletedTasks(),
        ) { active, completed ->
            _uiState.update { state ->
                state.copy(
                    activeTasks = active,
                    completedTasks = completed,
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun addTask(title: String, dueDate: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.addTask(title = title, dueDate = dueDate)
            WidgetRefresh.refreshTasks(getApplication())
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.completeTask(task)
            WidgetRefresh.refreshTasks(getApplication())
        }
    }

    fun uncompleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.uncompleteTask(task)
            WidgetRefresh.refreshTasks(getApplication())
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            WidgetRefresh.refreshTasks(getApplication())
        }
    }

    fun toggleShowCompleted() {
        _uiState.update { it.copy(showCompleted = !it.showCompleted) }
    }
}
