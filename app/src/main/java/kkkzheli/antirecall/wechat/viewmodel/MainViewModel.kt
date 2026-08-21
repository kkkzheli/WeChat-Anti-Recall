package kkkzheli.antirecall.wechat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.repository.MessageRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _contactNames = MutableLiveData<List<String>>()
    val contactNames: LiveData<List<String>> = _contactNames

    private val _groupNames = MutableLiveData<List<String>>()
    val groupNames: LiveData<List<String>> = _groupNames

    private val _messageCount = MutableLiveData(0)
    val messageCount: LiveData<Int> = _messageCount

    private val _notificationPermissionDenied = MutableLiveData(false)
    val notificationPermissionDenied: LiveData<Boolean> = _notificationPermissionDenied

    private val _systemAlertPermissionDenied = MutableLiveData(false)
    val systemAlertPermissionDenied: LiveData<Boolean> = _systemAlertPermissionDenied

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            repository.getAllMessages().collect { messages ->
                _messages.value = messages
                _messageCount.value = messages.size
            }
        }

        viewModelScope.launch {
            repository.getContactNames().collect { names ->
                _contactNames.value = names.sortedBy { it.lowercase() }
            }
        }

        viewModelScope.launch {
            repository.getGroupNames().collect { names ->
                _groupNames.value = names.sortedBy { it.lowercase() }
            }
        }
    }

    fun setSearchQuery(query: String) {
        if (query.isBlank()) {
            loadMessages()
        } else {
            viewModelScope.launch {
                repository.searchMessages(query).collect { messages ->
                    _messages.value = messages
                }
            }
        }
    }

    fun clearAllMessages() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _messages.value = emptyList()
            _messageCount.value = 0
            _contactNames.value = emptyList()
            _groupNames.value = emptyList()
        }
    }

    fun setNotificationPermissionDenied(denied: Boolean) {
        _notificationPermissionDenied.value = denied
    }

    fun setSystemAlertPermissionDenied(denied: Boolean) {
        _systemAlertPermissionDenied.value = denied
    }

    fun getMessages(): List<Message> = _messages.value.orEmpty()
    fun getContactNames(): List<String> = _contactNames.value.orEmpty()
    fun getGroupNames(): List<String> = _groupNames.value.orEmpty()

    companion object {
        const val GITHUB_REPO = "https://github.com/kkkzheli/WeChat-Anti-Recall"
        const val AUTHOR_NAME = "kkkzheli"
        const val APP_NAME = "Anti Recall"
    }
}
