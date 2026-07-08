package com.larchertech.antispam.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val CALL_BLOCKING_ENABLED = booleanPreferencesKey("call_blocking_enabled")
        val SMS_BLOCKING_ENABLED = booleanPreferencesKey("sms_blocking_enabled")
        val CALL_BLOCKING_SUBSCRIPTION_ID = intPreferencesKey("call_blocking_subscription_id")
        val SMS_BLOCKING_SUBSCRIPTION_ID = intPreferencesKey("sms_blocking_subscription_id")
    }

    val callBlockingEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.CALL_BLOCKING_ENABLED] ?: true }

    val smsBlockingEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SMS_BLOCKING_ENABLED] ?: true }

    /** null = "Todos os chips" (sem filtro). */
    val callBlockingSubscriptionId: Flow<Int?> =
        context.dataStore.data.map { it[Keys.CALL_BLOCKING_SUBSCRIPTION_ID] }

    /** null = "Todos os chips" (sem filtro). */
    val smsBlockingSubscriptionId: Flow<Int?> =
        context.dataStore.data.map { it[Keys.SMS_BLOCKING_SUBSCRIPTION_ID] }

    suspend fun setCallBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CALL_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setSmsBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SMS_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setCallBlockingSubscriptionId(subscriptionId: Int?) {
        context.dataStore.edit {
            if (subscriptionId == null) it.remove(Keys.CALL_BLOCKING_SUBSCRIPTION_ID)
            else it[Keys.CALL_BLOCKING_SUBSCRIPTION_ID] = subscriptionId
        }
    }

    suspend fun setSmsBlockingSubscriptionId(subscriptionId: Int?) {
        context.dataStore.edit {
            if (subscriptionId == null) it.remove(Keys.SMS_BLOCKING_SUBSCRIPTION_ID)
            else it[Keys.SMS_BLOCKING_SUBSCRIPTION_ID] = subscriptionId
        }
    }
}
