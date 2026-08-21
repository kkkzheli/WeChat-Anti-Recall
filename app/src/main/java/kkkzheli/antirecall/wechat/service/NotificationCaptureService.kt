package kkkzheli.antirecall.wechat.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationCompat
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.db.WeChatMessageEntity
import kkkzheli.antirecall.wechat.model.MessageType
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

    override fun onNotificationPosted(sn: Notification?, tag: String?, key: String?, notification: Notification?) {
        if (notification == null) return
        if (notification.packageName != PACKAGE_NAME) return

        scope.launch {
            captureAndSave(notification)
        }
    }

    override fun onNotificationRemoved(sn: Notification?, tag: String?, key: String?, notification: Notification?) {
        // Message recalled - already captured in onNotificationPosted
    }

    private suspend fun captureAndSave(notification: Notification) {
        val extras = notification.extras ?: return
        val contentText = extras.getString(Notification.EXTRA_TEXT).orEmpty()
        val contentTitle = extras.getString(Notification.EXTRA_TITLE).orEmpty()

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
                SpecialMessageDetector.SpecialType.VOICE_CALL -> MessageType.VOICE_CALL
                SpecialMessageDetector.SpecialType.VIDEO_CALL -> MessageType.VIDEO_CALL
                SpecialMessageDetector.SpecialType.TRANSFER -> MessageType.TRANSFER
                SpecialMessageDetector.SpecialType.RED_PACKET -> MessageType.RED_PACKET
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
                val spaceIndex = firstLine.indexOf(' ')
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
        if (lower.contains("voice") || lower.contains("语音")) return MessageType.VOICE
        if (lower.contains("image") || lower.contains("图片")) return MessageType.IMAGE
        if (lower.contains("video") || lower.contains("视频")) return MessageType.VIDEO
        if (lower.contains("location") || lower.contains("位置")) return MessageType.LOCATION
        if (lower.contains("file") || lower.contains("文件")) return MessageType.FILE
        if (lower.contains("http://") || lower.contains("https://")) return MessageType.LINK
        return MessageType.TEXT
    }

    private fun sendSpecialNotification(message: String, sender: String, chatName: String) {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

            val notification = NotificationCompat.Builder(this, CHANNEL_ID_SPECIAL)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Important Message")
                .setContentText("$sender${if (chatName.isNotEmpty()) " ($chatName)" else ""}: $message")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALERT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val PACKAGE_NAME = "com.tencent.mm"
        private const val CHANNEL_ID_SPECIAL = "special_messages"
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
