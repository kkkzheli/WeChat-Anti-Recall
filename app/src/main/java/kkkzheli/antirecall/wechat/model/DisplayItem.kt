package kkkzheli.antirecall.wechat.model

/**
 * One row of the main message list. Date separators, the unread divider and
 * the message cards themselves share one precomputed list so the LazyColumn
 * never does grouping work on the UI thread.
 */
sealed class DisplayItem {
    /** Sticky day separator; [epochDay] is a LocalDate.toEpochDay() value. */
    data class DateHeader(val epochDay: Long) : DisplayItem()

    /** "N new messages" divider inserted before the first unseen message. */
    data object UnreadDivider : DisplayItem()

    /** A message card — every message renders in full (avatar + name row). */
    data class MessageItem(val message: Message) : DisplayItem()
}
