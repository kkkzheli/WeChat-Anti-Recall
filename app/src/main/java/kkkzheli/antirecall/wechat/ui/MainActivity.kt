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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.service.KeepAliveService
import kkkzheli.antirecall.wechat.service.NotificationCaptureService
import kkkzheli.antirecall.wechat.ui.compose.MainScreen
import kkkzheli.antirecall.wechat.ui.compose.SearchScreen
import kkkzheli.antirecall.wechat.ui.compose.SettingsScreen
import kkkzheli.antirecall.wechat.ui.compose.filter.FilterScreen
import kkkzheli.antirecall.wechat.ui.theme.AppearanceRepository
import kkkzheli.antirecall.wechat.ui.theme.AppearanceSettings
import kkkzheli.antirecall.wechat.ui.theme.WeChatAntiRecallTheme
import kkkzheli.antirecall.wechat.viewmodel.MainViewModel
import android.widget.Toast
import android.util.Log

class MainActivity : ComponentActivity() {

    private var currentScreen by mutableStateOf(Screen.MAIN)

    enum class Screen { MAIN, SEARCH, FILTER, SETTINGS }

    private lateinit var viewModel: MainViewModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        // No splash screen: the window background shows through until the first
        // Compose frame, then the message list rains in from the top.

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
            val settings by AppearanceRepository.flow.collectAsState(initial = AppearanceSettings.DEFAULT)

            WeChatAntiRecallTheme(settings = settings) {
                // In-app font scaling on top of the system setting.
                val sysDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(sysDensity.density, sysDensity.fontScale * settings.fontScale)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val lastCaptureTimeMs by viewModel.lastCaptureTime.collectAsStateWithLifecycle()

                        // Directional transitions: deeper screens slide in from
                        // the right, going back slides from the left.
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                val forward = targetState != Screen.MAIN
                                val spec = tween<IntOffset>(300, easing = FastOutSlowInEasing)
                                if (forward) {
                                    (slideInHorizontally(spec) { it / 4 } + fadeIn(tween(300))) togetherWith
                                        (slideOutHorizontally(spec) { -it / 6 } + fadeOut(tween(200)))
                                } else {
                                    (slideInHorizontally(spec) { -it / 4 } + fadeIn(tween(300))) togetherWith
                                        (slideOutHorizontally(spec) { it / 6 } + fadeOut(tween(200)))
                                }
                            },
                            label = "screenTransition",
                        ) { screen ->
                            when (screen) {
                                Screen.MAIN -> {
                                    MainScreen(
                                        viewModel = viewModel,
                                        onNavigateToSearch = { currentScreen = Screen.SEARCH },
                                        onNavigateToFilter = { currentScreen = Screen.FILTER },
                                        onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                                        onDeleteMessage = { msg ->
                                            viewModel.deleteMessage(msg.id)
                                            Toast.makeText(applicationContext, getString(R.string.msg_deleted), Toast.LENGTH_SHORT).show()
                                        },
                                        lastCaptureTimeMs = lastCaptureTimeMs,
                                        titleStyle = settings.titleStyle,
                                        titleGradient = settings.titleGradient,
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
        val pkgName = "kkkzheli.antirecall.wechat"
        val current = android.provider.Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        ) ?: ""

        if (!current.contains(pkgName)) {
            Log.w("NCS", "NOT in listener list! Opening settings.")
            startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(this, getString(kkkzheli.antirecall.wechat.R.string.permission_needs_message), Toast.LENGTH_LONG).show()
            return
        }

        // Registered — try starting the service.
        val intent = Intent(this, NotificationCaptureService::class.java)
        try {
            startService(intent)
            Log.d("NCS", "startService()")
        } catch (e: Exception) {
            Log.e("NCS", "startService() FAILED", e)
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
        Toast.makeText(this, getString(R.string.permission_needs_message), Toast.LENGTH_LONG).show()
    }
}
