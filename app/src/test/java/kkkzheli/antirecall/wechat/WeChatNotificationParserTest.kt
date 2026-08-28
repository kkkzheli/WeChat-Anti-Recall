package kkkzheli.antirecall.wechat

import org.junit.Assert.assertEquals
import org.junit.Test
import kkkzheli.antirecall.wechat.util.WeChatNotificationParser
import kkkzheli.antirecall.wechat.util.WeChatNotificationParser.Parsed

class WeChatNotificationParserTest {

    private fun parse(title: String, text: String) = WeChatNotificationParser.parse(title, text)

    // ---- Single chat ----

    @Test
    fun singleChatBareContent() {
        assertEquals(Parsed(false, "", "张三", "你好"), parse("张三", "你好"))
    }

    /** Sample: WeChat may emit title="王五." text="王五.: 1". */
    @Test
    fun singleChatStripsContactOwnNamePrefix() {
        assertEquals(Parsed(false, "", "王五.", "1"), parse("王五.", "王五.: 1"))
        assertEquals(Parsed(false, "", "王五.", "2"), parse("王五.", "王五.: 2"))
    }

    @Test
    fun singleChatStripsPrefixWithFullWidthColon() {
        assertEquals(Parsed(false, "", "张三", "你好"), parse("张三", "张三：你好"))
    }

    @Test
    fun singleChatStripsPrefixAfterUnreadCount() {
        assertEquals(Parsed(false, "", "王五.", "1"), parse("王五.", "[2]王五.: 1"))
    }

    @Test
    fun singleChatStripsPrefixOnEveryMergedLine() {
        assertEquals(
            Parsed(false, "", "王五.", "1\n2"),
            parse("王五.", "王五.: 1\n王五.: 2"),
        )
    }

    @Test
    fun singleChatCaseInsensitivePrefix() {
        assertEquals(Parsed(false, "", "Lisi", "hi"), parse("Lisi", "LISI: hi"))
    }

    @Test
    fun digitPrefixedContentStaysSingleChat() {
        // Times and scores must not read as a group sender prefix.
        assertEquals(Parsed(false, "", "张三", "12:00 开会"), parse("张三", "12:00 开会"))
        assertEquals(Parsed(false, "", "张三", "3:2 领先"), parse("张三", "3:2 领先"))
    }

    @Test
    fun emptyTitleStaysSingleChat() {
        assertEquals(Parsed(false, "", "", "张三: 你好"), parse("", "张三: 你好"))
    }

    // ---- Group chat ----

    @Test
    fun groupByQunInTitle() {
        assertEquals(Parsed(true, "工作群", "张三", "开会了"), parse("工作群", "张三: 开会了"))
    }

    @Test
    fun groupByEnglishGroupInTitle() {
        assertEquals(Parsed(true, "Project Group", "Amy", "standup"), parse("Project Group", "Amy: standup"))
    }

    /** Sample: group names often lack 群 — sender prefix differing from title is the signal. */
    @Test
    fun groupWithoutQunInTitle() {
        assertEquals(Parsed(true, "朋友互助", "王五.", "test"), parse("朋友互助", "王五.: test"))
    }

    /** Sample: exotic group name, no 群 anywhere. */
    @Test
    fun groupWithUnicodeName() {
        assertEquals(
            Parsed(true, "༒星⃐༾海⃐༾行⃐༾༓星海行", "哥", "这条是示例消息"),
            parse("༒星⃐༾海⃐༾行⃐༾༓星海行", "哥: 这条是示例消息"),
        )
    }

    @Test
    fun groupSystemNoticeWithoutSenderPrefix() {
        assertEquals(
            Parsed(true, "工作群", "", "你已邀请张三加入群聊"),
            parse("工作群", "你已邀请张三加入群聊"),
        )
    }

    @Test
    fun groupStripsUnreadCountBeforeSender() {
        assertEquals(Parsed(true, "工作群", "张三", "开会了"), parse("工作群", "[3条]张三: 开会了"))
    }

    @Test
    fun groupUsesFullWidthColonSeparator() {
        assertEquals(Parsed(true, "朋友互助", "王五.", "语音"), parse("朋友互助", "王五.：语音"))
    }

    /** A digit head in a group text is a time/score, not a sender: keep it in the content. */
    @Test
    fun groupKeepsDigitPrefixedContentIntact() {
        assertEquals(Parsed(true, "工作群", "", "12:00 开会"), parse("工作群", "12:00 开会"))
        assertEquals(Parsed(true, "Project Group", "", "3:2 领先"), parse("Project Group", "3:2 领先"))
    }

    /**
     * Known, accepted ambiguity (unchanged from the original parser): a single
     * chat message whose text genuinely begins "别人: " is indistinguishable
     * from a group notification and classifies as a group message. The
     * sender-prefix signal is load-bearing — real groups without 群 in the
     * title (see groupWithoutQunInTitle) rely on it.
     */
    @Test
    fun singleChatTextWithDifferentNamePrefixClassifiesAsGroup() {
        assertEquals(Parsed(true, "张三", "备注", "帮我带杯咖啡"), parse("张三", "备注: 帮我带杯咖啡"))
    }

    // ---- Shared helpers ----

    @Test
    fun stripBracketPrefixVariants() {
        assertEquals("张三: hi", WeChatNotificationParser.stripBracketPrefix("[3]张三: hi"))
        assertEquals("张三: hi", WeChatNotificationParser.stripBracketPrefix("[3条]张三: hi"))
        assertEquals("张三: hi", WeChatNotificationParser.stripBracketPrefix("[3条消息] 张三: hi"))
        assertEquals("hi", WeChatNotificationParser.stripBracketPrefix("hi"))
    }

    @Test
    fun separatorFindsEarliestOfBothColonKinds() {
        assertEquals(2, WeChatNotificationParser.findFirstSeparatorPos("ab：c"))
        assertEquals(2, WeChatNotificationParser.findFirstSeparatorPos("ab:c"))
        assertEquals(2, WeChatNotificationParser.findFirstSeparatorPos("ab：c:d"))
        assertEquals(-1, WeChatNotificationParser.findFirstSeparatorPos(":lead"))
        assertEquals(-1, WeChatNotificationParser.findFirstSeparatorPos("none"))
    }
}
