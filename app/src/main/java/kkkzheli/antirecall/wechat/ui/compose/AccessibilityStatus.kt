package kkkzheli.antirecall.wechat.ui.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kkkzheli.antirecall.wechat.util.AccessibilityUtil

/**
 * Returns whether the accessibility keep-alive service is enabled, re-checked
 * every time the screen resumes (so the badge flips when the user returns from
 * the system accessibility settings).
 */
@Composable
fun rememberAccessibilityEnabled(context: Context): Boolean {
    var enabled by remember { mutableStateOf(AccessibilityUtil.isServiceEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = AccessibilityUtil.isServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return enabled
}
