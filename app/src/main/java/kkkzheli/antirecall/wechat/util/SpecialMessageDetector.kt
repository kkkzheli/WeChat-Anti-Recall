package kkkzheli.antirecall.wechat.util

import kkkzheli.antirecall.wechat.model.SpecialType

class SpecialMessageDetector {

    fun detect(title: String, text: String): SpecialType? {
        val combined = "$title $text".lowercase()

        if (combined.contains("语音通话") || combined.contains("voice call") ||
            combined.contains("incoming call") || combined.contains("来电")) {
            return SpecialType.VOICE_CALL
        }
        if (combined.contains("视频通话") || combined.contains("video call") ||
            combined.contains("视频来")) {
            return SpecialType.VIDEO_CALL
        }
        if (combined.contains("转账") || combined.contains("transfer") ||
            combined.contains("received a payment") || combined.contains("received a transfer")) {
            return SpecialType.TRANSFER
        }
        if (combined.contains("红包") || combined.contains("red packet") ||
            combined.contains("got a red packet") || combined.contains("领取了红包")) {
            return SpecialType.RED_PACKET
        }

        return null
    }

    fun getSpecialDisplayText(
        specialType: SpecialType,
        title: String,
        text: String
    ): String {
        return when (specialType) {
            SpecialType.VOICE_CALL -> "📞 ${title.ifEmpty { "语音通话" }}"
            SpecialType.VIDEO_CALL -> "🎥 ${title.ifEmpty { "视频通话" }}"
            SpecialType.TRANSFER -> "💰 ${title.ifEmpty { "转账" }}\n$text"
            SpecialType.RED_PACKET -> "🧧 ${title.ifEmpty { "红包" }}\n$text"
        }
    }
}
