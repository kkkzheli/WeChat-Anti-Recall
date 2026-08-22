package kkkzheli.antirecall.wechat.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
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

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "===== NotificationCaptureService onCreate =====")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: intent=$intent, startId=$startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        Log.i(TAG, "===== Listener connected! Service is now active =====")
    }

    override fun onListenerDisconnected() {
        Log.e(TAG, "===== Listener DISCONNECTED! Service dying! =====")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        Log.d(TAG, "Notif received: pkg=${sbn.packageName}, id=${sbn.id}")

        if (sbn.packageName != PACKAGE_NAME) return

        Log.d(TAG, "WeChat notif")

        val notification = sbn.notification ?: run {
            Log.w(TAG, "Notification is null")
            return
        }

        scope.launch {
            try {
                captureAndSave(notification, sbn.packageName)
                Log.d(TAG, "Message saved")
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn != null && sbn.packageName == PACKAGE_NAME) {
            Log.d(TAG, "Msg recalled: pkg=${sbn.packageName}")
        }
    }

    private suspend fun captureAndSave(notification: android.app.Notification, packageName: String) {
        val extras = notification.extras ?: run {
            Log.w(TAG, "Notification extras are null")
            return
        }
        val contentText = extras.getString(android.app.Notification.EXTRA_TEXT).orEmpty()
        val contentTitle = extras.getString(android.app.Notification.EXTRA_TITLE).orEmpty()

        Log.d(TAG, "  EXTRA_TEXT='${contentText.take(100)}', EXTRA_TITLE='${contentTitle}'")

        if (contentText.isEmpty() && contentTitle.isEmpty()) {
            Log.w(TAG, "  Both content empty, skipping")
            return
        }

        val senderName = extractSender(contentTitle, contentText)
        val isGroup = senderName.contains("群") || senderName.contains("Group")
        val chatName = if (isGroup) senderName else ""

        // Strip [N] prefix from message content (WeChat may prepend message count)
        val rawMessageText = contentText.ifEmpty { contentTitle }
        val messageText = stripMessageCountPrefix(rawMessageText)

        val specialInfo = specialDetector.detect(contentTitle, messageText)
        val isSpecial = specialInfo != null

        val displayText = if (specialInfo != null) {
            specialDetector.getSpecialDisplayText(specialInfo, contentTitle, messageText)
        } else {
            messageText
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
            detectMessageType(displayText)
        }

        val entity = WeChatMessageEntity(
            content = displayText,
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

        try {
            App.instance.repository.saveMessage(entity)
            Log.d(TAG, "  ✅ Saved message: sender=${entity.senderName}, chat=${entity.chatName}, type=${entity.messageType}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save message: ${e.message}", e)
        }

        if (isSpecial) {
            sendSpecialNotification(displayText, senderName, chatName)
        }
    }

    /**
     * Strip WeChat message count prefix like [3], [28], [3条] from any text.
     */
    private fun stripMessageCountPrefix(text: String): String {
        return text.replace(Regex("^\\[[\\d]+[条]?件]*?\\]\\s*"), "").trim()
    }

    private fun extractSender(title: String, text: String): String {
        val lines = text.lineSequence().take(5).filter { it.isNotBlank() }.toList()

        if (lines.isNotEmpty()) {
            for (line in lines) {
                val colonIndex = line.indexOf(':')
                if (colonIndex > 0 && colonIndex < 20) {
                    val name = line.substring(0, colonIndex).trim()
                        .let { stripMessageCountPrefix(it) }
                    if (name.isNotEmpty() && name.length < 40) {
                        return name
                    }
                }

                val spaceIndex = line.indexOf('、')
                if (spaceIndex > 3 && spaceIndex < 40) {
                    val name = line.substring(0, spaceIndex).trim()
                        .let { stripMessageCountPrefix(it) }
                    if (name.isNotEmpty() && name.length < 40) {
                        return name
                    }
                }
            }

            val firstLine = lines.first().let(::stripMessageCountPrefix).trim()
            if (firstLine.isNotEmpty()) return firstLine
        }

        val titleClean = stripMessageCountPrefix(title).trim()
        return if (titleClean.isNotEmpty()) titleClean else stripMessageCountPrefix(text).trim()
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
                .setContentTitle(getString(kkkzheli.antirecall.wechat.R.string.notification_channel_special))
                .setContentText("$sender${if (chatName.isNotEmpty()) " ($chatName)" else ""}: $message")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send special notification", e)
        }
    }

    companion object {
        const val PACKAGE_NAME = "com.tencent.mm"
        const val TAG = "AntiRecall"
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
