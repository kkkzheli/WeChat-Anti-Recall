package kkkzheli.antirecall.wechat.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kkkzheli.antirecall.wechat.util.NotificationHelper

class KeepAliveService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = android.app.Notification.Builder(this, NotificationHelper.CHANNEL_ID_KEEP_ALIVE)
            .setContentTitle("防保活服务运行中")
            .setContentText("微信消息实时捕获")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
