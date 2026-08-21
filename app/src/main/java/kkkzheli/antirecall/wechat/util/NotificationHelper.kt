package kkkzheli.antirecall.wechat.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    const val CHANNEL_ID_SPECIAL = "special_messages"
    const val CHANNEL_ID_KEEP_ALIVE = "keep_alive"

    fun createSpecialChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID_SPECIAL,
            "重要消息提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "微信转账、红包等重要消息实时提醒"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun createKeepAliveChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID_KEEP_ALIVE,
            "防保活服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Anti Recall 保活服务运行中"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
