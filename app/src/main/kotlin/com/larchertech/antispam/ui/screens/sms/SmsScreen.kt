package com.larchertech.antispam.ui.screens.sms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.larchertech.antispam.ui.common.ProtectionBanner
import com.larchertech.antispam.ui.common.formatTimestamp
import com.larchertech.antispam.ui.common.rememberRefreshOnResume

@Composable
fun SmsScreen(onOpenOnboarding: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = (context.applicationContext as AntiSpamApp).container
    val viewModel: SmsViewModel = viewModel(
        factory = SmsViewModel.Factory(
            container.database.blockedSmsDao(),
            container.database.allowedNumberDao(),
        ),
    )

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    var openedNumber by rememberSaveable { mutableStateOf<String?>(null) }
    val openedConversation = conversations.find { it.phoneNumberNormalized == openedNumber }
    val resumeCounter by rememberRefreshOnResume()
    val protectionComplete = remember(resumeCounter) { PermissionStatus.isSmsProtectionComplete(context) }

    if (openedConversation == null) {
        SmsConversationListScreen(
            modifier = modifier,
            conversations = conversations,
            onOpenConversation = { openedNumber = it },
            showProtectionBanner = !protectionComplete,
            onOpenOnboarding = onOpenOnboarding,
        )
    } else {
        SmsConversationDetailScreen(
            modifier = modifier,
            conversation = openedConversation,
            onBack = { openedNumber = null },
            onAllow = {
                viewModel.allowNumber(openedConversation.phoneNumberNormalized)
                openedNumber = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmsConversationListScreen(
    conversations: List<SmsConversation>,
    onOpenConversation: (String) -> Unit,
    showProtectionBanner: Boolean,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.sms_title)) }) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (showProtectionBanner) {
                ProtectionBanner(onClick = onOpenOnboarding)
            }
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.sms_empty))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(conversations, key = { it.phoneNumberNormalized }) { conversation ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenConversation(conversation.phoneNumberNormalized) },
                            headlineContent = { Text(conversation.phoneNumberNormalized) },
                            supportingContent = {
                                Text(
                                    "${conversation.lastMessage.body} · ${formatTimestamp(conversation.lastMessage.timestamp)}",
                                    maxLines = 1,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmsConversationDetailScreen(
    conversation: SmsConversation,
    onBack: () -> Unit,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(conversation.phoneNumberNormalized) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Button(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) { Text(stringResource(R.string.action_allow_number)) }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(conversation.messages, key = { it.id }) { message ->
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 320.dp).align(Alignment.CenterStart),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(message.body)
                                Text(
                                    formatTimestamp(message.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
