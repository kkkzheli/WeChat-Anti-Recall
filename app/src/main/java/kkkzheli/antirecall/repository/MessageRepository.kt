package kkkzheli.antirecall.wechat.repository

import kkkzheli.antirecall.wechat.db.WeChatDatabase
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.model.MessageType
import kkkzheli.antirecall.wechat.model.SpecialType
import kkkzheli.antirecall.wechat.db.WeChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepository(private val database: WeChatDatabase) {

    private val dao = database.messageDao()

    fun getAllMessages(): Flow<List<Message>> {
        return dao.getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchMessages(query: String): Flow<List<Message>> {
        return dao.searchMessages(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getMessagesByContact(contactName: String): Flow<List<Message>> {
        return dao.getMessagesByContact(contactName).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getMessagesByDateRange(startDate: String, endDate: String): Flow<List<Message>> {
        return dao.getMessagesByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getSpecialMessages(): Flow<List<Message>> {
        return dao.getSpecialMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getContactNames(): Flow<List<String>> {
        return dao.getDistinctContactNames()
    }

    fun getGroupNames(): Flow<List<String>> {
        return dao.getDistinctGroupNames()
    }

    fun getMessageCount(): Flow<Int> {
        return dao.getAllMessages().map { it.size }
    }

    suspend fun saveMessage(entity: WeChatMessageEntity): Long {
        return dao.insert(entity)
    }

    suspend fun saveMessages(entities: List<WeChatMessageEntity>) {
        dao.insertAll(entities)
    }

    suspend fun clearAllMessages() {
        dao.clearAll()
    }

    fun toDomain(entity: WeChatMessageEntity): Message {
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
            specialType = entity.specialType?.let { SpecialType.fromString(it) }
        )
    }

    fun toEntity(message: Message, index: Long = System.currentTimeMillis()): WeChatMessageEntity {
        return WeChatMessageEntity(
            content = message.content,
            senderName = message.senderName,
            chatName = message.chatName,
            messageType = message.messageType.name,
            specialType = message.specialType?.name,
            isSpecial = message.isSpecial,
            isGroup = message.senderName != message.chatName,
            timestamp = message.timestamp,
            displayDate = message.displayDate,
            displayTime = message.displayTime,
            index = index
        )
    }
}
