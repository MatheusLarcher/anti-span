package com.larchertech.antispam.ui.screens.calls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.larchertech.antispam.AntiSpamApp
import com.larchertech.antispam.R
import com.larchertech.antispam.blocking.PermissionStatus
import com.larchertech.antispam.data.db.BlockedCallEntity
import com.larchertech.antispam.ui.common.ProtectionBanner
import com.larchertech.antispam.ui.common.formatTimestamp
import com.larchertech.antispam.ui.common.rememberRefreshOnResume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(onOpenOnboarding: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = (context.applicationContext as AntiSpamApp).container
    val viewModel: CallsViewModel = viewModel(
        factory = CallsViewModel.Factory(
            container.database.blockedCallDao(),
            container.database.allowedNumberDao(),
        ),
    )

    val calls by viewModel.blockedCalls.collectAsStateWithLifecycle()
    val resumeCounter by rememberRefreshOnResume()
    val protectionComplete = remember(resumeCounter) { PermissionStatus.isCallProtectionComplete(context) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.calls_title)) }) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!protectionComplete) {
                ProtectionBanner(onClick = onOpenOnboarding)
            }
            if (calls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.calls_empty))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(calls, key = { it.id }) { call ->
                        BlockedCallRow(call = call, onAllow = { viewModel.allowNumber(call) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedCallRow(call: BlockedCallEntity, onAllow: () -> Unit) {
    ListItem(
        headlineContent = { Text(call.phoneNumberRaw) },
        supportingContent = { Text(formatTimestamp(call.timestamp)) },
        trailingContent = {
            Button(onClick = onAllow) { Text(stringResource(R.string.action_allow_number)) }
        },
    )
}
