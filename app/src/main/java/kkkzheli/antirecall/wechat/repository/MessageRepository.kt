package kkkzheli.antirecall.wechat.repository

import kkkzheli.antirecall.wechat.db.MessageStore
import kkkzheli.antirecall.wechat.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emptyFlow

/**
 * Repository using an in-memory store (Room not compatible with Kotlin 2.0 / current AGP).
 * Author: kkkzheli
 */
class MessageRepository(private val store: MessageStore) {

    fun getAllMessages(): Flow<List<Message>> = store.messages

    fun searchMessages(query: String): Flow<List<Message>> = store.messages.map { messages ->
        if (query.isBlank()) messages else messages.filter {
            it.content.contains(query, ignoreCase = true) ||
                it.senderName.contains(query, ignoreCase = true)
        }
    }

    fun getContactNames(): Flow<List<String>> = emptyFlow()

    fun getGroupNames(): Flow<List<String>> = emptyFlow()

    suspend fun clearAllMessages() {
        store.clearAll()
    }

    suspend fun insertMessage(message: Message) {
        store.insert(message)
    }
}
