package com.larchertech.antispam.ui.screens.sms

import com.larchertech.antispam.data.db.BlockedSmsEntity

data class SmsConversation(
    val phoneNumberNormalized: String,
    val messages: List<BlockedSmsEntity>,
) {
    val lastMessage: BlockedSmsEntity get() = messages.last()
}
