package kkkzheli.antirecall.wechat.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Stub: Notification capture service.
 * Author: kkkzheli
 */
class NotificationCaptureService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = Notification.Builder(this, "antirerecall_channel")
            .setContentTitle("Anti Recall")
            .setContentText("Monitoring notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
