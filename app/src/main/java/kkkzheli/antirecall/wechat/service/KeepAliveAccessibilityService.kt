package kkkzheli.antirecall.wechat.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Keep-alive anchor: a user-enabled accessibility service is treated as a
 * protected process by the system. Besides that elevated status, it actively
 * drives the daemon — on connect it starts the foreground keep-alive service,
 * and a heartbeat re-arms it every 30 s so a killed daemon is brought straight
 * back. It reads no window content and intercepts nothing.
 */
class KeepAliveAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "A11yKeepAlive"
        private const val HEARTBEAT_MS = 30_000L
        private const val PREF_NAME = "daemon_heartbeat"
        private const val KEY_LAST_HEARTBEAT_MS = "last_heartbeat_ms"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val heartbeat = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, HEARTBEAT_MS)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Accessibility service connected")
        startKeepAliveService()
        handler.removeCallbacks(heartbeat)
        handler.post(heartbeat)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Notification-state-changed events fire whenever WeChat posts/removes a
        // notification — keep the daemon warm on activity and re-arm it cheaply.
        refresh()
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.w(TAG, "Accessibility service unbound")
        handler.removeCallbacks(heartbeat)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(heartbeat)
        super.onDestroy()
    }

    private fun refresh() {
        try {
            getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_HEARTBEAT_MS, System.currentTimeMillis())
                .apply()
            startKeepAliveService()
        } catch (_: Exception) {
        }
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
