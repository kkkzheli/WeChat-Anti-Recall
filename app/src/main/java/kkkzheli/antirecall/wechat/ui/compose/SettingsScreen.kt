package kkkzheli.antirecall.wechat.ui.compose

import android.content.Context
import android.content.res.Configuration
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.ui.compose.github.GitHubOctocat
import kkkzheli.antirecall.wechat.ui.theme.ThemePreference
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onClearConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    val themePref by ThemePreference.readFlow().collectAsState(initial = ThemePreference.SYSTEM)

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
            // Theme section
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.settings_theme_mode), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                    val scope = rememberCoroutineScope()
                    ThemePickerRow(themePref) { newPref ->
                        scope.launch { ThemePreference.write(newPref) }
                    }
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

            // Author section
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.settings_author), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isDark = LocalContext.current.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                        val bgColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                        val fgColor = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                        Surface(shape = RoundedCornerShape(50), color = bgColor, modifier = Modifier.size(48.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = MainViewModel.AUTHOR_NAME.take(1).uppercase(), fontWeight = FontWeight.Bold, color = fgColor, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = MainViewModel.AUTHOR_NAME, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(text = "Anti Recall Project", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Version section - reads from BuildConfig
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = stringResource(R.string.settings_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            val pkgInfo = try { context.packageManager.getPackageInfo(context.packageName, 0) } catch (_: Exception) { null }
                            val vName = pkgInfo?.versionName ?: "?"
                            val vCode = pkgInfo?.versionCode?.toString() ?: "?"
                            Text(text = "Anti Recall v${vName} (build ${vCode})", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Based on Material Design 3 / Jetpack Compose\nTarget API 34 · minSdk 23", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions section
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.settings_permission_status), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

                    val nlsEnabled by lazy {
                        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                        enabled != null && enabled.contains("${context.packageName}/kkkzheli.antirecall.wechat.service.NotificationCaptureService")
                    }

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

                    PermissionCardRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = stringResource(R.string.settings_floating_window),
                        status = stringResource(R.string.settings_granted),
                        isEnabled = true,
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                    )
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

@Composable
private fun ThemePickerRow(current: ThemePreference, onSelected: (ThemePreference) -> Unit) {
    val scope = rememberCoroutineScope()
    val options = listOf(
        ThemePreference.SYSTEM to stringResource(R.string.theme_follow_system),
        ThemePreference.DARK to stringResource(R.string.theme_dark),
        ThemePreference.LIGHT to stringResource(R.string.theme_light),
    )

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { _, (pref, label) ->
                OutlinedButton(
                    onClick = { scope.launch { ThemePreference.write(pref) } },
                    modifier = Modifier.weight(1f).padding(horizontal = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (pref == current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    ),
                ) {
                    when (pref) {
                        ThemePreference.SYSTEM -> Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        ThemePreference.DARK -> Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp))
                        ThemePreference.LIGHT -> Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(label, fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun BatteryOptimizationRow(context: Context) {
    val isIgnoring = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        pm.isIgnoringBatteryOptimizations(context.packageName)
    } else true

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
    val isUnrestricted = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else true
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
            Icon(Icons.Default.Storage, contentDescription = null, tint = if (isUnrestricted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = stringResource(R.string.settings_auto_start), fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            val statusText = if (isUnrestricted) stringResource(R.string.settings_granted) else stringResource(R.string.settings_not_granted)
            CapsuleBadge(text = statusText, isError = !isUnrestricted)
        }
    }
}

private fun launchAutoStartIntent(context: Context) {
    try {
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
private fun CapsuleBadge(text: String, isError: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
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
