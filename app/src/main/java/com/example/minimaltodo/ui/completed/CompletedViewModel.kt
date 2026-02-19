package com.example.minimaltodo.ui.completed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimaltodo.data.entity.Task
import com.example.minimaltodo.data.repository.TaskRepository
import com.example.minimaltodo.widget.WidgetRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedViewModel @Inject constructor(
    application: Application,
    private val taskRepository: TaskRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CompletedUiState())
    val uiState: StateFlow<CompletedUiState> = _uiState

    init {
        taskRepository.observeCompletedTasks()
            .onEach { tasks ->
                _uiState.update { state ->
                    state.copy(
                        completedTasks = tasks,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
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
}
