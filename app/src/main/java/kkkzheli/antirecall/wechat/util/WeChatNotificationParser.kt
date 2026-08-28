package kkkzheli.antirecall.wechat.util

/**
 * Pure parser for WeChat (com.tencent.mm) notification titles/text.
 * No Android dependencies so it stays unit-testable.
 */
object WeChatNotificationParser {

    data class Parsed(
        val isGroup: Boolean,
        val chatName: String,
        val senderName: String,
        val content: String,
    )

    /** Strip any [...] prefix from WeChat notifications, e.g. [3], [3条], [3条消息]. */
    fun stripBracketPrefix(text: String): String {
        return text.replace(Regex("^\\[[^\\]]+\\]\\s*"), "").trim()
    }

    /**
     * Parse WeChat notification formats (com.tencent.mm):
     *  - Single chat: title = contact name, text = bare message content.
     *  - Group chat:  title = group name,   text = "sender nickname: content".
     *  - Both may carry an "[N]"/"[N条]" unread-count prefix on the text.
     *
     * A group message is recognized two ways: the title itself looks like a
     * group name (contains 群/Group — also covers group system notices, which
     * have no sender prefix), or the text carries a "name: " prefix whose name
     * differs from the title (catches groups whose name lacks 群).
     *
     * Some WeChat builds still prefix the text with the contact's own name in
     * a single chat (observed in some builds: title="王五.", text="王五.: 1"); that
     * redundant head is stripped from the content, per line, without turning
     * the message into a group message.
     */
    fun parse(rawTitle: String, rawText: String): Parsed {
        val title = stripBracketPrefix(rawTitle.trim()).trim()
        val stripped = stripBracketPrefix(rawText.trim())
        val firstLine = stripped.lineSequence().firstOrNull().orEmpty().trim()

        val sepPos = findFirstSeparatorPos(firstLine)
        val rawSender = if (sepPos in 1..59) firstLine.substring(0, sepPos).trim() else ""
        // A "name: " head only counts when it looks like a name — a pure-digit
        // prefix is a time or score ("12:00", "3:2"), not a sender.
        val sender = if (rawSender.isNotEmpty() && rawSender.any { !it.isDigit() }) rawSender else ""

        val titleLooksGroup = title.contains("群") || title.contains("Group", ignoreCase = true)
        val hasSenderPrefix =
            title.isNotEmpty() && sender.isNotEmpty() && !sender.equals(title, ignoreCase = true)

        return when {
            titleLooksGroup -> Parsed(
                isGroup = true,
                chatName = title,
                senderName = sender,
                // Only cut a "sender: " head when the head really is a sender:
                // when the digit guard blanked it, the prefix is a time or
                // score ("12:00 开会") and must stay part of the content.
                content = if (sender.isNotEmpty()) stripped.substring(sepPos + 1).trim() else stripped,
            )
            hasSenderPrefix -> Parsed(
                isGroup = true,
                chatName = title,
                senderName = sender,
                content = stripped.substring(sepPos + 1).trim(),
            )
            else -> Parsed(
                isGroup = false,
                chatName = "",
                senderName = title,
                content = stripOwnNamePrefix(stripped, title, sender),
            )
        }
    }

    fun findFirstSeparatorPos(text: String): Int {
        var minPos = -1
        listOf('：', ':').forEach { sep ->
            val pos = text.indexOf(sep)
            if (pos > 0 && (minPos < 0 || pos < minPos)) {
                minPos = pos
            }
        }
        return minPos
    }

    /**
     * Single chat, text prefixed with the contact's own name: drop that head
     * per line so merged "name: msg1\nname: msg2" texts are fully cleaned.
     * Lines whose prefix is anything else (i.e. real content) are kept as-is.
     */
    private fun stripOwnNamePrefix(text: String, title: String, sender: String): String {
        if (title.isEmpty() || sender.isEmpty() || !sender.equals(title, ignoreCase = true)) {
            return text
        }
        return text.lineSequence().joinToString("\n") { line ->
            val trimmed = line.trim()
            val p = findFirstSeparatorPos(trimmed)
            if (p in 1..59 && trimmed.substring(0, p).trim().equals(title, ignoreCase = true)) {
                trimmed.substring(p + 1).trimStart()
            } else {
                line
            }
        }
    }
}
