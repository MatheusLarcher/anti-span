package com.larchertech.antispam.ui.screens.settings

import android.Manifest
import android.app.role.RoleManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.core.content.getSystemService
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

    val roleRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { /* status real vira um checklist visível na Tarefa 6 */ }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* status real vira um checklist visível na Tarefa 6 */ }

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
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_request_call_role_title)) },
                supportingContent = { Text(stringResource(R.string.settings_request_call_role_subtitle)) },
                trailingContent = {
                    Button(onClick = {
                        val roleManager = context.getSystemService<RoleManager>()
                        val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        if (intent != null) roleRequestLauncher.launch(intent)
                    }) { Text(stringResource(R.string.action_activate)) }
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_request_contacts_title)) },
                supportingContent = { Text(stringResource(R.string.settings_request_contacts_subtitle)) },
                trailingContent = {
                    Button(onClick = {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }) { Text(stringResource(R.string.action_activate)) }
                },
            )
        }
    }
}
