package kkkzheli.antirecall.wechat.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Stub: Keep-alive service to prevent the app from being killed by the OS.
 * Author: kkkzheli
 */
class KeepAliveService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = Notification.Builder(this, "antirerecall_channel")
            .setContentTitle("Anti Recall")
            .setContentText("Keep alive")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(2, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
