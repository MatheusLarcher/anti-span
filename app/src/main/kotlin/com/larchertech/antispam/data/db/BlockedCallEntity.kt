package com.larchertech.antispam.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_calls")
data class BlockedCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumberNormalized: String,
    val phoneNumberRaw: String,
    val timestamp: Long,
)
