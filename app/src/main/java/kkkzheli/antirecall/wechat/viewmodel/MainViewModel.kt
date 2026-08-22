package kkkzheli.antirecall.wechat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _contactNames = MutableStateFlow<List<String>>(emptyList())
    val contactNames: StateFlow<List<String>> = _contactNames

    private val _groupNames = MutableStateFlow<List<String>>(emptyList())
    val groupNames: StateFlow<List<String>> = _groupNames

    // Multi-select support using Sets
    private val _selectedContacts = MutableStateFlow<Set<String>>(emptySet())
    val selectedContacts: StateFlow<Set<String>> = _selectedContacts

    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedGroups: StateFlow<Set<String>> = _selectedGroups

    private val _filteredMessages = MutableLiveData<List<Message>>()
    val filteredMessages: LiveData<List<Message>> = _filteredMessages

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
            clearFilter()
        }
    }

    fun setNotificationPermissionDenied(denied: Boolean) {
        _notificationPermissionDenied.value = denied
    }

    fun setSystemAlertPermissionDenied(denied: Boolean) {
        _systemAlertPermissionDenied.value = denied
    }

    // Multi-select toggle methods
    fun toggleContact(contact: String) {
        val current = _selectedContacts.value.toMutableSet()
        if (current.contains(contact)) {
            current.remove(contact)
        } else {
            current.add(contact)
        }
        _selectedContacts.value = current
        applyFilters()
    }

    fun toggleGroup(group: String) {
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(group)) {
            current.remove(group)
        } else {
            current.add(group)
        }
        _selectedGroups.value = current
        applyFilters()
    }

    fun clearFilter() {
        _selectedContacts.value = emptySet()
        _selectedGroups.value = emptySet()
        loadMessages()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            var allMessages: List<Message> = emptyList()
            repository.getAllMessages().collect { allMessages = it }

            val contactsToFilter = _selectedContacts.value
            val groupsToFilter = _selectedGroups.value

            val filtered: List<Message> = allMessages.filter { message ->
                val contactMatch = contactsToFilter.isEmpty() || contactsToFilter.contains(message.senderName)
                val groupMatch = groupsToFilter.isEmpty() || groupsToFilter.contains(message.chatName)
                contactMatch && groupMatch
            }
            _filteredMessages.value = filtered
            _messages.value = filtered
            _messageCount.value = filtered.size
        }
    }

    fun getMessages(): List<Message> = _messages.value.orEmpty()
    fun getContactNames(): List<String> = _contactNames.value
    fun getGroupNames(): List<String> = _groupNames.value

    companion object {
        const val GITHUB_REPO = "https://github.com/kkkzheli/WeChat-Anti-Recall"
        const val AUTHOR_NAME = "kkkzheli"
        const val APP_NAME = "Anti Recall"
    }
}
