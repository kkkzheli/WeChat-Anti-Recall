package kkkzheli.antirecall.wechat.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.service.KeepAliveService
import kkkzheli.antirecall.wechat.service.NotificationCaptureService
import kkkzheli.antirecall.wechat.ui.compose.MainScreen
import kkkzheli.antirecall.wechat.ui.compose.SearchScreen
import kkkzheli.antirecall.wechat.ui.compose.SettingsScreen
import kkkzheli.antirecall.wechat.ui.compose.filter.FilterScreen
import kkkzheli.antirecall.wechat.ui.theme.WeChatAntiRecallTheme
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import android.widget.Toast
import android.util.Log

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
        // Full screen: content extends behind status bar, transparent color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = 0x00000000
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(repository = App.instance.repository)

        // Back button: navigate to MAIN or exit app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentScreen != Screen.MAIN) {
                    currentScreen = Screen.MAIN
                }
                // When on MAIN, do nothing — let system handle it (exit app)
            }
        })

        setContent {
            WeChatAntiRecallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(200))
                        }
                    ) { screen ->
                        when (screen) {
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
                                FilterScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.MAIN },
                                )
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
        }

        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        // Re-ensure services are running after returning from settings/permissions
        startNotificationListener()
        startKeepAlive()
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
        // On some devices (HyperOS/Android 15+), getEnabledListenerPackages()
        // may not return our package even though the service is registered.
        // Best approach: try to start the service directly; if it fails, ask for permission.
        val intent = Intent(this, NotificationCaptureService::class.java)
        try {
            startService(intent)
            Log.d("NCS", "startService() SUCCESS")
        } catch (e: Exception) {
            Log.w("NCS", "startService() threw: ${e.message}, opening settings.")
            startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(
                this,
                getString(kkkzheli.antirecall.wechat.R.string.permission_needs_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startKeepAlive() {
        val intent = Intent(this, KeepAliveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showPermissionDialog() {
        Toast.makeText(this, "Notification permission is required for anti-recall feature. Please enable it in settings.", Toast.LENGTH_LONG).show()
    }
}
