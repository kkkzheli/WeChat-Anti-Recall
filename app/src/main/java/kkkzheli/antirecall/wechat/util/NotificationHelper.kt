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
            context.getString(kkkzheli.antirecall.wechat.R.string.notification_channel_special),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(kkkzheli.antirecall.wechat.R.string.notification_content_special)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun createKeepAliveChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID_KEEP_ALIVE,
            context.getString(kkkzheli.antirecall.wechat.R.string.keep_alive_title),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(kkkzheli.antirecall.wechat.R.string.keep_alive_content)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
