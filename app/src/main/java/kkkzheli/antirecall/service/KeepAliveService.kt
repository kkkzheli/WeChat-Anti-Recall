package kkkzheli.antirecall.wechat.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kkkzheli.antirecall.wechat.ui.MainActivity
import kkkzheli.antirecall.wechat.util.NotificationHelper

class KeepAliveService : Service() {

    private val notificationId = 10001

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureForeground()
        return START_STICKY
    }

    private fun ensureForeground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notification = createKeepAliveNotification()
        try {
            startForeground(notificationId, notification)
        } catch (e: Exception) {
            // Foreground service may fail if permissions are missing; continue anyway
        }
    }

    private fun createKeepAliveNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_NORMAL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Anti Recall running")
            .setContentText("Anti-recall service is active")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
