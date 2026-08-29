package kkkzheli.antirecall.wechat.ui.compose.message

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kkkzheli.antirecall.wechat.R
import kkkzheli.antirecall.wechat.model.Message
import kkkzheli.antirecall.wechat.model.MessageType
import kkkzheli.antirecall.wechat.model.SpecialType
import kkkzheli.antirecall.wechat.ui.theme.AntiRecallColors
import kkkzheli.antirecall.wechat.ui.theme.GroupBadgeBlue
import kkkzheli.antirecall.wechat.ui.theme.LocalAntiRecallColors
import kkkzheli.antirecall.wechat.ui.theme.LocalBubbleRadius
import kkkzheli.antirecall.wechat.ui.theme.LocalListDensity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** Avatars pick a stable color per name from this fixed, theme-independent palette. */
private val AvatarPalette = listOf(
    0xFF07C160, 0xFF2196F3, 0xFF9C27B0, 0xFFFF9800,
    0xFFE91E63, 0xFF00BCD4, 0xFF7B5C3D, 0xFF607D8B,
)

private val URL_REGEX = Regex("https?://\\S+")
private val AMOUNT_REGEX = Regex("[¥￥]\\s*[0-9]+(?:[.,][0-9]+)?")

@Composable
fun MessageCard(
    message: Message,
    onClick: (Message) -> Unit = {},
    onDelete: ((Message) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val radius = LocalBubbleRadius.current
    val density = LocalListDensity.current
    val special = LocalAntiRecallColors.current
    val bubbleShape = remember(radius) { RoundedCornerShape(radius) }

    // One commit distance shared by the library, the haptic tick and the icon
    // growth — they can never drift apart.
    val commitPx = with(LocalDensity.current) { 56.dp.toPx() }

    // Delete animation, part 1: confirming the swipe only arms the removal.
    // Deleting straight from confirmValueChange would pop the row out of the
    // list in a single frame; instead the box below springs the row fully
    // off-screen first.
    var pendingDelete by remember { mutableStateOf(false) }
    var rowWidthPx by remember { mutableStateOf(0f) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                pendingDelete = true
                true
            } else {
                false
            }
        },
        positionalThreshold = { commitPx },
    )

    // Delete animation, part 2: wait until the fly-out has carried the row
    // ~90% off-screen (bounded by a 600ms grace so a stalled frame pipeline
    // can never wedge the removal), then drop the data — animateItem() on the
    // list rows glides the neighbours shut to finish the effect.
    LaunchedEffect(pendingDelete) {
        if (!pendingDelete) return@LaunchedEffect
        withTimeoutOrNull(600L) {
            snapshotFlow { runCatching { dismissState.requireOffset() }.getOrDefault(0f) }
                .first { kotlin.math.abs(it) >= rowWidthPx * 0.9f }
        }
        onDelete?.invoke(message)
    }

    // Haptic tick the moment the drag crosses the commit threshold and again
    // on the way back — the user feels "release to delete" without watching.
    val view = LocalView.current
    val thresholdPx = commitPx
    LaunchedEffect(dismissState, thresholdPx) {
        var crossed = false
        snapshotFlow { runCatching { dismissState.requireOffset() }.getOrDefault(0f) }
            .collect { offset ->
                val over = kotlin.math.abs(offset) >= thresholdPx
                if (over != crossed) {
                    crossed = over
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Reveal the strip only while the card is actually displaced from rest.
            // derivedStateOf collapses per-frame offset reads into boolean flips,
            // so dragging recomposes nothing until the strip appears/disappears —
            // and it can never leak behind a settled bubble. requireOffset()
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
                            shape = bubbleShape,
                        ),
                ) {
                    // Trash icon on both sides, growing with the drag distance —
                    // the offset is read inside the graphicsLayer lambda, i.e.
                    // in the draw phase, so dragging recomposes nothing.
                    DeleteIcon(state = dismissState, thresholdPx = thresholdPx, modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp))
                    DeleteIcon(state = dismissState, thresholdPx = thresholdPx, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp))
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { rowWidthPx = it.width.toFloat() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        val isMoney = message.specialType == SpecialType.RED_PACKET || message.specialType == SpecialType.TRANSFER
        val container = resolveContainerColor(message, special)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = container),
            // Flat bubbles: any elevation draws a shadow slab that reads as a
            // separate rectangle beneath the bubble. Must stay at an explicit 0.
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = bubbleShape,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .clickable { onClick(message) }
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = density.cardPaddingVerticalDp.dp,
                        bottom = density.cardPaddingVerticalDp.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MessageAvatar(message, special)
                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    NameRow(message, container)
                    Spacer(modifier = Modifier.height(3.dp))
                    if (isMoney) {
                        MoneyCardBody(message, special)
                    } else {
                        PlainCardBody(message)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Building blocks
// ---------------------------------------------------------------------------

@Composable
private fun MessageAvatar(message: Message, special: AntiRecallColors) {
    val (bg, icon, label) = avatarContent(message, special)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        } else {
            Text(
                text = label ?: "#",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
            )
        }
    }
}

