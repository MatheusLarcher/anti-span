package com.larchertech.antispam.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.larchertech.antispam.AntiSpamApp
import com.larchertech.antispam.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = (context.applicationContext as AntiSpamApp).container
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(container.settingsRepository),
    )

    val callBlockingEnabled by viewModel.callBlockingEnabled.collectAsStateWithLifecycle()
    val smsBlockingEnabled by viewModel.smsBlockingEnabled.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_call_blocking_title)) },
                supportingContent = { Text(stringResource(R.string.settings_call_blocking_subtitle)) },
                trailingContent = {
                    Switch(checked = callBlockingEnabled, onCheckedChange = viewModel::setCallBlockingEnabled)
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_sms_blocking_title)) },
                supportingContent = { Text(stringResource(R.string.settings_sms_blocking_subtitle)) },
                trailingContent = {
                    Switch(checked = smsBlockingEnabled, onCheckedChange = viewModel::setSmsBlockingEnabled)
                },
            )
        }
    }
}
