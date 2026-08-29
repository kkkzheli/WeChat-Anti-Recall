package kkkzheli.antirecall.wechat.ui.compose

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kkkzheli.antirecall.wechat.R

private val HUE_RAINBOW = listOf(
    Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
    Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
)

/**
 * HSV color picker for the custom accent: a saturation/value plane drawn over
 * the currently selected hue, plus a hue rail. Pure Compose — no platform
 * widgets — and the confirmed opaque ARGB flows back through [onConfirm].
 */
@Composable
fun AccentColorPickerDialog(
    initialArgb: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val initial = remember {
        FloatArray(3).also { AndroidColor.colorToHSV(initialArgb, it) }
    }
    var hue by remember { mutableFloatStateOf(initial[0]) }
    var sat by remember { mutableFloatStateOf(initial[1]) }
    var value by remember { mutableFloatStateOf(initial[2]) }
    val argb = AndroidColor.HSVToColor(floatArrayOf(hue, sat, value))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.accent_picker_title)) },
        text = {
            Column {
                // Saturation → value plane: white→pure hue horizontally,
                // transparent→black vertically; the pointer picks (s, v).
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        // sat/value are clamped away from the extremes: a
                        // near-white or near-black accent becomes invisible
                        // as `primary` tint on light/dark surfaces, so the
                        // plane stops short of its corners and edges.
                        .pointerInput(Unit) {
                            detectTapGestures { o ->
                                sat = (o.x / size.width).coerceIn(0.15f, 1f)
                                value = (1f - o.y / size.height).coerceIn(0.25f, 1f)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                sat = (change.position.x / size.width).coerceIn(0.15f, 1f)
                                value = (1f - change.position.y / size.height).coerceIn(0.25f, 1f)
                            }
                        },
                ) {
                    drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    val cx = sat * size.width
                    val cy = (1f - value) * size.height
                    // Cursor in dp — raw px floats would scale with density.
                    val r = 6.dp.toPx()
                    drawCircle(Color.Black, radius = r, center = Offset(cx, cy), style = Stroke(width = 2.5.dp.toPx()))
                    drawCircle(Color.White, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))
                }

                Spacer(Modifier.height(12.dp))

                // Hue rail — tap or drag to rotate the plane's base hue.
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { o ->
                                hue = (o.x / size.width * 360f).coerceIn(0f, 360f)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                hue = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                            }
                        },
                ) {
                    drawRect(Brush.horizontalGradient(HUE_RAINBOW))
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Locale-neutral hex label, memoized so the literal stays
                    // out of the composition body.
                    val hexLabel = remember(argb) { "#%06X".format(argb and 0xFFFFFF) }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(argb))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = hexLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(argb) }) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
