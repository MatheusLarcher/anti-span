package com.larchertech.antispam.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllowedNumberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AllowedNumberEntity)

    @Query("DELETE FROM allowed_numbers WHERE phoneNumberNormalized = :phoneNumberNormalized")
    suspend fun delete(phoneNumberNormalized: String)

    @Query("SELECT * FROM allowed_numbers ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<AllowedNumberEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM allowed_numbers WHERE phoneNumberNormalized = :phoneNumberNormalized)")
    suspend fun isAllowed(phoneNumberNormalized: String): Boolean
}
