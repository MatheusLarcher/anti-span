package com.larchertech.antispam.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedSmsDao {
    @Insert
    suspend fun insert(sms: BlockedSmsEntity)

    @Query("SELECT * FROM blocked_sms ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<BlockedSmsEntity>>
}
