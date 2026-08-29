package kkkzheli.antirecall.wechat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val GreenPrimary = Color(0xFF07C160)
val RedPacketRed = Color(0xFFFF4444)
val TransferOrange = Color(0xFFFF9800)
val VoiceCallGreen = Color(0xFF81C784)
val VideoCallPurple = Color(0xFFA855F7)
val GroupSkyBlue = Color(0xFF4FC3F7)
val GroupBadgeBlue = Color(0xFF1565C0)
val GroupBlue = Color(0xFF2196F3)
val GroupBlueLight = Color(0xFFBBDEFB)
val NeutralStroke = Color(0xFFE0E0E0)
val DarkNeutralStroke = Color(0xFF404040)
val DarkGroupBlue = Color(0xFF1A5287)

object AntiRecallTypography {
    val Timestamp: Float = 11f
    val SenderName: Float = 14f
    val MessageContent: Float = 14f
    val SectionTitle: Float = 16f
    val BadgeText: Float = 12f
}

/**
 * Special-message container colors. The old dark palette accidentally mapped
 * transfer/red-packet (and voice/video) onto two identical values, which made
 * the types indistinguishable — the four values below are distinct per type.
 */
@Immutable
data class AntiRecallColors(
    val specialTransfer: Color,
    val specialRedPacket: Color,
    val specialVoiceCall: Color,
    val specialVideoCall: Color,
    val groupAccent: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val personalBackground: Color,
)

/** Saturated containers — the classic look, white text on strong color. */
fun getVividColors(): AntiRecallColors = AntiRecallColors(
    specialTransfer = TransferOrange,
    specialRedPacket = RedPacketRed,
    specialVoiceCall = VoiceCallGreen,
    specialVideoCall = VideoCallPurple,
    groupAccent = GroupBlueLight,
    cardBackground = Color(0xFFFDFDFD),
    cardBorder = NeutralStroke,
    personalBackground = Color(0xFFFFFFFF),
)

/** Night-friendly muted containers — same four hues, heavily desaturated. */
fun getSoftColors(): AntiRecallColors = AntiRecallColors(
    specialTransfer = Color(0xFF8C6239),
    specialRedPacket = Color(0xFF8C3A3A),
    specialVoiceCall = Color(0xFF3A6B52),
    specialVideoCall = Color(0xFF6B4A94),
    groupAccent = DarkGroupBlue,
    cardBackground = Color(0xFF2A2A2A),
    cardBorder = DarkNeutralStroke,
    personalBackground = Color(0xFF1E1E1E),
)

private val LightScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = GreenPrimary,
    secondary = Color(0xFF5B7A6E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE6DB),
    onSecondaryContainer = Color(0xFF182F25),
    tertiary = Color(0xFF7B5C3D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEDDBB),
    onTertiaryContainer = Color(0xFF301B04),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDFBF7),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFDFBF7),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE4E2DC),
    onSurfaceVariant = Color(0xFF464642),
    outline = Color(0xFF777773),
    outlineVariant = Color(0xFFC7C5BF),
    inverseSurface = Color(0xFF30302E),
    inverseOnSurface = Color(0xFFF3F0EB),
    inversePrimary = Color(0xFFA0D6B4),
    surfaceDim = Color(0xFFDED9D4),
    surfaceBright = Color(0xFFFDFBF7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F4EF),
    surfaceContainer = Color(0xFFF1EFEB),
    surfaceContainerHigh = Color(0xFFEBE9E4),
    surfaceContainerHighest = Color(0xFFE6E3DE),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFA0D6B4),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005233),
    onPrimaryContainer = Color(0xFF7CFBBA),
    secondary = Color(0xFFB0CAC0),
    onSecondary = Color(0xFF2D443A),
    secondaryContainer = Color(0xFF435B50),
    onSecondaryContainer = Color(0xFFCCE6DB),
    tertiary = Color(0xFFDFC09F),
    onTertiary = Color(0xFF483116),
    tertiaryContainer = Color(0xFF61472B),
    onTertiaryContainer = Color(0xFFFEDDBB),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF131311),
    onBackground = Color(0xFFE5E4DF),
    surface = Color(0xFF131311),
    onSurface = Color(0xFFE5E4DF),
    surfaceVariant = Color(0xFF434740),
    onSurfaceVariant = Color(0xFFC4C7C0),
    outline = Color(0xFF8E918B),
    outlineVariant = Color(0xFF434740),
    inverseSurface = Color(0xFFE5E4DF),
    inverseOnSurface = Color(0xFF30302E),
    inversePrimary = Color(0xFF006B42),
    surfaceDim = Color(0xFF131311),
    surfaceBright = Color(0xFF393835),
    surfaceContainerLowest = Color(0xFF0E0E0D),
    surfaceContainerLow = Color(0xFF1B1B19),
    surfaceContainer = Color(0xFF1F1F1C),
    surfaceContainerHigh = Color(0xFF2A2A27),
    surfaceContainerHighest = Color(0xFF353431),
)

