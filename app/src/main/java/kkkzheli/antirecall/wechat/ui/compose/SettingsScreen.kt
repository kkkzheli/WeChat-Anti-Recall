package kkkzheli.antirecall.wechat.ui.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.service.NotificationCaptureService
import kkkzheli.antirecall.wechat.ui.compose.github.GitHubOctocat
import kkkzheli.antirecall.wechat.ui.theme.AccentColor
import kkkzheli.antirecall.wechat.ui.theme.AppearanceRepository
import kkkzheli.antirecall.wechat.ui.theme.AppearanceSettings
import kkkzheli.antirecall.wechat.ui.theme.ListDensity
import kkkzheli.antirecall.wechat.ui.theme.SpecialPalette
import kkkzheli.antirecall.wechat.ui.theme.ThemeMode
import kkkzheli.antirecall.wechat.ui.theme.ThemePreset
import kkkzheli.antirecall.wechat.ui.theme.TitleStyle
import kkkzheli.antirecall.wechat.util.AccessibilityUtil
import kkkzheli.antirecall.wechat.util.AutoStartUtil
import kkkzheli.antirecall.wechat.util.PermissionUtil
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onClearConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    val settings by AppearanceRepository.flow.collectAsStateWithLifecycle(
        initialValue = AppearanceSettings.DEFAULT
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            // --------------------------------------------------------------
            // Appearance
            // --------------------------------------------------------------
            SectionHeader(stringResource(R.string.settings_section_appearance))

            // Colors & theme
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_colors_title),
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    // Dark / light / system
                    val modeOptions = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.theme_follow_system),
                        ThemeMode.DARK to stringResource(R.string.theme_dark),
                        ThemeMode.LIGHT to stringResource(R.string.theme_light),
                    )
                    SelectorChips(
                        options = modeOptions,
                        selected = settings.themeMode,
                        onSelect = { mode ->
                            scope.launch { AppearanceRepository.write { it.copy(themeMode = mode) } }
                        },
                        leadingIcon = { mode ->
                            when (mode) {
                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic color (Android 12+)
                    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.settings_dynamic_color), fontSize = 14.sp)
                            Text(
                                text = stringResource(
                                    if (dynamicSupported) R.string.settings_dynamic_color_desc
                                    else R.string.settings_dynamic_color_unsupported
                                ),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = settings.dynamicColor && dynamicSupported,
                            enabled = dynamicSupported,
                            onCheckedChange = { on ->
                                scope.launch { AppearanceRepository.write { it.copy(dynamicColor = on) } }
                            },
                        )
                    }

                    // Presets & accent only matter when dynamic color is off.
                    if (!settings.dynamicColor || !dynamicSupported) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.settings_theme_preset),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                        SelectorChips(
                            options = listOf(
                                ThemePreset.BRAND to stringResource(R.string.preset_brand),
                                ThemePreset.AMOLED to stringResource(R.string.preset_amoled),
                                ThemePreset.GRAPHITE to stringResource(R.string.preset_graphite),
                                ThemePreset.WARM_SAND to stringResource(R.string.preset_warm_sand),
                            ),
                            selected = settings.preset,
                            onSelect = { p ->
                                scope.launch { AppearanceRepository.write { it.copy(preset = p) } }
                            },
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.settings_accent_color),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                        AccentSwatchRow(
                            selectedArgb = settings.accentColorArgb,
                            onSelect = { c ->
                                scope.launch { AppearanceRepository.write { it.copy(accentColorArgb = c.argb) } }
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title style
                    Text(
                        text = stringResource(R.string.settings_title_style),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    SelectorChips(
                        options = listOf(
                            TitleStyle.GRADIENT to stringResource(R.string.title_style_gradient),
                            TitleStyle.ACCENT to stringResource(R.string.title_style_accent),
                            TitleStyle.STATIC to stringResource(R.string.title_style_static),
                        ),
                        selected = settings.titleStyle,
                        onSelect = { s ->
                            scope.launch { AppearanceRepository.write { it.copy(titleStyle = s) } }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Layout
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_layout_title),
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    // Bubble radius
                    Text(
                        text = stringResource(R.string.settings_bubble_radius),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    var radiusLocal by remember(settings.bubbleRadiusDp) {
                        mutableFloatStateOf(settings.bubbleRadiusDp.toFloat())
                    }
                    Slider(
                        value = radiusLocal,
                        onValueChange = { radiusLocal = it },
                        valueRange = 0f..28f,
                        onValueChangeFinished = {
                            scope.launch {
                                AppearanceRepository.write { it.copy(bubbleRadiusDp = radiusLocal.roundToInt()) }
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // List density
                    Text(
                        text = stringResource(R.string.settings_list_density),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    SelectorChips(
                        options = listOf(
                            ListDensity.COMPACT to stringResource(R.string.density_compact),
                            ListDensity.STANDARD to stringResource(R.string.density_standard),
                            ListDensity.RELAXED to stringResource(R.string.density_relaxed),
                        ),
                        selected = settings.density,
                        onSelect = { d ->
                            scope.launch { AppearanceRepository.write { it.copy(density = d) } }
                        },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Font scale
                    Text(
                        text = stringResource(R.string.settings_font_scale),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    var fontLocal by remember(settings.fontScale) {
                        mutableFloatStateOf(settings.fontScale)
                    }
                    Slider(
                        value = fontLocal,
                        onValueChange = { fontLocal = it },
                        valueRange = 0.85f..1.30f,
                        onValueChangeFinished = {
                            scope.launch {
                                AppearanceRepository.write {
                                    it.copy(fontScale = (fontLocal * 20).roundToInt() / 20f)
                                }
                            }
                        },
                    )
                    Text(
                        text = stringResource(R.string.font_scale_percent, (fontLocal * 100).roundToInt()),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Special message palette
                    Text(
                        text = stringResource(R.string.settings_special_palette),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    SelectorChips(
                        options = listOf(
                            SpecialPalette.VIVID to stringResource(R.string.palette_vivid),
                            SpecialPalette.SOFT to stringResource(R.string.palette_soft),
                        ),
                        selected = settings.specialPalette,
                        onSelect = { p ->
                            scope.launch { AppearanceRepository.write { it.copy(specialPalette = p) } }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GitHub section
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(GitHubOctocat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = stringResource(R.string.settings_github), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = MainViewModel.GITHUB_REPO, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { openUrl(context, MainViewModel.GITHUB_REPO) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.view_on_github))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About: avatar with a gradient ring + version pill
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val ringBrush = Brush.sweepGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary,
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ringBrush)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = MainViewModel.AUTHOR_NAME.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 26.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = MainViewModel.AUTHOR_NAME, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        text = stringResource(R.string.settings_about_project),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val pkgInfo = try { context.packageManager.getPackageInfo(context.packageName, 0) } catch (_: Exception) { null }
                    val vName = pkgInfo?.versionName ?: "?"
                    val vCode = pkgInfo?.versionCode?.toString() ?: "?"
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = stringResource(R.string.settings_version_info, vName, vCode),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_about_tech),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions section
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.settings_permission_status), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

                    val nlsEnabled = rememberNotificationAccessState(context)

                    PermissionCardRow(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_notification_permission),
                        status = if (nlsEnabled) stringResource(R.string.settings_granted) else stringResource(R.string.settings_not_granted),
                        isEnabled = nlsEnabled,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BatteryOptimizationRow(context)

                    Spacer(modifier = Modifier.height(8.dp))

                    AutoStartPermissionRow(context)

                    Spacer(modifier = Modifier.height(8.dp))

                    AccessibilityKeepAliveRow(context)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            ) {
                Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_clear_all))
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.dialog_clear_title)) },
            text = { Text(stringResource(R.string.dialog_clear_message)) },
            confirmButton = {
                TextButton(onClick = { onClearConfirmed(); showClearDialog = false }) {
                    Text(stringResource(R.string.dialog_clear_ok), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.dialog_clear_cancel)) } },
        )
    }
}

// ---------------------------------------------------------------------------
// Appearance building blocks
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

/** Generic chip selector; optional icon provider for the leading icon. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> SelectorChips(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ((T) -> androidx.compose.ui.graphics.vector.ImageVector)? = null,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
                leadingIcon = leadingIcon?.let { f -> { Icon(f(value), contentDescription = null, modifier = Modifier.size(16.dp)) } },
            )
        }
    }
}

@Composable
private fun AccentSwatchRow(selectedArgb: Int, onSelect: (AccentColor) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        AccentColor.entries.forEach { c ->
            val color = Color(c.argb)
            val isSelected = c.argb == selectedArgb
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelect(c) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Permission rows (unchanged behaviour)
// ---------------------------------------------------------------------------

@Composable
private fun BatteryOptimizationRow(context: Context) {
    val isIgnoring = rememberBatteryOptimizationState(context)

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    putExtra(Intent.EXTRA_PACKAGE_NAME, context.packageName)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback: open app details
                try {
                    val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                } catch (_: Exception) {}
            }
        },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = if (isIgnoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = stringResource(R.string.settings_battery_optimization), fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            val statusText = if (isIgnoring) stringResource(R.string.settings_granted) else stringResource(R.string.settings_not_granted)
            CapsuleBadge(text = statusText, isError = !isIgnoring)
        }
    }
}

@Composable
private fun AutoStartPermissionRow(context: Context) {
    val state = rememberAutoStartState(context)
    val statusText = when (state) {
        AutoStartUtil.State.ENABLED -> stringResource(R.string.settings_granted)
        AutoStartUtil.State.DISABLED -> stringResource(R.string.settings_not_granted)
        AutoStartUtil.State.UNKNOWN -> stringResource(R.string.auto_start_manual)
    }
    val iconTint = when (state) {
        AutoStartUtil.State.ENABLED -> MaterialTheme.colorScheme.primary
        AutoStartUtil.State.DISABLED -> MaterialTheme.colorScheme.onSurfaceVariant
        AutoStartUtil.State.UNKNOWN -> MaterialTheme.colorScheme.tertiary
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            launchAutoStartIntent(context)
        },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = stringResource(R.string.settings_auto_start), fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            CapsuleBadge(
                text = statusText,
                isError = state == AutoStartUtil.State.DISABLED,
                neutral = state == AutoStartUtil.State.UNKNOWN,
            )
        }
    }
}

/** Auto-start state, re-checked on resume so it flips after the user grants/revokes it. */
@Composable
private fun rememberAutoStartState(context: Context): AutoStartUtil.State {
    var state by remember { mutableStateOf(AutoStartUtil.detect(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state = AutoStartUtil.detect(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}

/** Notification-access state, re-checked on resume so it flips after the user grants/revokes it. */
@Composable
private fun rememberNotificationAccessState(context: Context): Boolean {
    var enabled by remember { mutableStateOf(isNotificationAccessEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = isNotificationAccessEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return enabled
}

private fun isNotificationAccessEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return enabled != null && enabled.contains("${context.packageName}/${NotificationCaptureService::class.java.name}")
}

@Composable
private fun rememberBatteryOptimizationState(context: Context): Boolean {
    var exempt by remember { mutableStateOf(PermissionUtil.isBatteryOptimizationExempt(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                exempt = PermissionUtil.isBatteryOptimizationExempt(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return exempt
}

@Composable
private fun AccessibilityKeepAliveRow(context: Context) {
    val enabled = rememberAccessibilityEnabled(context)

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            AccessibilityUtil.openSettings(context)
        },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.Accessible, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = stringResource(R.string.settings_accessibility_keepalive), fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            val statusText = if (enabled) stringResource(R.string.accessibility_badge_enabled) else stringResource(R.string.accessibility_badge_disabled)
            CapsuleBadge(text = statusText, isError = !enabled)
        }
    }
}

private fun launchAutoStartIntent(context: Context) {    try {
        val pkg = context.packageName
        when {
            // Xiaomi / HyperOS
            context.packageManager.hasSystemFeature("xiaomi.security.feature.AUTO_START_ENABLED") ||
                android.os.Build.BRAND.contains("redmi", ignoreCase = true) ||
                android.os.Build.BRAND.contains("xiaomi", ignoreCase = true) -> {
                val intent = Intent().apply {
                    component = android.content.ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            // Huawei
            android.os.Build.BRAND.contains("huawei", ignoreCase = true) ||
                android.os.Build.BRAND.contains("honor", ignoreCase = true) -> {
                val intent = Intent().apply {
                    component = android.content.ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            // Oppo / Realme
            android.os.Build.BRAND.contains("oppo", ignoreCase = true) ||
                android.os.Build.BRAND.contains("realme", ignoreCase = true) -> {
                val intent = Intent().apply {
                    component = android.content.ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            // Vivo
            android.os.Build.BRAND.contains("vivo", ignoreCase = true) -> {
                val intent = Intent().apply {
                    component = android.content.ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            // Samsung
            android.os.Build.BRAND.contains("samsung", ignoreCase = true) -> {
                val intent = Intent().apply {
                    component = android.content.ComponentName(
                        "com.samsung.android.sm",
                        "com.samsung.android.sm.app.dashboard.SmDashboardActivity"
                    )
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            // Generic fallback
            else -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$pkg")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    } catch (_: Exception) {
        // Fall back to app details settings
        try {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {}
    }
}

@Composable
private fun PermissionCardRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, status: String, isEnabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            CapsuleBadge(text = status, isError = !isEnabled)
        }
    }
}

@Composable
private fun CapsuleBadge(text: String, isError: Boolean = false, neutral: Boolean = false) {
    val bg: Color
    val fg: Color
    when {
        neutral -> {
            bg = MaterialTheme.colorScheme.tertiaryContainer
            fg = MaterialTheme.colorScheme.onTertiaryContainer
        }
        isError -> {
            bg = MaterialTheme.colorScheme.errorContainer
            fg = MaterialTheme.colorScheme.onErrorContainer
        }
        else -> {
            bg = MaterialTheme.colorScheme.primaryContainer
            fg = MaterialTheme.colorScheme.onPrimaryContainer
        }
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
        )
    }
}

private fun openUrl(context: Context, urlString: String) {
    try {
        val uri = Uri.parse(urlString)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {}
}
