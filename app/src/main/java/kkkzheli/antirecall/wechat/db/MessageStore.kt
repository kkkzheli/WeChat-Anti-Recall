package kkkzheli.antirecall.wechat.db

import kkkzheli.antirecall.wechat.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory data store replacing Room (Room 2.6.1 / Kotlin 2.0 incompatible).
 * Author: kkkzheli
 */
class MessageStore {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun setMessages(list: List<Message>) {
        _messages.value = list
    }

    fun clearAll() {
        _messages.value = emptyList()
    }

    fun insert(message: Message) {
        _messages.value = listOf(message) + _messages.value.filter { it.id != message.id }
    }
}
