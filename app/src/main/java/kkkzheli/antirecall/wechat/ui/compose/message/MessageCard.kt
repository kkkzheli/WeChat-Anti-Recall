package kkkzheli.antirecall.wechat.ui.compose.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Arrangement
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
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .then(if (showGroupBorder) Modifier.border(width = 3.dp, color = cardColors.groupAccent, shape = RoundedCornerShape(16.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = cardColors.containerColor),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Timestamp column (left)
            Column(
                modifier = Modifier.widthIn(max = 72.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = message.displayDate.ifEmpty { "--" },
                    fontSize = 11.sp,
                    color = cardColors.timestampColor.copy(alpha = 0.8f),
                    lineHeight = 14.sp,
                )
                Text(
                    text = message.displayTime.ifEmpty { "--" },
                    fontSize = 11.sp,
                    color = cardColors.timestampColor.copy(alpha = 0.8f),
                    lineHeight = 14.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right side: sender + content
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (message.isGroup) {
                        Row(
                            modifier = Modifier
                                .size(16.dp)
                                .background(cardColors.groupAccent, RoundedCornerShape(4.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "群", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = message.senderName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cardColors.senderColor,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

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
                        fontSize = 14.sp,
                        color = cardColors.contentColor.copy(alpha = 0.92f),
                        maxLines = if (message.isSpecial) 3 else 5,
                        lineHeight = 18.sp,
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
