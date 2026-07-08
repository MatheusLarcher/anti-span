package com.larchertech.antispam.blocking

import android.content.Context
import android.content.Intent
import android.telecom.Call
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService

data class SimInfo(val subscriptionId: Int, val displayName: String)

/** Lista e identifica chips ativos, e resolve por qual chip uma ligação/SMS chegou. */
object SimSubscriptions {

    fun listActive(context: Context): List<SimInfo> {
        if (!PermissionStatus.hasReadPhoneStatePermission(context)) return emptyList()
        val subscriptionManager = context.getSystemService<SubscriptionManager>() ?: return emptyList()
        return try {
            subscriptionManager.activeSubscriptionInfoList.orEmpty().map { info ->
                SimInfo(
                    subscriptionId = info.subscriptionId,
                    displayName = info.displayName?.toString()?.takeIf { it.isNotBlank() }
                        ?: "Chip ${info.simSlotIndex + 1}",
                )
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    /** Chip por onde a ligação chegou, ou null se não for possível identificar. */
    fun resolveCallSubscriptionId(context: Context, callDetails: Call.Details): Int? {
        val accountHandle = callDetails.accountHandle ?: return null
        val telephonyManager = context.getSystemService<TelephonyManager>() ?: return null
        return try {
            val subscriptionId = telephonyManager.getSubscriptionId(accountHandle)
            subscriptionId.takeIf { it != SubscriptionManager.INVALID_SUBSCRIPTION_ID }
        } catch (e: SecurityException) {
            null
        }
    }

    /** Chip por onde o SMS chegou, ou null se não for possível identificar (aparelho de chip único). */
    fun resolveSmsSubscriptionId(intent: Intent): Int? {
        val subscriptionId = intent.getIntExtra(
            SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX,
            SubscriptionManager.INVALID_SUBSCRIPTION_ID,
        )
        return subscriptionId.takeIf { it != SubscriptionManager.INVALID_SUBSCRIPTION_ID }
    }

    /**
     * `selectedSubscriptionId` null = "Todos os chips" (sempre bate). Caso contrário, só bate se
     * o chip do evento for identificável e igual ao selecionado — chip não identificável é
     * tratado como "diferente do selecionado" (decisão da spec: nunca bloquear por engano o chip
     * que o usuário pediu pra deixar de fora).
     */
    fun matchesFilter(selectedSubscriptionId: Int?, eventSubscriptionId: Int?): Boolean {
        if (selectedSubscriptionId == null) return true
        return eventSubscriptionId == selectedSubscriptionId
    }
}
