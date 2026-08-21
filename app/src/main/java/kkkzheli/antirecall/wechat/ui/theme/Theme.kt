package kkkzheli.antirecall.wechat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography as MaterialTypography

// -- Primary / WeChat-like palette --

val Green1 = Color(0xFF07C160)
val Green2 = Color(0xFF06AD56)
val Green3 = Color(0xFF059C4D)

val DarkGreen = Color(0xFF048A43)
val DarkGreenVariant = Color(0xFF037A3A)

val LightPrimaryBg = Color(0xFFF5FFF7)

// -- Special-message colours --

val RedPacketRed = Color(0xFFFF4444)
val RedPacketRedDark = Color(0xFFE03535)
val OrangeCall = Color(0xFFFF9800)
val OrangeCallDark = Color(0xFFE08700)

// -- Group accent --

val GroupBlue = Color(0xFF2196F3)
val GroupBlueLight = Color(0xFFBBDEFB)
val GroupBlueDark = Color(0xFF1565C0)

// -- Neutral palette --

val NeutralBackground = Color(0xFFF6F6F6)
val NeutralSurface = Color(0xFFFFFFFF)
val NeutralCard = Color(0xFFFDFDFD)
val NeutralGray = Color(0xFF666666)
val NeutralGrayLight = Color(0xFF999999)
val NeutralStroke = Color(0xFFE0E0E0)
val NeutralText = Color(0xFF333333)
val NeutralWhite = Color(0xFFFFFFFF)
val NeutralBlack = Color(0xFF1A1A1A)

// -- Dark-theme overrides --

val DarkPrimaryBg = Color(0xFF0A1F12)
val DarkSurface = Color(0xFF1E1E1E)
val DarkCard = Color(0xFF2A2A2A)
val DarkNeutralGray = Color(0xFFAAAAAA)
val DarkNeutralText = Color(0xFFE0E0E0)
val DarkNeutralStroke = Color(0xFF404040)
val DarkRedPacket = Color(0xFF8B2020)
val DarkOrangeCall = Color(0xFF8B5E2B)
val DarkGroupBlue = Color(0xFF1A5287)

// -- Typography helpers (used by MessageCard) --

object AntiRecallTypography {
    val Timestamp: Float = 11f
    val SenderName: Float = 14f
    val MessageContent: Float = 14f
    val SectionTitle: Float = 16f
    val BadgeText: Float = 12f
}

// -- Typography --

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

// -- Color schemes --

private val LightScheme = lightColorScheme(
    primary = Green1,
    onPrimary = NeutralWhite,
    secondary = Green2,
    onSecondary = NeutralWhite,
    tertiary = Green3,
    background = NeutralBackground,
    surface = NeutralSurface,
    onSurface = NeutralText,
    surfaceVariant = LightPrimaryBg,
    outline = NeutralStroke,
)

private val DarkScheme = darkColorScheme(
    primary = Green1,
    onPrimary = NeutralBlack,
    secondary = Green2,
    onSecondary = NeutralBlack,
    tertiary = Green3,
    background = Color(0xFF0D0D0D),
    surface = DarkSurface,
    onSurface = DarkNeutralText,
    surfaceVariant = DarkPrimaryBg,
    outline = DarkNeutralStroke,
)

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

val LightAntiRecallColors = AntiRecallColors(
    specialTransfer = RedPacketRed,
    specialRedPacket = RedPacketRed,
    specialVoiceCall = OrangeCall,
    specialVideoCall = OrangeCall,
    groupAccent = GroupBlue,
    cardBackground = NeutralCard,
    cardBorder = NeutralStroke,
    personalBackground = NeutralSurface,
)

val DarkAntiRecallColors = AntiRecallColors(
    specialTransfer = DarkRedPacket,
    specialRedPacket = DarkRedPacket,
    specialVoiceCall = DarkOrangeCall,
    specialVideoCall = DarkOrangeCall,
    groupAccent = DarkGroupBlue,
    cardBackground = DarkCard,
    cardBorder = DarkNeutralStroke,
    personalBackground = DarkSurface,
)

@Composable
fun WeChatAntiRecallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AntiRecallTypographyDefault,
        content = content
    )
}