/** AMOLED preset: every dark surface collapses to true black (OLED off-pixels). */
private val AmoledDarkScheme = DarkScheme.copy(
    background = Color(0xFF000000),
    onBackground = Color(0xFFE5E4DF),
    surface = Color(0xFF000000),
    surfaceDim = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0F0F0F),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF242424),
    surfaceVariant = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF333330),
)

/** Graphite preset: neutral grey family, no green cast. */
private val GraphiteLightScheme = lightColorScheme(
    primary = Color(0xFF5A6068),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5A6068).copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF3C4046),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE3E3E3),
    onSurfaceVariant = Color(0xFF464646),
    outline = Color(0xFF777676),
    outlineVariant = Color(0xFFC6C6C6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F4F4),
    surfaceContainer = Color(0xFFEEEEEE),
    surfaceContainerHigh = Color(0xFFE8E8E8),
    surfaceContainerHighest = Color(0xFFE2E2E2),
)

private val GraphiteDarkScheme = darkColorScheme(
    primary = Color(0xFFC3C7CD),
    onPrimary = Color(0xFF2C3036),
    primaryContainer = Color(0xFF43474D),
    onPrimaryContainer = Color(0xFFDEE2E8),
    background = Color(0xFF141416),
    onBackground = Color(0xFFE4E4E6),
    surface = Color(0xFF141416),
    onSurface = Color(0xFFE4E4E6),
    surfaceVariant = Color(0xFF434347),
    onSurfaceVariant = Color(0xFFC5C5C8),
    outline = Color(0xFF8F8F93),
    outlineVariant = Color(0xFF434347),
    surfaceContainerLowest = Color(0xFF0E0E10),
    surfaceContainerLow = Color(0xFF1A1A1C),
    surfaceContainer = Color(0xFF1E1E20),
    surfaceContainerHigh = Color(0xFF29292B),
    surfaceContainerHighest = Color(0xFF343436),
)

/** Warm sand preset: soft paper-warm light scheme, cozy dark counterpart. */
private val WarmSandLightScheme = lightColorScheme(
    primary = Color(0xFF8A6D3B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8A6D3B).copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF5E4A26),
    background = Color(0xFFFDF8F0),
    onBackground = Color(0xFF1E1A13),
    surface = Color(0xFFFDF8F0),
    onSurface = Color(0xFF1E1A13),
    surfaceVariant = Color(0xFFEBE1D0),
    onSurfaceVariant = Color(0xFF4C4636),
    outline = Color(0xFF7E765F),
    outlineVariant = Color(0xFFCFC5B1),
    surfaceContainerLowest = Color(0xFFFFFDFA),
    surfaceContainerLow = Color(0xFFF8F2E8),
    surfaceContainer = Color(0xFFF2ECDF),
    surfaceContainerHigh = Color(0xFFECE5D6),
    surfaceContainerHighest = Color(0xFFE6DFCF),
)

