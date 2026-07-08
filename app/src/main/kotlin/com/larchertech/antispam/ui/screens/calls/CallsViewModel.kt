package com.larchertech.antispam.ui.screens.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.larchertech.antispam.data.db.AllowedNumberDao
import com.larchertech.antispam.data.db.AllowedNumberEntity
import com.larchertech.antispam.data.db.BlockedCallDao
import com.larchertech.antispam.data.db.BlockedCallEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallsViewModel(
    private val blockedCallDao: BlockedCallDao,
    private val allowedNumberDao: AllowedNumberDao,
) : ViewModel() {

    val blockedCalls: StateFlow<List<BlockedCallEntity>> = blockedCallDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun allowNumber(call: BlockedCallEntity) {
        viewModelScope.launch {
            allowedNumberDao.insert(
                AllowedNumberEntity(
                    phoneNumberNormalized = call.phoneNumberNormalized,
                    addedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    class Factory(
        private val blockedCallDao: BlockedCallDao,
        private val allowedNumberDao: AllowedNumberDao,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CallsViewModel(blockedCallDao, allowedNumberDao) as T
        }
    }
}
