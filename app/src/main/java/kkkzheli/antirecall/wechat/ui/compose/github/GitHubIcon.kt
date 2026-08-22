package kkkzheli.antirecall.wechat.ui.compose.github

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GitHub Octocat logo outline — custom vector drawable.
 */
val GitHubOctocat: ImageVector
    get() {
        if (_icon != null) return _icon!!
        _icon = ImageVector.Builder(
            name = "GitHub-Octocat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 96f,
            viewportHeight = 96f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13.0f, 81.0f)
                curveTo(50.0f, 93.0f, 95.0f, 65.0f, 79.0f, 18.0f)
                curveTo(73.0f, -0.5f, 36.0f, 3.0f, 32.0f, 16.0f)
                curveTo(29.0f, 27.0f, 46.0f, 31.0f, 46.0f, 31.0f)
                curveToRelative(-4.0f, 4.0f, 1.0f, 10.0f, 1.0f, 10.0f)
                lineToRelative(17.0f, 4.0f)
                curveTo(78.0f, 53.0f, 73.0f, 34.0f, 73.0f, 34.0f)
                curveToRelative(3.0f, -2.0f, 20.0f, 1.0f, 20.0f, 1.0f)
                curveToRelative(17.0f, 51.0f, -28.0f, 78.0f, -65.0f, 65.0f)
                close()
                moveTo(17.5f, 33.5f)
                curveToRelative(-1.5f, 0.0f, -3.5f, 1.0f, -3.5f, 3.5f)
                curveToRelative(0.0f, 2.5f, 2.0f, 3.5f, 3.5f, 3.5f)
                curveToRelative(1.5f, 0.0f, 3.5f, -2.0f, 3.5f, -3.5f)
                curveToRelative(0.0f, -2.5f, -2.0f, -3.5f, -3.5f, -3.5f)
                close()
                moveTo(73.5f, 33.5f)
                curveToRelative(-1.5f, 0.0f, -3.5f, 1.0f, -3.5f, 3.5f)
                curveToRelative(0.0f, 2.5f, 2.0f, 3.5f, 3.5f, 3.5f)
                curveToRelative(1.5f, 0.0f, 3.5f, -2.0f, 3.5f, -3.5f)
                curveToRelative(0.0f, -2.5f, -2.0f, -3.5f, -3.5f, -3.5f)
                close()
            }
        }.build()
        return _icon!!
    }

private var _icon: ImageVector? = null
