package com.larchertech.antispam.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allowed_numbers")
data class AllowedNumberEntity(
    @PrimaryKey val phoneNumberNormalized: String,
    val addedAt: Long,
)
