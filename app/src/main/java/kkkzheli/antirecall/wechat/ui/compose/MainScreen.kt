package kkkzheli.antirecall.wechat.ui.compose

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import kkkzheli.antirecall.wechat.R
import androidx.compose.ui.res.stringResource
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.ui.compose.message.MessageCard
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel

/**
 * Main screen showing all captured WeChat messages.
 * Author: kkkzheli
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFilter: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteMessage: (Message) -> Unit = {},
    lastCaptureTimeMs: Long? = null,
    nlsRegistered: Boolean = false,
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.messages.observeForever { list -> messages = list ?: emptyList() }
        viewModel.messageCount.observeForever { c -> count = c ?: 0 }
    }

    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anti Recall") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = onNavigateToFilter) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.action_filter))
                            if (count > 0) {
                                Badge(modifier = Modifier.padding(start = 2.dp)) {
                                    Text(count.toString(), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            // Service status banner
            LastCaptureStatusBanner(
                context = ctx,
                lastCaptureTimeMs = lastCaptureTimeMs,
                nlsRegistered = nlsRegistered,
                onClick = {
                    if (!nlsRegistered) {
                        Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).also {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(it)
                        }
                    }
                },
            )

            if (messages.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.empty_messages), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(R.string.empty_capturing), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(messages) { message ->
                        MessageCard(message = message, onClick = {}, onLongPress = { msg -> onDeleteMessage(msg) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LastCaptureStatusBanner(context: android.content.Context, lastCaptureTimeMs: Long?, nlsRegistered: Boolean, onClick: () -> Unit) {
    val now = remember { System.currentTimeMillis() }
    val show = lastCaptureTimeMs != null && (now - lastCaptureTimeMs) < 5 * 60_000L
    val shouldShowInfo = !show && !nlsRegistered

    if (show) {
        // Small green dot in a slim bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.status_running),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    } else if (shouldShowInfo && !nlsRegistered) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.status_needs_permission),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}
