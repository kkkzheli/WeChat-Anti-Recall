package kkkzheli.antirecall.wechat.viewmodel

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kkkzheli.antirecall.wechat.App
import kkkzheli.antirecall.wechat.model.DisplayItem
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.repository.MessageRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _allMessages = MutableLiveData<List<Message>>(emptyList())
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    /** Main-screen rows: date headers, unread divider, message cards. */
    private val _displayItems = MutableLiveData<List<DisplayItem>>(emptyList())
    val displayItems: LiveData<List<DisplayItem>> = _displayItems

    /** Unread (filtered) message count under the current last-seen marker. */
    private val _unreadCount = MutableLiveData(0)
    val unreadCount: LiveData<Int> = _unreadCount

    /**
     * Set once the entrance burst has played for this process. Lives on the
     * ViewModel (which outlives screen switches) so coming back from
     * Settings/Search does not replay it.
     */
    @Volatile
    var entranceBurstPlayed = false

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
    // Declared above init: the pipeline starts collectors that call
    // stripBracketPrefix, so the regex must exist before any of them runs.
    private val BRACKET_PREFIX = Regex("^\\[[^\\]]+\\]\\s*")

    /** Timestamp of the newest message the user has laid eyes on ("unread" line). */
    private val lastSeenFlow = App.dataStore.data.map { it[KEY_LAST_SEEN] ?: 0L }

    /** Everything the list pipeline needs besides the messages themselves. */
    private data class Filters(
        val query: String,
        val contacts: Set<String>,
        val groups: Set<String>,
        val start: LocalDate?,
        val end: LocalDate?,
    )

    private val filterFlow = combine(
        _searchQuery, _selectedContacts, _selectedGroups, _startDate, _endDate
    ) { query, contacts, groups, start, end ->
        Filters(query, contacts, groups, start, end)
    }

    init {
        // Whole list pipeline — filtering, date grouping and the unread
        // divider — runs upstream on the Default dispatcher; the collect
        // block only posts the finished values on Main.
        viewModelScope.launch {
            combine(repository.getAllMessages(), lastSeenFlow, filterFlow) { all, lastSeen, f ->
                computeDisplay(all, lastSeen, f)
            }.flowOn(Dispatchers.Default).collect { r ->
                _allMessages.value = r.filtered
                _displayItems.value = r.display
                _filteredMessages.value = r.filtered
                _messages.value = r.filtered
                _messageCount.value = r.filtered.size
                _unreadCount.value = r.unreadCount
                if (r.newestTs != null) {
                    _lastCaptureTime.value = r.newestTs
                }
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

    /** Everything the main list UI needs, computed off the main thread. */
    data class DisplayResult(
        val display: List<DisplayItem>,
        val filtered: List<Message>,
        val newestTs: Long?,
        val unreadCount: Int,
    )

    /**
     * Pure, off-main computation: filter → build display rows. Returns the
     * display list, the filtered message list, the newest message's timestamp
     * (from the UNfiltered list — the running banner must not follow the
     * active search/filter), and how many filtered messages are unread.
     */
    private fun computeDisplay(
        all: List<Message>,
        lastSeen: Long,
        f: Filters,
    ): DisplayResult {
        val zone = ZoneId.systemDefault()
        val query = f.query.lowercase()

        val filtered = all.filter { message ->
            if (query.isNotEmpty()) {
                val hit = message.content.lowercase().contains(query) ||
                    message.senderName.lowercase().contains(query) ||
                    message.chatName.lowercase().contains(query)
                if (!hit) return@filter false
            }
            // Filter comparison uses cleaned names on both sides; the ||
            // short-circuits so no filter means no regex work.
            val contactMatch = f.contacts.isEmpty() || f.contacts.contains(stripBracketPrefix(message.senderName))
            val groupMatch = f.groups.isEmpty() || f.groups.contains(stripBracketPrefix(message.chatName))
            if (!contactMatch || !groupMatch) return@filter false

            if (f.start != null || f.end != null) {
                try {
                    val msgDate = Instant.ofEpochMilli(message.timestamp).atZone(zone).toLocalDate()
                    if (f.start != null && msgDate.isBefore(f.start)) return@filter false
                    if (f.end != null && msgDate.isAfter(f.end)) return@filter false
                } catch (_: Exception) {
                    return@filter false
                }
            }
            true
        }

        val display = ArrayList<DisplayItem>(filtered.size + 8)
        var currentDay = Long.MIN_VALUE
        var seenUnread = false
        var dividerPlaced = false
        for (m in filtered) {
            val day = try {
                Instant.ofEpochMilli(m.timestamp).atZone(zone).toLocalDate().toEpochDay()
            } catch (_: Exception) {
                Long.MIN_VALUE
            }

            if (day != currentDay) {
                display.add(DisplayItem.DateHeader(day))
                currentDay = day
            }
            // The unread divider sits BETWEEN the unseen block (above, newer)
            // and the first already-seen message (below, older) — placed
            // lazily, once, when the boundary is actually crossed. When the
            // whole list is unseen there is no boundary to draw, so no divider.
            if (!dividerPlaced && lastSeen > 0 && m.timestamp <= lastSeen) {
                if (seenUnread) {
                    display.add(DisplayItem.UnreadDivider)
                    dividerPlaced = true
                }
            } else if (lastSeen > 0 && m.timestamp > lastSeen) {
                seenUnread = true
            }

            display.add(DisplayItem.MessageItem(m))
        }

        val newest = all.firstOrNull()?.timestamp
        val unread = if (lastSeen > 0) filtered.count { it.timestamp > lastSeen } else 0
        return DisplayResult(display, filtered, newest, unread)
    }

    /**
     * Record that the user has seen everything up to [newestTimestamp] —
     * collapses the unread divider on the next emission. Monotonic: an older
     * timestamp never rewinds the marker.
     */
    fun markAllSeen(newestTimestamp: Long) {
        if (newestTimestamp <= 0) return
        viewModelScope.launch {
            App.dataStore.edit { prefs ->
                val current = prefs[KEY_LAST_SEEN] ?: 0L
                if (newestTimestamp > current) prefs[KEY_LAST_SEEN] = newestTimestamp
            }
        }
    }

    /** Strip any [...] prefix, e.g. [3], [3条], [3条消息]. */
    private fun stripBracketPrefix(name: String): String {
        return name.replace(BRACKET_PREFIX, "").trim()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    fun clearAllMessages() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _allMessages.value = emptyList()
            _messages.value = emptyList()
            _displayItems.value = emptyList()
            _filteredMessages.value = emptyList()
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
    }

    fun clearFilter() {
        _selectedContacts.value = emptySet()
        _selectedGroups.value = emptySet()
        _startDate.value = null
        _endDate.value = null
        _showDatePicker.value = false
    }

    /**
     * Set a date/time range filter. Either start or end may be null (partial range).
     */
    fun setDateRange(start: LocalDate?, end: LocalDate?) {
        _startDate.value = start
        _endDate.value = end
    }

    /**
     * Clear only the date range filter, keeping contacts/groups filters active.
     */
    fun clearDateRange() {
        _startDate.value = null
        _endDate.value = null
    }

    fun showDatePicker(show: Boolean) {
        _showDatePicker.value = show
    }

    fun getDisplayItems(): List<DisplayItem> = _displayItems.value.orEmpty()
    fun getMessages(): List<Message> = _messages.value.orEmpty()
    fun getContactNames(): List<String> = _contactNames.value
    fun getGroupNames(): List<String> = _groupNames.value

    companion object {
        const val GITHUB_REPO = "https://github.com/kkkzheli/WeChat-Anti-Recall"
        const val AUTHOR_NAME = "kkkzheli"
        const val APP_NAME = "Anti Recall"

        private val KEY_LAST_SEEN = longPreferencesKey("pref_last_seen_ts")
    }
}
