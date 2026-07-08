package com.larchertech.antispam.ui.screens.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.larchertech.antispam.data.db.AllowedNumberDao
import com.larchertech.antispam.data.db.AllowedNumberEntity
import com.larchertech.antispam.data.db.BlockedSmsDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SmsViewModel(
    private val blockedSmsDao: BlockedSmsDao,
    private val allowedNumberDao: AllowedNumberDao,
) : ViewModel() {

    val conversations: StateFlow<List<SmsConversation>> = blockedSmsDao.observeAll()
        .map { messages ->
            messages.groupBy { it.phoneNumberNormalized }
                .map { (number, msgs) -> SmsConversation(number, msgs) }
                .sortedByDescending { it.lastMessage.timestamp }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun allowNumber(phoneNumberNormalized: String) {
        viewModelScope.launch {
            allowedNumberDao.insert(
                AllowedNumberEntity(
                    phoneNumberNormalized = phoneNumberNormalized,
                    addedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    class Factory(
        private val blockedSmsDao: BlockedSmsDao,
        private val allowedNumberDao: AllowedNumberDao,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SmsViewModel(blockedSmsDao, allowedNumberDao) as T
        }
    }
}
