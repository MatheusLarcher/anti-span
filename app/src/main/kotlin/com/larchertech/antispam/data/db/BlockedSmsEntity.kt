package com.larchertech.antispam.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_sms")
data class BlockedSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumberNormalized: String,
    val phoneNumberRaw: String,
    val body: String,
    val timestamp: Long,
)
