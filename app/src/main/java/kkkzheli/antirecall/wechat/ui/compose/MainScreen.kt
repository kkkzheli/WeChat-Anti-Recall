package kkkzheli.antirecall.wechat.ui.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import kotlinx.coroutines.delay
import kkkzheli.antirecall.wechat.R
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
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.messages.observeForever { list -> messages = list ?: emptyList() }
        viewModel.messageCount.observeForever { c -> count = c ?: 0 }
    }

    val ctx = LocalContext.current
    val permissions = rememberPermissions(ctx)

    // Heartbeat so the "running" banner flips back to the title banner when the
    // last-capture window expires without any new DB emission or user input.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }
    val isRunning = lastCaptureTimeMs != null && (now - lastCaptureTimeMs) < 5 * 60_000L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppTitle(permissions) },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = onNavigateToFilter) {
                        Box {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.action_filter),
                                modifier = Modifier.align(Alignment.Center),
                            )
                            if (count > 0) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-6).dp)
                                        .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp),
                                ) {
                                    Text(
                                        text = count.toString(),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    )
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
            // Status banner: warning when permissions missing, running/granted otherwise
            StatusBanner(
                permissions = permissions,
                isRunning = isRunning,
                onClick = onNavigateToSettings,
            )

            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.width(80.dp).height(80.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.empty_messages), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(R.string.empty_capturing), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageCard(
                            message = message,
                            onClick = {},
                            onDelete = { msg ->
                                onDeleteMessage(msg)
                                messages = messages.filterNot { it.id == msg.id }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(permissions: PermissionsState, isRunning: Boolean, onClick: () -> Unit) {
    if (!permissions.allGranted) {
        PermissionWarningBanner(permissions, onClick)
    } else if (isRunning) {
        RunningBanner()
    }
}

@Composable
private fun RunningBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x8C0D47A1))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color.White, CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.status_running),
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AppTitle(permissions: PermissionsState) {
    if (permissions.allGranted) {
        GradientTitle()
    } else {
        Text(stringResource(R.string.app_name))
    }
}

/**
 * Seamless looping gradient title. The gradient period equals the title width and
 * the color sequence starts and ends on the same color, so when [phase] wraps from
 * 1 back to 0 the pattern is identical — no visible jump.
 */
@Composable
private fun GradientTitle() {
    val transition = rememberInfiniteTransition(label = "titleWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    var titleWidth by remember { mutableStateOf(0f) }
    val period = titleWidth.coerceAtLeast(1f)
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1565C0),
            Color(0xFF00E5FF),
            Color(0xFF4FC3F7),
            Color(0xFF1565C0),
        ),
        start = Offset(phase * period, 0f),
        end = Offset(phase * period + period, 0f),
        tileMode = TileMode.Repeated,
    )
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            brush = brush,
        ),
        onTextLayout = { result -> titleWidth = result.size.width.toFloat() },
    )
}

@Composable
private fun PermissionWarningBanner(permissions: PermissionsState, onClick: () -> Unit) {
    val missing = buildList {
        if (!permissions.notificationAccess) add(stringResource(R.string.settings_notification_permission))
        if (!permissions.accessibility) add(stringResource(R.string.settings_accessibility_keepalive))
        if (!permissions.batteryOptimization) add(stringResource(R.string.settings_battery_optimization))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.banner_permission_missing, missing.joinToString("、")),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.permission_needs_open_settings),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
