package kkkzheli.antirecall.wechat.model

data class NotificationMessage(
    val id: Long = 0,
    val tag: String = "",
    val key: String = "",
    val contentTitle: String = "",
    val contentText: String = "",
    val subText: String = "",
    val bigText: String = "",
    val senderName: String = "",
    val chatName: String = "",
    val messageType: MessageType = MessageType.OTHER,
    val specialType: SpecialType? = null,
    val isGroup: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpecial: Boolean = false,
    val displayDate: String = "",
    val displayTime: String = ""
)
