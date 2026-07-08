package com.larchertech.antispam.ui.screens.onboarding

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.larchertech.antispam.R
import com.larchertech.antispam.blocking.PermissionStatus
import com.larchertech.antispam.ui.common.rememberRefreshOnResume

private data class ChecklistItem(
    val title: String,
    val subtitle: String,
    val granted: Boolean,
    val onRequest: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resumeCounter by rememberRefreshOnResume()

    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}
    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}
    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {}

    val hasContacts = remember(resumeCounter) { PermissionStatus.hasContactsPermission(context) }
    val hasCallRole = remember(resumeCounter) { PermissionStatus.hasCallScreeningRole(context) }
    val hasSms = remember(resumeCounter) { PermissionStatus.hasReceiveSmsPermission(context) }
    val hasNotificationAccess = remember(resumeCounter) { PermissionStatus.hasNotificationAccess(context) }
    val hasBatteryExemption = remember(resumeCounter) { PermissionStatus.isIgnoringBatteryOptimizations(context) }

    val items = listOf(
        ChecklistItem(
            title = stringResource(R.string.onboarding_contacts_title),
            subtitle = stringResource(R.string.onboarding_contacts_subtitle),
            granted = hasContacts,
            onRequest = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) },
        ),
        ChecklistItem(
            title = stringResource(R.string.onboarding_call_role_title),
            subtitle = stringResource(R.string.onboarding_call_role_subtitle),
            granted = hasCallRole,
            onRequest = {
                val roleManager = context.getSystemService<RoleManager>()
                val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                if (intent != null) roleLauncher.launch(intent)
            },
        ),
        ChecklistItem(
            title = stringResource(R.string.onboarding_sms_title),
            subtitle = stringResource(R.string.onboarding_sms_subtitle),
            granted = hasSms,
            onRequest = { smsLauncher.launch(Manifest.permission.RECEIVE_SMS) },
        ),
        ChecklistItem(
            title = stringResource(R.string.onboarding_notification_access_title),
            subtitle = stringResource(R.string.onboarding_notification_access_subtitle),
            granted = hasNotificationAccess,
            onRequest = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
        ),
        ChecklistItem(
            title = stringResource(R.string.onboarding_battery_title),
            subtitle = stringResource(R.string.onboarding_battery_subtitle),
            granted = hasBatteryExemption,
            onRequest = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            },
        ),
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.onboarding_title)) }) },
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) { Text(stringResource(R.string.onboarding_continue)) }
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            item {
                Text(
                    stringResource(R.string.onboarding_intro),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            items(items) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = { Text(item.subtitle) },
                    trailingContent = {
                        if (item.granted) {
                            Column {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = stringResource(R.string.onboarding_status_granted),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        } else {
                            Button(onClick = item.onRequest) { Text(stringResource(R.string.action_activate)) }
                        }
                    },
                )
            }
        }
    }
}
