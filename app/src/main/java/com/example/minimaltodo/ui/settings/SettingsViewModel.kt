package com.example.minimaltodo.ui.settings

import androidx.lifecycle.ViewModel
import com.example.minimaltodo.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val resetHour: Flow<Int> = settingsRepository.resetHour

    fun getResetHour(): Int = settingsRepository.getResetHour()

    fun setResetHour(hour: Int) {
        settingsRepository.setResetHour(hour)
    }
}
