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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.service.KeepAliveService
import kkkzheli.antirecall.wechat.service.NotificationCaptureService
import kkkzheli.antirecall.wechat.ui.compose.MainScreen
import kkkzheli.antirecall.wechat.ui.compose.SearchScreen
import kkkzheli.antirecall.wechat.ui.compose.SettingsScreen
import kkkzheli.antirecall.wechat.ui.compose.filter.FilterScreen
import kkkzheli.antirecall.wechat.ui.theme.ThemePreference
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
            val themePref by ThemePreference.readFlow().collectAsState(initial = ThemePreference.SYSTEM)

            WeChatAntiRecallTheme(userPreferredTheme = themePref) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current

                    val lastCaptureTimeMs by viewModel.lastCaptureTime.collectAsStateWithLifecycle()
                    val nlsEnabled by lazy {
                        val enabled = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                        enabled != null && enabled.contains("${context.packageName}/kkkzheli.antirecall.wechat.service.NotificationCaptureService")
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith
                            fadeOut(animationSpec = tween(250))
                        }
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
                                        Toast.makeText(applicationContext, "Message deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    lastCaptureTimeMs = lastCaptureTimeMs,
                                    nlsRegistered = nlsEnabled,
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

    private fun deleteSingleMessage(message: kkkzheli.antirecall.wechat.model.Message) {
        // The actual deletion is handled via ViewModel which queries from Room DB
        viewModel.deleteMessage(message.id)
        Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, "Notification permission is required for anti-recall feature. Please enable it in settings.", Toast.LENGTH_LONG).show()
    }
}
