package kkkzheli.antirecall.wechat.model

import kkkzheli.antirecall.wechat.model.Message

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

    /**
     * A message card. [compact] is true for the 2nd+ message of a same-sender
     * run (≤5 min apart on the same day): the avatar and name row are hidden
     * and the row tightens, WeChat-style.
     */
    data class MessageItem(val message: Message, val compact: Boolean) : DisplayItem()
}
