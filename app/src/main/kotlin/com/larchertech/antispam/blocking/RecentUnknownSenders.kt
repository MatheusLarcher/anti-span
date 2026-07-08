package com.larchertech.antispam.blocking

import java.util.concurrent.ConcurrentHashMap

/**
 * Correlaciona o SmsReceivedReceiver (que sabe o corpo/remetente confiável, vindo do PDU) com o
 * SmsNotificationListenerService (que só vê a notificação, sem tempo de consultar contato/DB de
 * novo). TTL curto porque os dois eventos chegam a poucos milissegundos de distância.
 */
object RecentUnknownSenders {
    private const val TTL_MILLIS = 10_000L
    private val markedAt = ConcurrentHashMap<String, Long>()

    fun markUnknown(normalizedNumber: String) {
        markedAt[normalizedNumber] = System.currentTimeMillis()
    }

    fun isRecentlyUnknown(normalizedNumber: String): Boolean {
        val markedTimestamp = markedAt[normalizedNumber] ?: return false
        val stillValid = System.currentTimeMillis() - markedTimestamp <= TTL_MILLIS
        if (!stillValid) markedAt.remove(normalizedNumber)
        return stillValid
    }
}
