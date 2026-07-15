package com.larchertech.antispam.blocking

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils

/** Normalização e checagem de contato para números de telefone de ligações recebidas. */
object PhoneNumbers {

    fun normalize(rawNumber: String): String {
        return PhoneNumberUtils.stripSeparators(rawNumber)
    }

    /** Usa o mesmo mecanismo de lookup do discador nativo do Android (lida com variações de formatação). */
    fun isSavedContact(context: Context, rawNumber: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(rawNumber),
        )
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)
        return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            cursor.count > 0
        } ?: false
    }
}
