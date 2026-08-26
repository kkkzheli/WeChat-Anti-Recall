package kkkzheli.antirecall.wechat.ui.compose

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kkkzheli.antirecall.wechat.service.NotificationCaptureService
import kkkzheli.antirecall.wechat.util.AccessibilityUtil
import kkkzheli.antirecall.wechat.util.PermissionUtil

data class PermissionsState(
    val notificationAccess: Boolean,
    val accessibility: Boolean,
    val batteryOptimization: Boolean,
) {
    val allGranted: Boolean
        get() = notificationAccess && accessibility && batteryOptimization
}

/**
 * Real-time permission state, re-checked every time the screen resumes
 * (so returning from system settings immediately refreshes the banner).
 */
@Composable
fun rememberPermissions(context: Context): PermissionsState {
    var state by remember { mutableStateOf(checkPermissions(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state = checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}

private fun checkPermissions(context: Context): PermissionsState {
    val nls = (Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        ?: "").contains("${context.packageName}/${NotificationCaptureService::class.java.name}")
    val accessibility = AccessibilityUtil.isServiceEnabled(context)
    val battery = PermissionUtil.isBatteryOptimizationExempt(context)
    return PermissionsState(nls, accessibility, battery)
}
