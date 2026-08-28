package kkkzheli.antirecall.wechat.ui.compose.message

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.model.SpecialType
import kkkzheli.antirecall.wechat.ui.theme.RedPacketRed
import kkkzheli.antirecall.wechat.ui.theme.OrangeCall

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(
    message: Message,
    onClick: (Message) -> Unit = {},
    onDelete: ((Message) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val cardColors = resolveMessageCardColors(message)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete?.invoke(message)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Reveal the strip only while the card is actually displaced from rest.
            // Reading requireOffset() also makes this scope recompose live as the card
            // moves, so the strip can never leak behind a settled bubble. requireOffset()
            // throws before the first layout (offset is NaN), which we swallow.
            val displaced = runCatching { kotlin.math.abs(dismissState.requireOffset()) > 1f }.getOrDefault(false)
            if (displaced) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(16.dp),
                        ),
                ) {
                    // Trash icon on both sides: right for end-to-start swipes,
                    // left for start-to-end swipes.
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_message_title),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp),
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_message_title),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColors.containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = { onClick(message) },
                    )
                    .alpha(if (isPressed) 0.7f else 1f)
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
                if (message.isGroup && message.chatName.isNotEmpty()) {
                    // Group record: line 1 group name (bold), line 2 sender
                    // nickname (absent on group system notices), line 3 content.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GroupBadge(cardColors.groupAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.chatName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardColors.senderColor,
                            maxLines = 1,
                        )
                    }
                    if (message.senderName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.bodySmall,
                            color = cardColors.senderColor.copy(alpha = 0.72f),
                            maxLines = 1,
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (message.isGroup) {
                            GroupBadge(cardColors.groupAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.titleMedium,
                            color = cardColors.senderColor,
                            maxLines = 1,
                        )
                    }
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
}

@Composable
private fun GroupBadge(accent: Color) {
    Row(
        modifier = Modifier
            .size(16.dp)
            .background(accent, RoundedCornerShape(4.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.msg_group_badge), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
