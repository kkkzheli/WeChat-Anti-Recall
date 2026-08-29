package kkkzheli.antirecall.wechat.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.model.DisplayItem
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.ui.compose.message.MessageCard
import kkkzheli.antirecall.wechat.ui.theme.TitleStyle
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Which status banner (if any) shows below the top bar. */
private enum class MainBanner { HIDDEN, RUNNING, WARNING }

/**
 * Main screen showing all captured WeChat messages.
 * Author: kkkzheli
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFilter: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteMessage: (Message) -> Unit = {},
    lastCaptureTimeMs: Long? = null,
    titleStyle: TitleStyle = TitleStyle.GRADIENT,
) {
    // Seed from the current LiveData values so the first frame already has data.
    // observeForever has no lifecycle owner, so the observers are removed manually
    // on dispose instead of leaking past this screen.
    var displayItems by remember { mutableStateOf(viewModel.getDisplayItems()) }
    var count by remember { mutableIntStateOf(viewModel.messageCount.value ?: 0) }
    var unreadCount by remember { mutableIntStateOf(viewModel.unreadCount.value ?: 0) }

    DisposableEffect(Unit) {
        val itemsObserver = Observer<List<DisplayItem>> { list -> displayItems = list ?: emptyList() }
        val countObserver = Observer<Int> { c -> count = c ?: 0 }
        val unreadObserver = Observer<Int> { u -> unreadCount = u ?: 0 }
        viewModel.displayItems.observeForever(itemsObserver)
        viewModel.messageCount.observeForever(countObserver)
        viewModel.unreadCount.observeForever(unreadObserver)
        onDispose {
            viewModel.displayItems.removeObserver(itemsObserver)
            viewModel.messageCount.removeObserver(countObserver)
            viewModel.unreadCount.removeObserver(unreadObserver)
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Entrance burst: one shared Animatable, played exactly once per process —
    // the flag lives on the ViewModel, so coming back from Settings/Search
    // starts settled at 1f (no replay, no blank flash) instead of re-animating.
    val entrance = remember { Animatable(if (viewModel.entranceBurstPlayed) 1f else 0f) }
    LaunchedEffect(displayItems.isNotEmpty()) {
        if (displayItems.isEmpty() || viewModel.entranceBurstPlayed) return@LaunchedEffect
        viewModel.entranceBurstPlayed = true
        entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing))
    }

    // Newest message (the list is timestamp-DESC; index 0 may be its date header).
    val newestItem = remember(displayItems) {
        displayItems.firstOrNull { it is DisplayItem.MessageItem } as? DisplayItem.MessageItem
    }
    val newestId = newestItem?.message?.id

    // Auto-scroll / floating "N new" pill. The id guard survives navigation via
    // rememberSaveable; re-emissions of the same id never re-trigger. A restored
    // deep scroll position (process death) is never yanked — only subsequent,
    // genuinely-new messages scroll or raise the pill.
    var lastAutoScrolledId by rememberSaveable { mutableLongStateOf(-1L) }
    var initialized by remember { mutableStateOf(false) }
    var pendingNew by remember { mutableIntStateOf(0) }
    var pillVisible by remember { mutableStateOf(false) }

    // Boolean flips only — re-evaluated when the first visible item changes,
    // never per frame during a fling.
    val nearTop by remember { derivedStateOf { listState.firstVisibleItemIndex <= 1 } }

    LaunchedEffect(newestId) {
        if (newestId == null) return@LaunchedEffect
        if (!initialized) {
            initialized = true
            lastAutoScrolledId = newestId
            return@LaunchedEffect
        }
        if (newestId != lastAutoScrolledId) {
            lastAutoScrolledId = newestId
            if (nearTop) {
                listState.animateScrollToItem(0)
            } else {
                pendingNew += 1
                pillVisible = true
            }
        }
    }

    // Pill lifecycle: auto-hide after a few seconds, and clear as soon as the
    // user is back at the top where the new messages are visible.
    LaunchedEffect(pillVisible) {
        if (pillVisible) {
            delay(4000)
            pillVisible = false
        }
    }
    LaunchedEffect(nearTop) {
        if (nearTop) {
            pillVisible = false
            pendingNew = 0
        }
    }

    // The unread count comes straight from the list pipeline (computed on the
    // same pass that places the divider) — no per-emission re-count here.

    // Collapse the unread divider once seen: while the user sits at the top,
    // and on leaving the screen — but only if they were up there. A user who
    // never scrolled up keeps their divider for next time.
    val newestTs = newestItem?.message?.timestamp ?: 0L
    LaunchedEffect(nearTop, newestTs) {
        if (nearTop && newestTs > 0L) viewModel.markAllSeen(newestTs)
    }
    val newestTsState by rememberUpdatedState(newestTs)
    val nearTopState by rememberUpdatedState(nearTop)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && nearTopState) {
                viewModel.markAllSeen(newestTsState)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val ctx = LocalContext.current
    val permissions = rememberPermissions(ctx)

    // Heartbeat so the "running" banner flips back to hidden when the
    // last-capture window expires without any new DB emission or user input.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }
    // `now` is read only inside the derived calc, so the 30s heartbeat tick
    // re-evaluates just this flag — the banner recomposes only when it flips.
    val isRunning by remember(lastCaptureTimeMs) {
        val started = lastCaptureTimeMs
        derivedStateOf { started != null && (now - started) < 5 * 60_000L }
    }

    val colorScheme = MaterialTheme.colorScheme
    // Page backdrop: one subtle vertical wash behind everything, including the
    // (transparent) top bar, so the glass surfaces have something to sit on.
    val bgBrush = remember(colorScheme) {
        Brush.verticalGradient(
            0f to colorScheme.background,
            1f to colorScheme.surfaceContainerLowest,
        )
    }

    // Glass top bar: tint + hairline fade in only once the list can scroll
    // back (boolean flip, then the color animates; both are consumed in the
    // draw phase below, so neither per-frame scroll nor the color tween
    // recomposes anything).
    val condensed by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 4 }
    }
    val barTint = if (condensed) colorScheme.surface.copy(alpha = 0.96f) else Color.Transparent
    val animBarColor by animateColorAsState(barTint, tween(250), label = "barTint")
    val hairline = colorScheme.outlineVariant

    val banner = when {
        !permissions.allGranted -> MainBanner.WARNING
        isRunning -> MainBanner.RUNNING
        else -> MainBanner.HIDDEN
    }

    Scaffold(
        modifier = modifier.background(bgBrush),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                modifier = Modifier.drawBehind {
                    val c = animBarColor
                    if (c.alpha > 0.01f) {
                        drawRect(c)
                        drawLine(
                            color = hairline.copy(alpha = 0.55f * c.alpha),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1f,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    // The count pill lives next to the title (screen top-left), not
                    // as an overlay on the filter icon — there it has the whole top
                    // bar to grow and can never be squeezed by neighbouring icons.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppTitle(permissions, titleStyle)
                        AnimatedVisibility(
                            visible = count > 0,
                            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
                            exit = fadeOut(tween(150)),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.animateContentSize(),
                                ) {
                                    AnimatedContent(
                                        targetState = count,
                                        transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(120)) },
                                        label = "countPill",
                                    ) { c ->
                                        Text(
                                            text = c.toString(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Visible,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFilter) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.action_filter))
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Status banner: warning when permissions missing, running/granted otherwise
            AnimatedContent(
                targetState = banner,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically(tween(220)) { -it / 2 }) togetherWith
                        (fadeOut(tween(160)) + slideOutVertically(tween(160)) { -it / 2 })
                },
                label = "banner",
            ) { b ->
                when (b) {
                    MainBanner.WARNING -> PermissionWarningBanner(permissions, onNavigateToSettings)
                    MainBanner.RUNNING -> RunningBanner()
                    MainBanner.HIDDEN -> Spacer(modifier = Modifier.height(0.dp))
                }
            }

            if (displayItems.isEmpty()) {
                FloatingEmptyState(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        displayItems.forEachIndexed { index, item ->
                            when (item) {
                                is DisplayItem.DateHeader -> stickyHeader(
                                    key = "day_${item.epochDay}",
                                    contentType = "date",
                                ) {
                                    DayHeader(item.epochDay)
                                }
                                DisplayItem.UnreadDivider -> item(key = "unread", contentType = "unread") {
                                    UnreadDividerRow(unreadCount)
                                }
                                is DisplayItem.MessageItem -> item(
                                    key = "msg_${item.message.id}",
                                    contentType = msgContentType(item),
                                ) {
                                    BurstItem(index, entrance) {
                                        MessageCard(
                                            message = item.message,
                                            compact = item.compact,
                                            onClick = {},
                                            onDelete = { msg ->
                                                onDeleteMessage(msg)
                                                displayItems = displayItems.filterNot {
                                                    (it as? DisplayItem.MessageItem)?.message?.id == msg.id
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating "N new messages" pill — appears only when a new
                    // message lands while the user is scrolled away from the top.
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp),
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = pillVisible && !nearTop,
                            enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
                            exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180)),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.inverseSurface,
                                shadowElevation = 6.dp,
                                modifier = Modifier.clickable {
                                    scope.launch { listState.animateScrollToItem(0) }
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.new_messages_count, pendingNew),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Entrance burst — plays once, the first time the list has content. Progress
// is one shared Animatable read inside each item's graphicsLayer lambda, so
// the whole stagger costs zero recompositions and is finished before any
// fling can interact with it.
// ---------------------------------------------------------------------------

private val ENTRANCE_MS = 650
private const val ENTRANCE_STAGGER = 0.062f   // ≈40ms between items
private const val ENTRANCE_ITEM_SPAN = 0.385f // ≈250ms per item's own fade+rise

@Composable
private fun BurstItem(
    index: Int,
    progress: Animatable<Float, AnimationVector1D>,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.graphicsLayer {
            val p = progress.value
            val start = (index % 12) * ENTRANCE_STAGGER
            val local = ((p - start) / ENTRANCE_ITEM_SPAN).coerceIn(0f, 1f)
            val eased = 1f - (1f - local) * (1f - local) * (1f - local)
            alpha = eased
            translationY = (1f - eased) * 18.dp.toPx()
        },
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// List furniture
// ---------------------------------------------------------------------------

@Composable
private fun DayHeader(epochDay: Long) {
    // Opaque backing so scrolled cards never show through the sticky header.
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = dayLabel(epochDay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun dayLabel(epochDay: Long): String {
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val today = LocalDate.now().toEpochDay()
    return when (epochDay) {
        today -> stringResource(R.string.day_today)
        today - 1 -> stringResource(R.string.day_yesterday)
        else -> LocalDate.ofEpochDay(epochDay).format(formatter)
    }
}

@Composable
private fun UnreadDividerRow(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
        )
        Text(
            text = stringResource(R.string.new_messages_count, count),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
        )
    }
}

private fun msgContentType(item: DisplayItem.MessageItem): String = when {
    item.message.isSpecial -> "special"
    item.message.chatName.isNotEmpty() -> if (item.compact) "group_c" else "group"
    else -> if (item.compact) "personal_c" else "personal"
}

// ---------------------------------------------------------------------------
// Empty state — gently floating, all motion in the draw phase.
// ---------------------------------------------------------------------------

@Composable
private fun FloatingEmptyState(modifier: Modifier = Modifier) {
    val floatTransition = rememberInfiniteTransition(label = "emptyFloat")
    val dy by floatTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dy",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.graphicsLayer { translationY = dy },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.width(80.dp).height(80.dp),
                tint = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.empty_messages),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_capturing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RunningBanner() {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer { alpha = pulseAlpha }
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.status_running),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AppTitle(permissions: PermissionsState, style: TitleStyle) {
    // Normal title until every permission is granted — the flourish is a
    // reward for a fully armed capture pipeline.
    if (!permissions.allGranted) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        return
    }
    when (style) {
        TitleStyle.GRADIENT -> GradientTitle()
        TitleStyle.ACCENT -> Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary,
        )
        TitleStyle.STATIC -> Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Colors of the title gradient; first and last match so the repeated tile wraps seamlessly. */
private val TitleGradientColors = listOf(
    Color(0xFF1565C0),
    Color(0xFF00E5FF),
    Color(0xFF4FC3F7),
    Color(0xFF1565C0),
)

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
        colors = TitleGradientColors,
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
