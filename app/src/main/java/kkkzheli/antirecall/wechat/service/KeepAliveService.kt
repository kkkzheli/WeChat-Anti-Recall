package kkkzheli.antirecall.wechat.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kkkzheli.antirecall.wechat.util.NotificationHelper

class KeepAliveService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = android.app.Notification.Builder(this, NotificationHelper.CHANNEL_ID_KEEP_ALIVE)
            .setContentTitle(getString(kkkzheli.antirecall.wechat.R.string.keep_alive_title))
            .setContentText(getString(kkkzheli.antirecall.wechat.R.string.keep_alive_content))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        // Manifest declares foregroundServiceType="specialUse", so use two-arg form
        // which inherits the type from manifest (compatible across all API levels)
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
