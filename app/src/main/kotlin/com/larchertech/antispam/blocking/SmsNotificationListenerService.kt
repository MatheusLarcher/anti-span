package com.larchertech.antispam.blocking

import android.app.Notification
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class SmsNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
        if (defaultSmsPackage == null || sbn.packageName != defaultSmsPackage) return

        val title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        if (title.isNullOrBlank()) return

        val normalized = PhoneNumbers.normalize(title)
        if (RecentUnknownSenders.isRecentlyUnknown(normalized)) {
            cancelNotification(sbn.key)
        }
    }
}
