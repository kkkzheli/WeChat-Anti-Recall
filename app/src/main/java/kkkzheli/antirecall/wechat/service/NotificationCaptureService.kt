package kkkzheli.antirecall.wechat.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.db.WeChatMessageEntity
import kkkzheli.antirecall.wechat.model.MessageType
import kkkzheli.antirecall.wechat.model.SpecialType
import kkkzheli.antirecall.wechat.util.NotificationHelper
import kkkzheli.antirecall.wechat.util.SpecialMessageDetector
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationCaptureService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val specialDetector = SpecialMessageDetector()

    override fun onNotificationPosted(sbn: StatusBarNotification?, record: RankingMap?) {
        val notification = sbn?.notification ?: return
        if (sbn.packageName != PACKAGE_NAME) return

        scope.launch {
            captureAndSave(notification, sbn.packageName)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, record: RankingMap?, reason: Int) {
        // Message recalled - already captured in onNotificationPosted
    }

    private suspend fun captureAndSave(notification: android.app.Notification, packageName: String) {
        val extras = notification.extras ?: return
        val contentText = extras.getString(android.app.Notification.EXTRA_TEXT).orEmpty()
        val contentTitle = extras.getString(android.app.Notification.EXTRA_TITLE).orEmpty()

        if (contentText.isEmpty() && contentTitle.isEmpty()) return

        val senderName = extractSender(contentTitle, contentText)
        val isGroup = senderName.contains("群") || senderName.contains("Group")
        val chatName = if (isGroup) senderName else ""

        val specialInfo = specialDetector.detect(contentTitle, contentText)
        val isSpecial = specialInfo != null

        val messageText = if (specialInfo != null) {
            specialDetector.getSpecialDisplayText(specialInfo, contentTitle, contentText)
        } else {
            contentText.ifEmpty { contentTitle }
        }

        val now = System.currentTimeMillis()
        val displayDate = dateFormat.format(Date(now))
        val displayTime = timeFormat.format(Date(now))

        val messageType = if (specialInfo != null) {
            when (specialInfo) {
                SpecialType.VOICE_CALL -> MessageType.VOICE_CALL
                SpecialType.VIDEO_CALL -> MessageType.VIDEO_CALL
                SpecialType.TRANSFER -> MessageType.TRANSFER
                SpecialType.RED_PACKET -> MessageType.RED_PACKET
                else -> MessageType.TEXT
            }
        } else {
            detectMessageType(contentText)
        }

        val entity = WeChatMessageEntity(
            content = messageText,
            senderName = senderName,
            chatName = chatName,
            messageType = messageType.name,
            specialType = specialInfo?.name,
            isSpecial = isSpecial,
            isGroup = isGroup,
            timestamp = now,
            displayDate = displayDate,
            displayTime = displayTime
        )

        App.instance.repository.saveMessage(entity)

        if (isSpecial) {
            sendSpecialNotification(messageText, senderName, chatName)
        }
    }

    private fun extractSender(title: String, text: String): String {
        val lines = text.lineSequence().take(3).filter { it.isNotBlank() }.toList()
        return if (lines.isNotEmpty()) {
            val firstLine = lines.first()
            val colonIndex = firstLine.indexOf(':')
            if (colonIndex > 0 && colonIndex < 15) {
                firstLine.substring(0, colonIndex).trim()
            } else {
                val spaceIndex = firstLine.indexOf('、')
                if (spaceIndex > 3 && spaceIndex < 15) {
                    firstLine.substring(0, spaceIndex).trim()
                } else {
                    title.trim().ifEmpty { firstLine.trim() }
                }
            }
        } else {
            title.trim()
        }
    }

    private fun detectMessageType(text: String): MessageType {
        val lower = text.lowercase()
        if (lower.contains("voice") || lower.contains("voice call")) return MessageType.VOICE_CALL
        if (lower.contains("video") || lower.contains("video call")) return MessageType.VIDEO_CALL
        if (lower.contains("语音") && !lower.contains("音频通话")) return MessageType.VOICE
        if (lower.contains("图片") || lower.contains("照片")) return MessageType.IMAGE
        if (lower.contains("视频")) return MessageType.VIDEO
        if (lower.contains("location") || lower.contains("位置")) return MessageType.LOCATION
        if (lower.contains("file") || lower.contains("文件")) return MessageType.FILE
        if (lower.contains("http://") || lower.contains("https://")) return MessageType.LINK
        return MessageType.TEXT
    }

    private fun sendSpecialNotification(message: String, sender: String, chatName: String) {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

            val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_SPECIAL)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("重要消息提醒")
                .setContentText("$sender${if (chatName.isNotEmpty()) " ($chatName)" else ""}: $message")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val PACKAGE_NAME = "com.tencent.mm"
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
