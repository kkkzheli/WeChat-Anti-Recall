package kkkzheli.antirecall.wechat.model

data class Contact(
    val name: String,
    val isGroup: Boolean = false,
    val messageCount: Int = 0,
    val lastMessageTime: Long = 0L,
    val avatarUrl: String? = null
) {
    val sortKey: String
        get() = name.trim().ifEmpty { "zzz" }.lowercase()
}
