package com.larchertech.antispam.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.larchertech.antispam.data.db.AllowedNumberDao
import com.larchertech.antispam.data.db.AllowedNumberEntity
import com.larchertech.antispam.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val allowedNumberDao: AllowedNumberDao,
) : ViewModel() {
    val callBlockingEnabled: StateFlow<Boolean> = settingsRepository.callBlockingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val allowedNumbers: StateFlow<List<AllowedNumberEntity>> = allowedNumberDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCallBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCallBlockingEnabled(enabled) }
    }

    fun removeAllowedNumber(phoneNumberNormalized: String) {
        viewModelScope.launch { allowedNumberDao.delete(phoneNumberNormalized) }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val allowedNumberDao: AllowedNumberDao,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, allowedNumberDao) as T
        }
    }
}