private fun avatarContent(
    message: Message,
    special: AntiRecallColors,
): Triple<Color, ImageVector?, String?> {
    return when (message.specialType) {
        SpecialType.RED_PACKET -> Triple(special.specialRedPacket, Icons.Default.LocalMall, null)
        SpecialType.TRANSFER -> Triple(special.specialTransfer, Icons.Default.Money, null)
        SpecialType.VOICE_CALL -> Triple(special.specialVoiceCall, Icons.Default.Call, null)
        SpecialType.VIDEO_CALL -> Triple(special.specialVideoCall, Icons.Default.CallEnd, null)
        null -> {
            val name = (if (message.isGroup && message.chatName.isNotEmpty()) message.chatName else message.senderName).trim()
            val color = AvatarPalette[kotlin.math.abs(name.hashCode()) % AvatarPalette.size]
            Triple(Color(color), null, name.firstOrNull()?.uppercase())
        }
    }
}

@Composable
private fun NameRow(message: Message, containerColor: Color) {
    // The left column is the only weighted child, so the timestamp keeps just
    // its intrinsic width and sits flush at the card's top-right; on group
    // records the sender line lives inside the column so it hugs the group
    // name instead of being pushed down by the taller stamp.
    Row(verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            if (message.isGroup && message.chatName.isNotEmpty()) {
                // Group record: line 1 group name (bold) + badge; the sender
                // nickname moves to line 2 (absent on group system notices).
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GroupBadge()
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = message.chatName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = nameColor(message),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (message.senderName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = senderColor(message, containerColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    color = nameColor(message),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        MessageTimestamp(message, containerColor)
    }
}

/**
 * Top-right corner stamp: date on the first line, wall-clock time (with
 * seconds) below, both in the grey secondary color. Special bubbles sit on a
 * saturated container where grey would vanish, so there the stamp follows the
 * container's luminance — dark text on light vivid palettes (voice green /
 * transfer orange), white on dark ones — and stays legible everywhere.
 */
@Composable
private fun MessageTimestamp(message: Message, containerColor: Color) {
    val stampColor =
        if (message.specialType == null) MaterialTheme.colorScheme.onSurfaceVariant
        else if (containerColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.55f)
        else Color.White
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(start = 8.dp),
    ) {
        Text(
            text = message.displayDate.ifEmpty { "--" },
            style = MaterialTheme.typography.labelSmall,
            color = stampColor,
            maxLines = 1,
        )
        Text(
            text = message.displayTime.ifEmpty { "--" },
            style = MaterialTheme.typography.labelSmall,
            color = stampColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun nameColor(message: Message): Color =
    if (message.specialType != null) Color.White else MaterialTheme.colorScheme.onSurface

/** Secondary sender line — follows the container like [nameColor] does. */
@Composable
private fun senderColor(message: Message, containerColor: Color): Color =
    if (message.specialType == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    else if (containerColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f)
    else Color.White.copy(alpha = 0.8f)

/** Red packet / transfer body: icon tile, bold amount, original notice below. */
@Composable
private fun MoneyCardBody(message: Message, special: AntiRecallColors) {
    val parsed = remember(message.id, message.content) { parseMoneyContent(message.content) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.92f),
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (message.specialType == SpecialType.TRANSFER) Icons.Default.Money else Icons.Default.LocalMall,
                    contentDescription = null,
                    tint = if (message.specialType == SpecialType.TRANSFER) special.specialTransfer else special.specialRedPacket,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = parsed.first,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
            )
            val detail = parsed.second
            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                )
            }
        }
    }
}

/** Normal body: annotated text (links + emoji) plus the message-type glyph. */
@Composable
private fun PlainCardBody(message: Message) {
    val content = message.content
    val isSpecial = message.specialType != null
    val shown = if (isSpecial) stripLeadingEmoji(content) else content
    // colorScheme is a composable read — capture before remember.
    val linkColor = MaterialTheme.colorScheme.primary
    val annotated = remember(message.id, shown, isSpecial, linkColor) {
        buildPlainContent(shown, isSpecial, linkColor)
    }
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSpecial) Color.White.copy(alpha = 0.92f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
            modifier = Modifier.weight(1f),
        )
        typeIcon(message.messageType)?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = stringResource(typeIconDescription(message.messageType)),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 6.dp, top = 2.dp)
                    .size(14.dp),
            )
        }
    }
}