private val WarmSandDarkScheme = darkColorScheme(
    primary = Color(0xFFE3C68C),
    onPrimary = Color(0xFF3F2E10),
    primaryContainer = Color(0xFF574425),
    onPrimaryContainer = Color(0xFFFEDDBB),
    background = Color(0xFF16130E),
    onBackground = Color(0xFFEBE2D4),
    surface = Color(0xFF16130E),
    onSurface = Color(0xFFEBE2D4),
    surfaceVariant = Color(0xFF4C4636),
    onSurfaceVariant = Color(0xFFCFC5B1),
    outline = Color(0xFF98907C),
    outlineVariant = Color(0xFF4C4636),
    surfaceContainerLowest = Color(0xFF100D09),
    surfaceContainerLow = Color(0xFF1D1913),
    surfaceContainer = Color(0xFF211D17),
    surfaceContainerHigh = Color(0xFF2C2821),
    surfaceContainerHighest = Color(0xFF37322B),
)

/** Applies the accent color on top of a preset scheme (dynamic color off). */
private fun ColorScheme.withAccent(accentArgb: Int): ColorScheme {
    val accent = Color(accentArgb)
    val onAccent = if (accent.luminance() > 0.5f) Color.Black else Color.White
    val container = accent.copy(alpha = 0.18f)
    return copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = container,
        onPrimaryContainer = accent,
        inversePrimary = accent,
    )
}

private fun schemeFor(preset: ThemePreset, dark: Boolean): ColorScheme = when (preset) {
    ThemePreset.BRAND -> if (dark) DarkScheme else LightScheme
    // AMOLED only changes the dark side; in light mode it falls back to brand.
    ThemePreset.AMOLED -> if (dark) AmoledDarkScheme else LightScheme
    ThemePreset.GRAPHITE -> if (dark) GraphiteDarkScheme else GraphiteLightScheme
    ThemePreset.WARM_SAND -> if (dark) WarmSandDarkScheme else WarmSandLightScheme
}

private val AntiRecallTypographyDefault = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.W400, fontSize = 57.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.W400, fontSize = 45.sp),
    displaySmall = TextStyle(fontWeight = FontWeight.W400, fontSize = 36.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.W400, fontSize = 32.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.W400, fontSize = 28.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.W400, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.W500, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.W400, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.W400, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.W400, fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp),
)

// ---------------------------------------------------------------------------
// Appearance composition locals — frozen once per settings change, read at
// composition time by the message cards and the list (never per frame).
// ---------------------------------------------------------------------------

val LocalAntiRecallColors = staticCompositionLocalOf { getVividColors() }
val LocalListDensity = staticCompositionLocalOf { ListDensity.STANDARD }
val LocalBubbleRadius = staticCompositionLocalOf { 16.dp }

@Composable
fun WeChatAntiRecallTheme(
    settings: AppearanceSettings = AppearanceSettings.DEFAULT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // Status-bar icons follow the APP theme (not the system one) — the user
    // can force dark mode in-app, and a light background needs dark icons.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            }
        }
    }

    val colorScheme = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> schemeFor(settings.preset, dark).withAccent(settings.accentColorArgb)
    }

    val antiRecallColors = if (settings.specialPalette == SpecialPalette.VIVID) getVividColors() else getSoftColors()

    CompositionLocalProvider(
        LocalAntiRecallColors provides antiRecallColors,
        LocalListDensity provides settings.density,
        LocalBubbleRadius provides settings.bubbleRadiusDp.dp,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AntiRecallTypographyDefault,
            content = content
        )
    }
}

/** Unwrap ContextWrapper chain (ContextThemeWrapper etc.) to the Activity. */
private tailrec fun android.content.Context.findActivity(): android.app.Activity? =
    when (this) {
        is android.app.Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
