package com.larchertech.antispam.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val CALL_BLOCKING_ENABLED = booleanPreferencesKey("call_blocking_enabled")
    }

    val callBlockingEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.CALL_BLOCKING_ENABLED] ?: true }

    suspend fun setCallBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CALL_BLOCKING_ENABLED] = enabled }
    }
}
