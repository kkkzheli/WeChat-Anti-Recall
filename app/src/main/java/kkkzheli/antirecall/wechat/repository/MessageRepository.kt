package kkkzheli.antirecall.wechat.repository

import kkkzheli.antirecall.wechat.db.WeChatDatabase
import kkkzheli.antirecall.wechat.db.WeChatMessageEntity
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.model.MessageType
import kkkzheli.antirecall.wechat.model.SpecialType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepository(private val database: WeChatDatabase) {

    private val dao = database.messageDao()

    fun getAllMessages(): Flow<List<Message>> {
        return dao.getAllMessages().map { entities -> entities.map { entityToMessage(it) } }
    }

    fun searchMessages(query: String): Flow<List<Message>> {
        return dao.searchMessages(query).map { entities -> entities.map { entityToMessage(it) } }
    }

    fun getContactNames(): Flow<List<String>> = dao.getContactNames()
    fun getGroupNames(): Flow<List<String>> = dao.getGroupNames()
    fun getMessageCount(): Flow<Int> = dao.getMessageCount()

    suspend fun saveMessage(entity: WeChatMessageEntity) {
        dao.insert(entity)
    }

    suspend fun clearAllMessages() {
        dao.clearAll()
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    private fun entityToMessage(entity: WeChatMessageEntity): Message {
        return Message(
            id = entity.id,
            content = entity.content,
            senderName = entity.senderName,
            chatName = entity.chatName,
            messageType = MessageType.fromString(entity.messageType),
            timestamp = entity.timestamp,
            displayDate = entity.displayDate,
            displayTime = entity.displayTime,
            isSpecial = entity.isSpecial,
            specialType = entity.specialType?.let { SpecialType.fromString(it) },
            isGroup = entity.isGroup
        )
    }
}
