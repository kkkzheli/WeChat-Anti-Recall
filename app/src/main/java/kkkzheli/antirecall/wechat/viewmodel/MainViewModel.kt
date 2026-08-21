package kkkzheli.antirecall.wechat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.repository.MessageRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    // Filter/screen data exposed as StateFlow for Compose collectAsState
    private val _filteredContactsChannel = Channel<List<String>>(Channel.BUFFERED)
    val filteredContacts: StateFlow<List<String>> = _filteredContactsChannel.receiveAsFlow()
        .toStateFlow(viewModelScope, emptyList())

    private val _filteredGroupsChannel = Channel<List<String>>(Channel.BUFFERED)
    val filteredGroups: StateFlow<List<String>> = _filteredGroupsChannel.receiveAsFlow()
        .toStateFlow(viewModelScope, emptyList())

    private val _contactNamesInternal = MutableLiveData<List<String>>()
    val contactNames: StateFlow<List<String>> = _contactNamesInternal.valueAsFlow()
        .toStateFlow(viewModelScope, emptyList())

    private val _groupNamesInternal = MutableLiveData<List<String>>()
    val groupNames: StateFlow<List<String>> = _groupNamesInternal.valueAsFlow()
        .toStateFlow(viewModelScope, emptyList())

    private val _messageCount = MutableLiveData(0)
    val messageCount: LiveData<Int> = _messageCount

    // Store current lists for filtering using MutableStateFlow (not Compose mutableStateOf)
    private val _currentContactNames = MutableStateFlow(emptyList<String>())
    val currentContactNames: StateFlow<List<String>> = _currentContactNames

    private val _currentGroupNames = MutableStateFlow(emptyList<String>())
    val currentGroupNames: StateFlow<List<String>> = _currentGroupNames

    init {
        viewModelScope.launch {
            repository.getAllMessages().collect { list ->
                _messages.value = list
                _messageCount.value = list.size
            }
        }

        viewModelScope.launch {
            repository.getContactNames().collect { names ->
                val sorted = names.sortedBy { it.lowercase() }
                _contactNamesInternal.value = sorted
                _currentContactNames.value = sorted
                _filteredContactsChannel.send(sorted)
            }
        }

        viewModelScope.launch {
            repository.getGroupNames().collect { names ->
                val sorted = names.sortedBy { it.lowercase() }
                _groupNamesInternal.value = sorted
                _currentGroupNames.value = sorted
                _filteredGroupsChannel.send(sorted)
            }
        }
    }

    fun setSearchQuery(query: String) {
        // Search handled locally via localQuery state
    }

    fun setContactFilter(filter: String) {
        viewModelScope.launch {
            val all = _currentContactNames.value
            val filtered = if (filter.isBlank()) all else all.filter { it.contains(filter, ignoreCase = true) }
            _filteredContactsChannel.send(filtered)
        }
    }

    fun setGroupFilter(filter: String) {
        viewModelScope.launch {
            val all = _currentGroupNames.value
            val filtered = if (filter.isBlank()) all else all.filter { it.contains(filter, ignoreCase = true) }
            _filteredGroupsChannel.send(filtered)
        }
    }

    fun clearAllMessages() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _messages.value = emptyList()
            _messageCount.value = 0
        }
    }

    fun setNotificationPermissionDenied(denied: Boolean) {}
    fun setSystemAlertPermissionDenied(denied: Boolean) {}

    fun getContactNames(): List<String> = _currentContactNames.value.orEmpty()
    fun getMessages(): List<Message> = _messages.value.orEmpty()

    companion object {
        const val GITHUB_REPO = "https://github.com/kkkzheli/WeChat-Anti-Recall"
        const val AUTHOR_NAME = "kkkzheli"
        const val APP_NAME = "Anti Recall"
    }
}

private fun <T> MutableLiveData<T>.valueAsFlow(): Flow<T> = callbackFlow {
    trySend(value ?: return@callbackFlow)
    close()
}

private fun <T : Any> Flow<T>.toStateFlow(scope: kotlinx.coroutines.CoroutineScope, initialValue: T): StateFlow<T> {
    return callbackFlow {
        trySend(initialValue)
        this@toStateFlow.collect { value -> trySend(value) }
        close()
    }.stateIn(scope, started = SharingStarted.WhileSubscribed(5000), initialValue)
}
