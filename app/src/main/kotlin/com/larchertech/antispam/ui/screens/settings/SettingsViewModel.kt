package com.larchertech.antispam.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.larchertech.antispam.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    val callBlockingEnabled: StateFlow<Boolean> = settingsRepository.callBlockingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val smsBlockingEnabled: StateFlow<Boolean> = settingsRepository.smsBlockingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setCallBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCallBlockingEnabled(enabled) }
    }

    fun setSmsBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSmsBlockingEnabled(enabled) }
    }

    class Factory(private val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository) as T
        }
    }
}
