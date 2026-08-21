package kkkzheli.antirecall.wechat.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.content.ContextCompat
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.service.KeepAliveService
import kkkzheli.antirecall.wechat.service.NotificationCaptureService
import kkkzheli.antirecall.wechat.ui.compose.MainScreen
import kkkzheli.antirecall.wechat.ui.compose.SearchScreen
import kkkzheli.antirecall.wechat.ui.compose.SettingsScreen
import kkkzheli.antirecall.wechat.ui.compose.filter.FilterScreen
import kkkzheli.antirecall.wechat.ui.theme.WeChatAntiRecallTheme
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private var currentScreen by mutableStateOf(Screen.MAIN)

    enum class Screen { MAIN, SEARCH, FILTER, SETTINGS }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startNotificationListener()
            startKeepAlive()
        } else {
            viewModel.setNotificationPermissionDenied(true)
            showPermissionDialog()
        }
    }

    private val systemAlertPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            viewModel.setSystemAlertPermissionDenied(true)
        }
    }

    private val ignoreBatteryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(repository = App.instance.repository)

        setContent {
            WeChatAntiRecallTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val focusManager = LocalFocusManager.current
                    val context = LocalContext.current

                    when (currentScreen) {
                        Screen.MAIN -> {
                            MainScreen(
                                viewModel = viewModel,
                                onNavigateToSearch = { currentScreen = Screen.SEARCH },
                                onNavigateToFilter = { currentScreen = Screen.FILTER },
                                onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                            )
                        }
                        Screen.SEARCH -> {
                            SearchScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MAIN })
                        }
                        Screen.FILTER -> {
                            FilterScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MAIN })
                        }
                        Screen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MAIN },
                                onClearConfirmed = { viewModel.clearAllMessages() },
                            )
                        }
                    }
                }
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startNotificationListener()
                startKeepAlive()
            }
        } else {
            startNotificationListener()
            startKeepAlive()
        }
    }

    private fun startNotificationListener() {
        val componentName = ComponentName(this, NotificationCaptureService::class.java)
        val enabled = android.provider.Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        if (enabled == null || !enabled.contains(componentName.flattenToString())) {
            startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(this, getString(kkkzheli.antirecall.wechat.R.string.permission_needs_message), Toast.LENGTH_LONG).show()
        } else {
            val intent = Intent(this, NotificationCaptureService::class.java)
            startService(intent)
        }
    }

    private fun startKeepAlive() {
        val intent = Intent(this, KeepAliveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerMgr = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            val canIgnore = powerMgr.isIgnoringBatteryOptimizations(packageName)
            if (!canIgnore) {
                val intent = Intent(
                    android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
                ignoreBatteryLauncher.launch(intent)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
                systemAlertPermissionLauncher.launch(Manifest.permission.SYSTEM_ALERT_WINDOW)
            }
        }
    }

    private fun showPermissionDialog() {
        Toast.makeText(this, "通知权限是防撤回功能的核心，请在设置中开启", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
