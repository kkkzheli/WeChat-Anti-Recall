package kkkzheli.antirecall.wechat.ui.compose.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.model.SpecialType
import kkkzheli.antirecall.wechat.ui.theme.*

@Composable
fun MessageCard(
    message: Message,
    onClick: (Message) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val cardColors = resolveMessageCardColors(message, isDark)
    val showGroupBorder = message.chatName.isNotEmpty() && message.senderName != message.chatName

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(message) })
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .then(if (showGroupBorder) Modifier.border(width = 3.dp, color = cardColors.groupAccent, shape = RoundedCornerShape(8.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = cardColors.containerColor),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Timestamp column (left)
            Column(
                modifier = Modifier.widthIn(max = 72.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = message.displayDate.ifEmpty { "--" },
                    fontSize = 10.sp,
                    color = cardColors.timestampColor,
                    lineHeight = 12.sp,
                )
                Text(
                    text = message.displayTime.ifEmpty { "--" },
                    fontSize = 10.sp,
                    color = cardColors.timestampColor,
                    lineHeight = 12.sp,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right side: sender + content
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = message.senderName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cardColors.senderColor,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 2.dp),
                )

                Row(verticalAlignment = Alignment.Top) {
                    // Special icon
                    if (message.isSpecial && message.specialType != null) {
                        Icon(
                            imageVector = getSpecialIcon(message.specialType),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Top)
                                .padding(end = 4.dp),
                            tint = Color.White,
                        )
                    }

                    Text(
                        text = message.content,
                        fontSize = 13.sp,
                        color = cardColors.contentColor,
                        maxLines = if (message.isSpecial) 2 else 4,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun resolveMessageCardColors(message: Message, isDark: Boolean): MessageCardColors {
    val colors = when {
        message.isSpecial -> {
            when (message.specialType) {
                SpecialType.RED_PACKET, SpecialType.TRANSFER -> {
                    MessageCardColors(
                        containerColor = if (isDark) DarkAntiRecallColors.specialRedPacket else RedPacketRed,
                        timestampColor = Color.White.copy(alpha = 0.8f),
                        senderColor = Color.White,
                        contentColor = Color.White,
                        groupAccent = GroupBlueLight,
                    )
                }
                SpecialType.VOICE_CALL, SpecialType.VIDEO_CALL -> {
                    MessageCardColors(
                        containerColor = if (isDark) DarkAntiRecallColors.specialVoiceCall else OrangeCall,
                        timestampColor = Color.White.copy(alpha = 0.8f),
                        senderColor = Color.White,
                        contentColor = Color.White,
                        groupAccent = GroupBlueLight,
                    )
                }
                null -> defaultPersonalColors(isDark)
            }
        }
        message.chatName.isNotEmpty() && message.senderName != message.chatName -> {
            val bg = if (isDark) DarkAntiRecallColors.cardBackground else LightPrimaryBg
            MessageCardColors(
                containerColor = bg,
                timestampColor = if (isDark) DarkNeutralGray else NeutralGrayLight,
                senderColor = if (isDark) DarkNeutralText else NeutralText,
                contentColor = if (isDark) DarkNeutralText else NeutralText,
                groupAccent = if (isDark) DarkGroupBlue else GroupBlue,
            )
        }
        else -> defaultPersonalColors(isDark)
    }
    return colors
}

private fun defaultPersonalColors(isDark: Boolean): MessageCardColors {
    return MessageCardColors(
        containerColor = if (isDark) DarkAntiRecallColors.cardBackground else NeutralSurface,
        timestampColor = if (isDark) DarkNeutralGray else NeutralGrayLight,
        senderColor = if (isDark) DarkNeutralText else NeutralText,
        contentColor = if (isDark) DarkNeutralText else NeutralText,
        groupAccent = GroupBlueLight,
    )
}

@Composable
private fun getSpecialIcon(type: SpecialType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        SpecialType.RED_PACKET -> Icons.Default.LocalMall
        SpecialType.TRANSFER -> Icons.Default.Money
        SpecialType.VOICE_CALL -> Icons.Default.Call
        SpecialType.VIDEO_CALL -> Icons.Default.CallEnd
    }
}

private data class MessageCardColors(
    val containerColor: Color,
    val timestampColor: Color,
    val senderColor: Color,
    val contentColor: Color,
    val groupAccent: Color,
)
