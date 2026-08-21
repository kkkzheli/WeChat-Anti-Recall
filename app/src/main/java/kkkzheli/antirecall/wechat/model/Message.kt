package kkkzheli.antirecall.wechat.model

/**
 * Represents a captured WeChat message notification.
 * Author: kkkzheli
 */
data class Message(
    val id: Long = 0,
    val senderName: String = "",
    val chatName: String = "",
    val content: String = "",
    val displayDate: String = "",
    val displayTime: String = "",
    val timestamp: Long = 0,
    val isSpecial: Boolean = false,
    val specialType: SpecialType? = null,
)
