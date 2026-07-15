package com.larchertech.antispam.blocking

import android.Manifest
import android.content.pm.PackageManager
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.CallScreeningService.CallResponse
import android.util.Log
import androidx.core.content.ContextCompat
import com.larchertech.antispam.AntiSpamApp
import com.larchertech.antispam.data.db.BlockedCallEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AntiSpamCallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val rawNumber = callDetails.handle?.schemeSpecificPart

        if (rawNumber.isNullOrBlank()) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val container = (application as AntiSpamApp).container

        serviceScope.launch {
            var responded = false
            try {
                val blockingEnabled = container.settingsRepository.callBlockingEnabled.first()
                val hasContactsPermission = ContextCompat.checkSelfPermission(
                    this@AntiSpamCallScreeningService,
                    Manifest.permission.READ_CONTACTS,
                ) == PackageManager.PERMISSION_GRANTED
                val normalized = PhoneNumbers.normalize(rawNumber)
                val known = !blockingEnabled || !hasContactsPermission ||
                    PhoneNumbers.isSavedContact(this@AntiSpamCallScreeningService, rawNumber) ||
                    container.database.allowedNumberDao().isAllowed(normalized)

                if (known) {
                    respondToCall(callDetails, CallResponse.Builder().build())
                    responded = true
                } else {
                    respondToCall(
                        callDetails,
                        CallResponse.Builder()
                            .setDisallowCall(true)
                            .setRejectCall(true)
                            .setSkipNotification(true)
                            .setSkipCallLog(true)
                            .build(),
                    )
                    responded = true
                    container.database.blockedCallDao().insert(
                        BlockedCallEntity(
                            phoneNumberNormalized = normalized,
                            phoneNumberRaw = rawNumber,
                            timestamp = System.currentTimeMillis(),
                        ),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao avaliar chamada, deixando tocar normalmente", e)
                if (!responded) respondToCall(callDetails, CallResponse.Builder().build())
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "AntiSpamCallScreening"
    }
}
