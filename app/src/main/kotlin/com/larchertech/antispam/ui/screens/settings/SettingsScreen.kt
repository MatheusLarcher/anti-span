package com.larchertech.antispam.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.larchertech.antispam.AntiSpamApp
import com.larchertech.antispam.R
import com.larchertech.antispam.data.db.AllowedNumberEntity
import com.larchertech.antispam.ui.common.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onOpenOnboarding: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = (context.applicationContext as AntiSpamApp).container
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(container.settingsRepository, container.database.allowedNumberDao()),
    )

    val callBlockingEnabled by viewModel.callBlockingEnabled.collectAsStateWithLifecycle()
    val allowedNumbers by viewModel.allowedNumbers.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_call_blocking_title)) },
                    supportingContent = { Text(stringResource(R.string.settings_call_blocking_subtitle)) },
                    trailingContent = {
                        Switch(checked = callBlockingEnabled, onCheckedChange = viewModel::setCallBlockingEnabled)
                    },
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_open_onboarding_title)) },
                    supportingContent = { Text(stringResource(R.string.settings_open_onboarding_subtitle)) },
                    trailingContent = {
                        Button(onClick = onOpenOnboarding) { Text(stringResource(R.string.action_open)) }
                    },
                )
            }
            item { HorizontalDivider() }
            item {
                Text(
                    text = stringResource(R.string.settings_allowed_numbers_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
            if (allowedNumbers.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.settings_allowed_numbers_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(allowedNumbers, key = { it.phoneNumberNormalized }) { entry ->
                    AllowedNumberRow(entry = entry, onRemove = { viewModel.removeAllowedNumber(entry.phoneNumberNormalized) })
                }
            }
        }
    }
}

@Composable
private fun AllowedNumberRow(entry: AllowedNumberEntity, onRemove: () -> Unit) {
    ListItem(
        headlineContent = { Text(entry.phoneNumberNormalized) },
        supportingContent = { Text(formatTimestamp(entry.addedAt)) },
        trailingContent = {
            OutlinedButton(onClick = onRemove) { Text(stringResource(R.string.action_remove)) }
        },
    )
}
