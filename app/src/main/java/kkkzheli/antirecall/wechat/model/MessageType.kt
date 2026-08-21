package kkkzheli.antirecall.wechat.model

enum class MessageType(val label: String) {
    TEXT("文本"),
    VOICE("语音"),
    IMAGE("图片"),
    VIDEO("视频"),
    LOCATION("位置"),
    RED_PACKET("红包"),
    TRANSFER("转账"),
    VOICE_CALL("语音通话"),
    VIDEO_CALL("视频通话"),
    LINK("链接"),
    FILE("文件"),
    SYSTEM("系统消息"),
    OTHER("其他");

    companion object {
        fun fromString(value: String?): MessageType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

enum class SpecialType {
    VOICE_CALL,
    VIDEO_CALL,
    TRANSFER,
    RED_PACKET;

    companion object {
        fun fromString(value: String?): SpecialType? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
