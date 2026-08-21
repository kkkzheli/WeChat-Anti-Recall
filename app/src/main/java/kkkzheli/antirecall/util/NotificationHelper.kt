package kkkzheli.antirecall.wechat.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    const val CHANNEL_ID_SPECIAL = "special_messages"
    const val CHANNEL_ID_NORMAL = "captured_messages"

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val specialChannel = NotificationChannel(
            CHANNEL_ID_SPECIAL,
            "重要消息提醒",
            NotificationManager.IMPORTANCE_MAX
        ).apply {
            description = "语音通话、转账、红包等重要消息提醒"
            enableLights(true)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val normalChannel = NotificationChannel(
            CHANNEL_ID_NORMAL,
            "消息记录",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "防撤回消息记录通知"
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        manager.createNotificationChannel(specialChannel)
        manager.createNotificationChannel(normalChannel)
    }

    fun sendImportantNotification(
        context: Context,
        title: String,
        content: String,
        priority: Int = NotificationCompat.PRIORITY_MAX
    ) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_SPECIAL)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
                .setCategory(NotificationCompat.CATEGORY_ALERT)
                .setAutoCancel(true)
                .setShowWhen(true)
                .build()

            manager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return android.content.pm.PackageManager.PERMISSION_GRANTED ==
                context.checkPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    context.packageName
                )
        }
        return true
    }
}
