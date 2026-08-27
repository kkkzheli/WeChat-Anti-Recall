package kkkzheli.antirecall.wechat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.util.AccessibilityUtil
import kkkzheli.antirecall.wechat.util.PermissionUtil

/**
 * Foreground daemon that keeps the capture chain alive and self-heals.
 * START_STICKY brings it back after a process kill; the watchdog re-arms on
 * restart and surfaces which granted permissions are actually keeping the
 * daemon running (notification listener / accessibility / battery exemption).
 */
class KeepAliveService : Service() {
    companion object {
        private const val CHANNEL_ID = "keep_alive"
        private const val NOTIFICATION_ID = 1
        private const val WATCHDOG_MS = 30_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val watchdog = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, WATCHDOG_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        handler.removeCallbacks(watchdog)
        handler.post(watchdog)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(watchdog)
        super.onDestroy()
        @Suppress("DEPRECATION")
        stopForeground(true)
    }

    private fun refresh() {
        try {
            startForeground(NOTIFICATION_ID, createNotification())
        } catch (_: Exception) {
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.keep_alive_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.keep_alive_content)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.keep_alive_title))
            .setContentText(healthText())
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    /** Reflects which granted permissions are actually protecting the daemon. */
    private fun healthText(): String {
        val missing = buildList {
            if (!AccessibilityUtil.isServiceEnabled(this@KeepAliveService)) {
                add(getString(R.string.settings_accessibility_keepalive))
            }
            if (!isNotificationListenerEnabled()) {
                add(getString(R.string.settings_notification_permission))
            }
            if (!PermissionUtil.isBatteryOptimizationExempt(this@KeepAliveService)) {
                add(getString(R.string.settings_battery_optimization))
            }
        }
        return if (missing.isEmpty()) {
            getString(R.string.keep_alive_content)
        } else {
            getString(R.string.banner_permission_missing, missing.joinToString("、"))
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?: return false
        return enabled.contains("${packageName}/${NotificationCaptureService::class.java.name}")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
