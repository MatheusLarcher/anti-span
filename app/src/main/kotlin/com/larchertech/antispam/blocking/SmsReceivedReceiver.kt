package com.larchertech.antispam.blocking

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.larchertech.antispam.AntiSpamApp
import com.larchertech.antispam.data.db.BlockedSmsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceivedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val rawNumber = messages[0].originatingAddress ?: return
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }

        val container = (context.applicationContext as AntiSpamApp).container
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val blockingEnabled = container.settingsRepository.smsBlockingEnabled.first()
                val hasContactsPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS,
                ) == PackageManager.PERMISSION_GRANTED
                val normalized = PhoneNumbers.normalize(rawNumber)
                val selectedSubscriptionId = container.settingsRepository.smsBlockingSubscriptionId.first()
                val eventSubscriptionId = SimSubscriptions.resolveSmsSubscriptionId(intent)
                val chipMatches = SimSubscriptions.matchesFilter(selectedSubscriptionId, eventSubscriptionId)
                val known = !blockingEnabled || !hasContactsPermission || !chipMatches ||
                    PhoneNumbers.isSavedContact(context, rawNumber) ||
                    container.database.allowedNumberDao().isAllowed(normalized)

                if (!known) {
                    RecentUnknownSenders.markUnknown(normalized)
                    container.database.blockedSmsDao().insert(
                        BlockedSmsEntity(
                            phoneNumberNormalized = normalized,
                            phoneNumberRaw = rawNumber,
                            body = body,
                            timestamp = System.currentTimeMillis(),
                        ),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao processar SMS recebido", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SmsReceivedReceiver"
    }
}
