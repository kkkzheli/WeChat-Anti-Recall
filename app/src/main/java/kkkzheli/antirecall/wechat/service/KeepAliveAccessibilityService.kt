package kkkzheli.antirecall.wechat.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

/**
 * Keep-alive anchor: a user-enabled accessibility service keeps this process alive.
 * It reads no window content and intercepts nothing — the binding itself is the
 * protection. When the system binds it (e.g. after boot), it also starts the
 * foreground keep-alive service so the whole chain restarts.
 */
class KeepAliveAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        startKeepAliveService()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Keep-alive only — no-op.
    }

    override fun onInterrupt() {
        // Keep-alive only — no-op.
    }

    private fun startKeepAliveService() {
        try {
            val intent = Intent(this, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (_: Exception) {
        }
    }
}
