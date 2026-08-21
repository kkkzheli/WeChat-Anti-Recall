package kkkzheli.antirecall.wechat.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wechat_messages")
data class WeChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val senderName: String,
    val chatName: String,
    val messageType: String,
    val specialType: String? = null,
    val isSpecial: Boolean = false,
    val isGroup: Boolean = false,
    val timestamp: Long,
    val displayDate: String,
    val displayTime: String,
    val index: Long = System.currentTimeMillis()
)
