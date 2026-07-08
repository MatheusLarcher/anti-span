package com.larchertech.antispam.blocking

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.core.content.getSystemService

data class SimInfo(val subscriptionId: Int, val displayName: String)

/** Lista os chips ativos do aparelho. Vazio se não tiver permissão ou não houver chip ativo. */
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
}
