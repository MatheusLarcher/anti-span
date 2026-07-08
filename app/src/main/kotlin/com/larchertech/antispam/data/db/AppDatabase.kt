package com.larchertech.antispam.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BlockedCallEntity::class, BlockedSmsEntity::class, AllowedNumberEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun blockedSmsDao(): BlockedSmsDao
    abstract fun allowedNumberDao(): AllowedNumberDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anti-spam.db",
                ).build().also { instance = it }
            }
        }
    }
}
