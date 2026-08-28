package kkkzheli.antirecall.wechat.ui.compose.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.model.SpecialType
import kkkzheli.antirecall.wechat.ui.theme.GroupBadgeBlue
import kkkzheli.antirecall.wechat.ui.theme.RedPacketRed
import kkkzheli.antirecall.wechat.ui.theme.TransferOrange
import kkkzheli.antirecall.wechat.ui.theme.VoiceCallGreen
import kkkzheli.antirecall.wechat.ui.theme.VideoCallPurple

/** Shared shapes, hoisted so composing a row during a fling allocates none. */
private val BubbleShape = RoundedCornerShape(16.dp)
private val BadgeShape = RoundedCornerShape(5.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(
    message: Message,
    onClick: (Message) -> Unit = {},
    onDelete: ((Message) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val cardColors = resolveMessageCardColors(message)

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
            // derivedStateOf collapses the per-frame offset reads to the boolean
            // flips, so dragging recomposes nothing until the strip appears or
            // disappears — it can never leak behind a settled bubble. requireOffset()
            // throws before the first layout (offset is NaN), which we swallow.
            val displaced by remember(dismissState) {
                derivedStateOf {
                    runCatching { kotlin.math.abs(dismissState.requireOffset()) > 1f }.getOrDefault(false)
                }
            }
            if (displaced) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = BubbleShape,
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
            // Flat bubbles: any elevation draws a shadow slab that reads as a
            // separate rectangle beneath the bubble. Must stay at an explicit 0.
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = BubbleShape,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BubbleShape)
                    .clickable { onClick(message) }
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
                        GroupBadge()
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
                            GroupBadge()
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

/**
 * Group marker: a solid deep-blue chip with a white ring and heavy glyph,
 * independent of the theme accent — the badge alone marks a group bubble,
 * since its background matches personal bubbles. Wraps its own width so the
 * localized word fits in every locale ("群", "Group", "群組").
 */
@Composable
private fun GroupBadge() {
    Text(
        text = stringResource(R.string.msg_group_badge),
        fontSize = 10.sp,
        color = Color.White,
        fontWeight = FontWeight.Black,
        maxLines = 1,
        modifier = Modifier
            .clip(BadgeShape)
            .background(GroupBadgeBlue)
            .border(1.dp, Color.White.copy(alpha = 0.9f), BadgeShape)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
private fun resolveMessageCardColors(message: Message): MessageCardColors {
    val scheme = MaterialTheme.colorScheme

    return when {
        message.isSpecial -> {
            when (message.specialType) {
                SpecialType.RED_PACKET -> specialCardColors(RedPacketRed, scheme)
                SpecialType.TRANSFER -> specialCardColors(TransferOrange, scheme)
                SpecialType.VOICE_CALL -> specialCardColors(VoiceCallGreen, scheme)
                SpecialType.VIDEO_CALL -> specialCardColors(VideoCallPurple, scheme)
                null -> defaultPersonalColors(scheme)
            }
        }
        // Group bubbles use the same container color as personal ones — the
        // blue 群 badge is what marks a group, not the bubble background.
        else -> defaultPersonalColors(scheme)
    }
}

private fun specialCardColors(background: Color, scheme: ColorScheme): MessageCardColors {
    return MessageCardColors(
        containerColor = background,
        timestampColor = Color.White.copy(alpha = 0.8f),
        senderColor = Color.White,
        contentColor = Color.White,
    )
}

private fun defaultPersonalColors(scheme: ColorScheme): MessageCardColors {
    return MessageCardColors(
        containerColor = scheme.surfaceContainerLow,
        timestampColor = scheme.onSurfaceVariant,
        senderColor = scheme.onSurface,
        contentColor = scheme.onSurface,
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
)
