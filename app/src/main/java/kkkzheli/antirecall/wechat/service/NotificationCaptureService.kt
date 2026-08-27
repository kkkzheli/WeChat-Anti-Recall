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
import java.util.TimeZone

class NotificationCaptureService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var _listenerConnected = false
    val isListenerConnected: Boolean get() = _listenerConnected

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
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
        _listenerConnected = true
        Log.i(TAG, "===== Listener connected! Service is now active =====")
        // The notification listener binding is a keep-alive anchor too — bring up
        // the foreground daemon so the whole chain runs off any granted anchor.
        startKeepAliveService()
    }

    override fun onListenerDisconnected() {
        Log.e(TAG, "===== Listener DISCONNECTED =====")
        _listenerConnected = false
    }

    private fun startKeepAliveService() {
        try {
            val intent = Intent(this, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (_: Exception) {
        }
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

        val (senderName, messageText) = extractSenderAndContent(contentTitle, contentText)
        val isGroup = senderName.contains("群") || senderName.contains("Group")
        val chatName = if (isGroup) senderName else ""

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
            Log.d(TAG, "  Saved message: sender=${entity.senderName}, chat=${entity.chatName}, type=${entity.messageType}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save message: ${e.message}", e)
        }

        if (isSpecial) {
            sendSpecialNotification(displayText, senderName, chatName)
        }
    }

    /** Strip any [...] prefix from WeChat notifications, e.g. [3], [3条], [3条消息]. */
    private fun stripBracketPrefix(text: String): String {
        return text.replace(Regex("^\\[[^\\]]+\\]\\s*"), "").trim()
    }

    /**
     * Extract sender name and message content from WeChat notification text.
     * Format: [N]/[N条]/(none) + senderName + (:|：|、) + messageContent
     * Returns Pair(senderName, content). If no separator found, title holds sender and text is content.
     */
    private fun extractSenderAndContent(title: String, text: String): Pair<String, String> {
        val firstLine = text.trim().lineSequence().firstOrNull().orEmpty()
        val stripped = stripBracketPrefix(firstLine).trim()

        val sepPos = findFirstSeparatorPos(stripped)

        val sender = if (sepPos > 0 && sepPos < 60) {
            stripped.substring(0, sepPos).trim()
        } else {
            // No separator — single message; title holds sender, text IS the content
            return stripBracketPrefix(title).trim() to stripped
        }

        val content = if (sepPos > 0 && sepPos < stripped.length) {
            stripped.substring(sepPos + 1).trim()
        } else {
            ""
        }

        // Sanity checks
        if (sender.isEmpty() || sender.length > 60) {
            return stripBracketPrefix(title).trim() to content
        }
        return sender to content
    }

    private fun findFirstSeparatorPos(text: String): Int {
        var minPos = -1
        listOf('：', ':', '、').forEach { sep ->
            val pos = text.indexOf(sep)
            if (pos > 0 && (minPos < 0 || pos < minPos)) {
                minPos = pos
            }
        }
        return minPos
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
