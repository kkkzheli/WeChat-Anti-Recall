package kkkzheli.antirecall.wechat.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import kkkzheli.antirecall.wechat.repo.MessageRepository
import kkkzheli.antirecall.wechat.ui.compose.MainScreen
import kkkzheli.antirecall.wechat.ui.compose.SettingsScreen
import kkkzheli.antirecall.wechat.ui.compose.SearchScreen
import kkkzheli.antirecall.wechat.ui.compose.filter.FilterDropdown
import kkkzheli.antirecall.wechat.ui.compose.filter.MessageFilter
import kkkzheli.antirecall.wechat.ui.theme.AntiRecallTheme
import kkkzheli.antirecall.wechat.service.NotificationCaptureService
import kkkzheli.antirecall.wechat.service.KeepAliveService
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import kkkzheli.antirecall.wechat.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private var currentScreen by mutableStateOf<Screen>(Screen.MAIN)

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

        viewModel = MainViewModel(
            repository = App.instance.repository,
            lifecycleOwner = this
        )

        setContent {
            AntiRecallTheme {
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
                                onFocusChanged = { focused ->
                                    if (!focused) focusManager.clearFocus()
                                }
                            )
                        }
                        Screen.SEARCH -> {
                            SearchScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MAIN }
                            )
                        }
                        Screen.FILTER -> {
                            FilterDropdown(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MAIN }
                            )
                        }
                        Screen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MAIN }
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
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
        val componentName = ComponentName(
            this,
            NotificationCaptureService::class.java
        )
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
        )
        if (enabled == null || !enabled.contains(componentName.flattenToString())) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "请在设置中允许 Anti Recall 读取通知",
                Toast.LENGTH_LONG
            ).show()
        } else {
            val intent = Intent(this, NotificationCaptureService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
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
            val canIgnore = !Settings.isIgnoredBatteryOptimizations(this)
            if (!canIgnore) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
                ignoreBatteryLauncher.launch(intent)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.SYSTEM_ALERT_WINDOW
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                systemAlertPermissionLauncher.launch(Manifest.permission.SYSTEM_ALERT_WINDOW)
            }
        }
    }

    private fun showPermissionDialog() {
        Toast.makeText(
            this,
            "通知权限是防撤回功能的核心，请在设置中开启",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