/**
 * Non-special content gets URL links (clickable, primary color) and emoji
 * enlarged by ~1 size step. Special bubbles stay plain white — a primary link
 * on a red packet background would be unreadable.
 */
private fun buildPlainContent(content: String, isSpecial: Boolean, linkColor: Color): AnnotatedString {
    val base = 14.sp
    return buildAnnotatedString {
        var last = 0
        if (!isSpecial) {
            for (m in URL_REGEX.findAll(content)) {
                append(content, last, m.range.first)
                withLink(
                    LinkAnnotation.Url(
                        m.value,
                        TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                            )
                        )
                    )
                ) { append(m.value) }
                last = m.range.last + 1
            }
        }
        append(content, last, content.length)
        var i = 0
        while (i < content.length) {
            val cp = content.codePointAt(i)
            val chars = Character.charCount(cp)
            if (isEmojiCp(cp)) addStyle(SpanStyle(fontSize = base * 1.2f), i, i + chars)
            i += chars
        }
    }
}

private fun isEmojiCp(cp: Int): Boolean =
    cp in 0x1F000..0x1FAFF || cp in 0x2600..0x27BF || cp in 0x2B00..0x2BFF || cp in 0xFE00..0xFE0F

/** Drop a leading emoji (e.g. the 💰/📞 injected by SpecialMessageDetector). */
private fun stripLeadingEmoji(content: String): String =
    content.dropWhile { it.isWhitespace() || it.isHighSurrogate() || it.isLowSurrogate() }

/** Red-packet notices arrive with an emoji prefix baked into the content. */
private fun parseMoneyContent(content: String): Pair<String, String?> {
    val cleaned = stripLeadingEmoji(content)
    val amount = AMOUNT_REGEX.find(cleaned)?.value
    val firstLine = cleaned.lineSequence().firstOrNull()?.trim().orEmpty()
    val title = amount ?: firstLine.take(24).ifBlank { "¥" }
    val detail = if (amount != null && firstLine.length > amount.length + 4) firstLine else null
    return title to detail
}

private fun typeIcon(type: MessageType): ImageVector? = when (type) {
    MessageType.VOICE -> Icons.Default.Mic
    MessageType.IMAGE -> Icons.Default.Image
    MessageType.VIDEO -> Icons.Default.Videocam
    MessageType.FILE -> Icons.Default.InsertDriveFile
    MessageType.LINK -> Icons.Default.Link
    MessageType.LOCATION -> Icons.Default.Place
    else -> null
}

private fun typeIconDescription(type: MessageType): Int = when (type) {
    MessageType.VOICE -> R.string.msg_type_voice
    MessageType.IMAGE -> R.string.msg_type_image
    MessageType.VIDEO -> R.string.msg_type_video
    MessageType.FILE -> R.string.msg_type_file
    MessageType.LINK -> R.string.msg_type_link
    MessageType.LOCATION -> R.string.msg_type_location
    else -> R.string.msg_type_other
}

@Composable
private fun DeleteIcon(
    state: SwipeToDismissBoxState,
    thresholdPx: Float,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = stringResource(R.string.delete_message_title),
        tint = MaterialTheme.colorScheme.error,
        modifier = modifier
            .graphicsLayer {
                // Draw-phase read of the drag offset: the trash grows and
                // fades in as the drag approaches the commit threshold.
                val off = runCatching { kotlin.math.abs(state.requireOffset()) }.getOrDefault(0f)
                val p = (off / thresholdPx).coerceIn(0f, 1f)
                scaleX = 0.6f + 0.4f * p
                scaleY = 0.6f + 0.4f * p
                alpha = 0.5f + 0.5f * p
            },
    )
}

@Composable
private fun GroupBadge() {
    Text(
        text = stringResource(R.string.msg_group_badge),
        fontSize = 10.sp,
        color = Color.White,
        fontWeight = FontWeight.Black,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(GroupBadgeBlue)
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(5.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
private fun resolveContainerColor(
    message: Message,
    special: AntiRecallColors,
): Color {
    // Special cards keep their saturated identity containers: the name and
    // content text inside is white, which is only readable on these colors.
    return when (message.specialType) {
        SpecialType.RED_PACKET -> special.specialRedPacket
        SpecialType.TRANSFER -> special.specialTransfer
        SpecialType.VOICE_CALL -> special.specialVoiceCall
        SpecialType.VIDEO_CALL -> special.specialVideoCall
        null -> MaterialTheme.colorScheme.surfaceContainerLow
    }
}
