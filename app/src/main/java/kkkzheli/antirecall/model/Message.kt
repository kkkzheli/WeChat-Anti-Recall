package kkkzheli.antirecall.wechat.model

data class Message(
    val id: Long = 0,
    val content: String,
    val senderName: String,
    val chatName: String,
    val messageType: MessageType,
    val timestamp: Long,
    val displayDate: String,
    val displayTime: String,
    val isSpecial: Boolean = false,
    val specialType: SpecialType? = null
) {
    companion object {
        val EMPTY = Message(
            content = "",
            senderName = "",
            chatName = "",
            messageType = MessageType.TEXT,
            timestamp = 0L,
            displayDate = "",
            displayTime = ""
        )
    }
}
