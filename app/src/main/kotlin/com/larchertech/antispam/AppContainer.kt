package com.larchertech.antispam

import android.content.Context
import com.larchertech.antispam.data.db.AppDatabase
import com.larchertech.antispam.data.settings.SettingsRepository

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val settingsRepository: SettingsRepository = SettingsRepository(context)
}
