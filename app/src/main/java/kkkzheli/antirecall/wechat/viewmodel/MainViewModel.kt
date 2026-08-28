package kkkzheli.antirecall.wechat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.repository.MessageRepository
import java.time.LocalDate

class MainViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _allMessages = MutableLiveData<List<Message>>(emptyList())
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _searchQuery = MutableStateFlow("")

    private val _contactNames = MutableStateFlow<List<String>>(emptyList())
    val contactNames: StateFlow<List<String>> = _contactNames

    private val _groupNames = MutableStateFlow<List<String>>(emptyList())
    val groupNames: StateFlow<List<String>> = _groupNames

    // Multi-select support using Sets
    private val _selectedContacts = MutableStateFlow<Set<String>>(emptySet())
    val selectedContacts: StateFlow<Set<String>> = _selectedContacts

    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedGroups: StateFlow<Set<String>> = _selectedGroups

    // Date range filtering
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker

    private val _filteredMessages = MutableLiveData<List<Message>>()
    val filteredMessages: LiveData<List<Message>> = _filteredMessages

    private val _messageCount = MutableLiveData(0)
    val messageCount: LiveData<Int> = _messageCount

    private val _notificationPermissionDenied = MutableLiveData(false)
    val notificationPermissionDenied: LiveData<Boolean> = _notificationPermissionDenied

    private val _systemAlertPermissionDenied = MutableLiveData(false)
    val systemAlertPermissionDenied: LiveData<Boolean> = _systemAlertPermissionDenied

    // Last capture timestamp (updated when a new message is saved)
    private val _lastCaptureTime = MutableStateFlow<Long?>(null)
    val lastCaptureTime: StateFlow<Long?> = _lastCaptureTime

    // Compiled once at class level — reused by every stripBracketPrefix call.
    // Declared above init: loadMessages() starts collectors that call
    // stripBracketPrefix, so the regex must exist before any of them runs.
    private val BRACKET_PREFIX = Regex("^\\[[^\\]]+\\]\\s*")

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            repository.getAllMessages().flowOn(Dispatchers.Default).collect { list ->
                _allMessages.value = list
                if (list.isNotEmpty()) {
                    _lastCaptureTime.value = list.firstOrNull()?.timestamp
                }
                recomputeVisible()
            }
        }

        viewModelScope.launch {
            repository.getContactNames().flowOn(Dispatchers.Default).collect { names ->
                _contactNames.value = names.map(::stripBracketPrefix).sortedBy { it.lowercase() }
            }
        }

        viewModelScope.launch {
            repository.getGroupNames().flowOn(Dispatchers.Default).collect { names ->
                _groupNames.value = names.map(::stripBracketPrefix).sortedBy { it.lowercase() }
            }
        }
    }

    /** Strip any [...] prefix, e.g. [3], [3条], [3条消息]. */
    private fun stripBracketPrefix(name: String): String {
        return name.replace(BRACKET_PREFIX, "").trim()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
        recomputeVisible()
    }

    fun clearAllMessages() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _allMessages.value = emptyList()
            _messages.value = emptyList()
            _messageCount.value = 0
            _contactNames.value = emptyList()
            _groupNames.value = emptyList()
            _lastCaptureTime.value = null
            clearFilter()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun setNotificationPermissionDenied(denied: Boolean) {
        _notificationPermissionDenied.value = denied
    }

    fun setSystemAlertPermissionDenied(denied: Boolean) {
        _systemAlertPermissionDenied.value = denied
    }

    fun toggleContact(contact: String) {
        // contact is already a cleaned display name (from sorted + stripBracketPrefix in loadMessages)
        val current = _selectedContacts.value.toMutableSet()
        if (current.contains(contact)) {
            current.remove(contact)
        } else {
            current.add(contact)
        }
        _selectedContacts.value = current
        recomputeVisible()
    }

    fun toggleGroup(group: String) {
        // group is already a cleaned display name
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(group)) {
            current.remove(group)
        } else {
            current.add(group)
        }
        _selectedGroups.value = current
        recomputeVisible()
    }

    fun clearFilter() {
        _selectedContacts.value = emptySet()
        _selectedGroups.value = emptySet()
        _startDate.value = null
        _endDate.value = null
        _showDatePicker.value = false
        recomputeVisible()
    }

    /**
     * Set a date/time range filter. Either start or end may be null (partial range).
     */
    fun setDateRange(start: LocalDate?, end: LocalDate?) {
        _startDate.value = start
        _endDate.value = end
        recomputeVisible()
    }

    /**
     * Clear only the date range filter, keeping contacts/groups filters active.
     */
    fun clearDateRange() {
        _startDate.value = null
        _endDate.value = null
        recomputeVisible()
    }

    fun showDatePicker(show: Boolean) {
        _showDatePicker.value = show
    }

    private fun recomputeVisible() {
        val all = _allMessages.value.orEmpty()
        val contacts = _selectedContacts.value
        val groups = _selectedGroups.value
        val startDT = _startDate.value
        val endDT = _endDate.value
        val query = _searchQuery.value.lowercase()

        val filtered = all.filter { message ->
            // Search query — case-insensitive substring across content/sender/chat
            if (query.isNotEmpty()) {
                val hit = message.content.lowercase().contains(query) ||
                    message.senderName.lowercase().contains(query) ||
                    message.chatName.lowercase().contains(query)
                if (!hit) return@filter false
            }
            // Contact filter — compare cleaned names on both sides (|| short-circuits, so no filter = no regex work)
            val contactMatch = contacts.isEmpty() || contacts.contains(stripBracketPrefix(message.senderName))
            // Group filter — compare cleaned names on both sides
            val groupMatch = groups.isEmpty() || groups.contains(stripBracketPrefix(message.chatName))
            if (!contactMatch || !groupMatch) return@filter false

            // Date range filter — compare local dates using device timezone
            if (startDT != null || endDT != null) {
                try {
                    val msgDate = java.time.Instant.ofEpochMilli(message.timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    if (startDT != null && msgDate.isBefore(startDT)) return@filter false
                    if (endDT != null && msgDate.isAfter(endDT)) return@filter false
                } catch (_: Exception) {
                    return@filter false
                }
            }

            true
        }
        _filteredMessages.value = filtered
        _messages.value = filtered
        _messageCount.value = filtered.size
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
