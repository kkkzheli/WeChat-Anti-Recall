package kkkzheli.antirecall.wechat.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.ui.MainActivity
import kkkzheli.antirecall.wechat.util.AccessibilityUtil
import kkkzheli.antirecall.wechat.util.NotificationHelper
import kkkzheli.antirecall.wechat.util.PermissionUtil

/**
 * Foreground daemon that keeps the capture chain alive and self-heals.
 * START_STICKY brings it back after a process kill; the watchdog re-arms on
 * restart and surfaces which granted permissions are actually keeping the
 * daemon running (notification listener / accessibility / battery exemption).
 * The notification carries the captured-message count (refreshed on IO) and
 * an elapsed-time chronometer, plus the brand accent color and icon.
 */
class KeepAliveService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val WATCHDOG_MS = 30_000L
        private const val BRAND_GREEN = 0xFF07C160.toInt()
    }

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var startedAtMs = 0L
    private var cachedCount = 0
    private var countLoaded = false

    private val watchdog = object : Runnable {
        override fun run() {
            serviceScope.launch {
                val count = try {
                    App.instance.repository.getMessageCount().first()
                } catch (_: Exception) {
                    cachedCount
                }
                cachedCount = count
                countLoaded = true
                withContext(Dispatchers.Main) { refresh() }
            }
            handler.postDelayed(this, WATCHDOG_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startedAtMs = System.currentTimeMillis()
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
        serviceScope.cancel()
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

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_KEEP_ALIVE)
            .setContentTitle(getString(R.string.keep_alive_title))
            .setContentText(contentText())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(BRAND_GREEN)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(openAppIntent())
        // Elapsed running time — anchored to process start so repeated
        // startForeground() calls don't reset the counter.
        if (startedAtMs > 0L) {
            builder.setUsesChronometer(true).setWhen(startedAtMs)
        }
        return builder.build()
    }

    private fun contentText(): String {
        val missing = healthMissing()
        if (missing.isNotEmpty()) {
            return getString(R.string.banner_permission_missing, missing.joinToString("、"))
        }
        if (countLoaded && cachedCount > 0) {
            return getString(R.string.keep_alive_count, cachedCount)
        }
        return getString(R.string.keep_alive_content)
    }

    /** Permissions that are still missing — empty list means fully protected. */
    private fun healthMissing(): List<String> = buildList {
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

    private fun isNotificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?: return false
        return enabled.contains("${packageName}/${NotificationCaptureService::class.java.name}")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
