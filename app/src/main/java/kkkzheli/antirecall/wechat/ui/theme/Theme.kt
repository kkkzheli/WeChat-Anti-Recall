package kkkzheli.antirecall.wechat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kkkzheli.antirecall.wechat.App

val GreenPrimary = Color(0xFF07C160)
val RedPacketRed = Color(0xFFFF4444)
val TransferOrange = Color(0xFFFF9800)
val VoiceCallGreen = Color(0xFF81C784)
val VideoCallPurple = Color(0xFFA855F7)
val GroupSkyBlue = Color(0xFF4FC3F7)
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

fun getLightColors(): AntiRecallColors {
    return AntiRecallColors(
        specialTransfer = RedPacketRed,
        specialRedPacket = RedPacketRed,
        specialVoiceCall = VoiceCallGreen,
        specialVideoCall = VideoCallPurple,
        groupAccent = GroupBlueLight,
        cardBackground = Color(0xFFFDFDFD),
        cardBorder = NeutralStroke,
        personalBackground = Color(0xFFFFFFFF),
    )
}

fun getDarkColors(): AntiRecallColors {
    return AntiRecallColors(
        specialTransfer = Color(0xFF8B2020),
        specialRedPacket = Color(0xFF8B2020),
        specialVoiceCall = Color(0xFF8B5E2B),
        specialVideoCall = Color(0xFF8B5E2B),
        groupAccent = DarkGroupBlue,
        cardBackground = Color(0xFF2A2A2A),
        cardBorder = DarkNeutralStroke,
        personalBackground = Color(0xFF1E1E1E),
    )
}

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

val LightAntiRecallColors = getLightColors()
val DarkAntiRecallColors = getDarkColors()

/** User theme preference persisted in DataStore. */
enum class ThemePreference {
    SYSTEM, DARK, LIGHT;

    companion object {
        private val KEY = booleanPreferencesKey("pref_theme_mode")

        @JvmStatic
        fun readFlow(): Flow<ThemePreference> = App.dataStore.data.map { prefs ->
            when (prefs[KEY]) {
                null -> SYSTEM
                true -> DARK
                false -> LIGHT
            }
        }

        @JvmStatic
        suspend fun write(pref: ThemePreference) {
            App.dataStore.edit { prefs ->
                prefs.clear()
                if (pref != SYSTEM) {
                    prefs[KEY] = pref == DARK
                }
            }
        }
    }
}

@Composable
fun WeChatAntiRecallTheme(
    userPreferredTheme: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val actualDark = when (userPreferredTheme) {
        ThemePreference.SYSTEM -> systemDark
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (actualDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        actualDark -> DarkScheme
        else -> LightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AntiRecallTypographyDefault,
        content = content
    )
}
