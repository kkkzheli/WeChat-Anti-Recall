package kkkzheli.antirecall.wechat.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.repository.MessageRepository
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(
    private val repository: MessageRepository,
    lifecycleOwner: LifecycleOwner
) : ViewModel() {

    private val _messages = mutableListOf<Message>()
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchResults = _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllMessages()
            else repository.searchMessages(query)
        }
        .stateIn(lifecycleOwner.lifecycle, SharingStarted.WhileSubscribed(500), emptyList())

    private val _contactNames = mutableListOf<String>()
    val contactNames: StateFlow<List<String>> = _contactNames.asStateFlow()

    private val _groupNames = mutableListOf<String>()
    val groupNames: StateFlow<List<String>> = _groupNames.asStateFlow()

    private val _contactFilter = MutableStateFlow("")
    val filteredContacts = combine(_contactNames, _contactFilter) { contacts, filter ->
        contacts.sortedBy { it.lowercase() }
            .filter { it.contains(filter.lowercase()) || filter.isBlank() }
    }.stateIn(lifecycleOwner.lifecycle, SharingStarted.WhileSubscribed(500), emptyList())

    private val _groupFilter = MutableStateFlow("")
    val filteredGroups = combine(_groupNames, _groupFilter) { groups, filter ->
        groups.sortedBy { it.lowercase() }
            .filter { it.contains(filter.lowercase()) || filter.isBlank() }
    }.stateIn(lifecycleOwner.lifecycle, SharingStarted.WhileSubscribed(500), emptyList())

    private val _specialMessages = mutableListOf<Message>()
    val specialMessages: StateFlow<List<Message>> = _specialMessages.asStateFlow()

    private val _messageCount = MutableStateFlow(0)
    val messageCount: StateFlow<Int> = _messageCount.asStateFlow()

    private val _notificationPermissionDenied = AtomicBoolean(false)
    private val _systemAlertPermissionDenied = AtomicBoolean(false)
    val notificationPermissionDenied: StateFlow<Boolean> =
        _notificationPermissionDenied.asStateFlow().asStateFlow()
    val systemAlertPermissionDenied: StateFlow<Boolean> =
        _systemAlertPermissionDenied.asStateFlow().asStateFlow()

    init {
        lifecycleOwner.lifecycleScope.launch {
            repository.getAllMessages().collect { list ->
                _messages.clear()
                _messages.addAll(list)
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            repository.getAllMessages().collect { list ->
                _messageCount.value = list.size
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            repository.getContactNames().collect { names ->
                _contactNames.clear()
                _contactNames.addAll(names)
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            repository.getGroupNames().collect { names ->
                _groupNames.clear()
                _groupNames.addAll(names)
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            repository.getSpecialMessages().collect { list ->
                _specialMessages.clear()
                _specialMessages.addAll(list)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setContactFilter(filter: String) {
        _contactFilter.value = filter
    }

    fun setGroupFilter(filter: String) {
        _groupFilter.value = filter
    }

    fun clearAllMessages() {
        lifecycleOwner.lifecycleScope.launch {
            repository.clearAllMessages()
            _messages.clear()
            _specialMessages.clear()
            _messageCount.value = 0
        }
    }

    fun setNotificationPermissionDenied(denied: Boolean) {
        _notificationPermissionDenied.set(denied)
    }

    fun setSystemAlertPermissionDenied(denied: Boolean) {
        _systemAlertPermissionDenied.set(denied)
    }

    fun getContactNames(): List<String> = _contactNames.sortedBy { it.lowercase() }
    fun getGroupNames(): List<String> = _groupNames.sortedBy { it.lowercase() }
    fun getMessages(): List<Message> = _messages.toList()

    companion object {
        const val GITHUB_REPO = "https://github.com/kkkzheli/WeChat-Anti-Recall"
        const val AUTHOR_NAME = "kkkzheli"
        const val APP_NAME = "Anti Recall"
    }
}
