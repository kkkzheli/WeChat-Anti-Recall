package kkkzheli.antirecall.wechat.ui.compose.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.model.SpecialType
import kkkzheli.antirecall.wechat.ui.theme.RedPacketRed
import kkkzheli.antirecall.wechat.ui.theme.OrangeCall

@Composable
fun MessageCard(
    message: Message,
    onClick: (Message) -> Unit = {},
    onLongPress: ((Message) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val cardColors = resolveMessageCardColors(message)
    var pressed by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColors.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        val clickModifier = if (onLongPress != null) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick(message) },
                    onLongPress = { onLongPress(message) },
                    onPress = {
                        pressed = true
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                )
            }
        } else {
            Modifier
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { if (onLongPress == null) onClick(message) })
                .then(clickModifier)
                .alpha(if (pressed) 0.7f else 1f)
                .padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 72.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = message.displayDate.ifEmpty { "--" },
                    style = MaterialTheme.typography.labelSmall,
                    color = cardColors.timestampColor.copy(alpha = 0.8f),
                )
                Text(
                    text = message.displayTime.ifEmpty { "--" },
                    style = MaterialTheme.typography.labelSmall,
                    color = cardColors.timestampColor.copy(alpha = 0.8f),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (message.isGroup) {
                        Row(
                            modifier = Modifier
                                .size(16.dp)
                                .background(cardColors.groupAccent, RoundedCornerShape(4.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = stringResource(R.string.msg_group_badge), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.titleMedium,
                        color = cardColors.senderColor,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.Top) {
                    if (message.isSpecial && message.specialType != null) {
                        Icon(
                            imageVector = getSpecialIcon(message.specialType),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).align(Alignment.Top).padding(end = 4.dp),
                            tint = Color.White,
                        )
                    }

                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cardColors.contentColor.copy(alpha = 0.92f),
                        maxLines = if (message.isSpecial) 3 else 5,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun resolveMessageCardColors(message: Message): MessageCardColors {
    val scheme = MaterialTheme.colorScheme

    return when {
        message.isSpecial -> {
            when (message.specialType) {
                SpecialType.RED_PACKET, SpecialType.TRANSFER -> {
                    MessageCardColors(
                        containerColor = RedPacketRed,
                        timestampColor = Color.White.copy(alpha = 0.8f),
                        senderColor = Color.White,
                        contentColor = Color.White,
                        groupAccent = scheme.primaryContainer,
                    )
                }
                SpecialType.VOICE_CALL, SpecialType.VIDEO_CALL -> {
                    MessageCardColors(
                        containerColor = OrangeCall,
                        timestampColor = Color.White.copy(alpha = 0.8f),
                        senderColor = Color.White,
                        contentColor = Color.White,
                        groupAccent = scheme.primaryContainer,
                    )
                }
                null -> defaultPersonalColors(scheme)
            }
        }
        message.chatName.isNotEmpty() && message.senderName != message.chatName -> {
            MessageCardColors(
                containerColor = scheme.surfaceVariant,
                timestampColor = scheme.onSurfaceVariant,
                senderColor = scheme.onSurface,
                contentColor = scheme.onSurface,
                groupAccent = scheme.primary,
            )
        }
        else -> defaultPersonalColors(scheme)
    }
}

private fun defaultPersonalColors(scheme: ColorScheme): MessageCardColors {
    return MessageCardColors(
        containerColor = scheme.surfaceContainerLow,
        timestampColor = scheme.onSurfaceVariant,
        senderColor = scheme.onSurface,
        contentColor = scheme.onSurface,
        groupAccent = scheme.primaryContainer,
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
