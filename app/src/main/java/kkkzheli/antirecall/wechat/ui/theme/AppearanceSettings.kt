package kkkzheli.antirecall.wechat.ui.theme

import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kkkzheli.antirecall.wechat.App

/** Dark/light mode, following the system by default. */
enum class ThemeMode { SYSTEM, DARK, LIGHT }

/**
 * Color-palette family. Each preset supplies a light and a dark ColorScheme;
 * [AMOLED] additionally goes pure black in dark mode for OLED panels.
 */
enum class ThemePreset { BRAND, AMOLED, GRAPHITE, WARM_SAND }

/** Spacing of the message list. */
enum class ListDensity(val rowSpacingDp: Int, val cardPaddingVerticalDp: Int) {
    COMPACT(2, 6), STANDARD(6, 10), RELAXED(10, 14)
}

/** Visual style of the top-bar app title (all still permission-gated). */
enum class TitleStyle { GRADIENT, ACCENT, STATIC }

/**
 * Which flowing-gradient palette animates the title once every permission is
 * granted (the gradient easter egg). COCKTAIL is the original blue/cyan wash.
 */
enum class TitleGradientStyle { COCKTAIL, IRIDESCENT, WARM }

/** Container colors of special messages: saturated (vivid) or night-friendly (soft). */
enum class SpecialPalette { VIVID, SOFT }

/**
 * Every user-facing appearance preference, persisted as individual DataStore
 * keys. Nothing here ever calls prefs.clear() — the old ThemePreference.write
 * wiped the whole store on every theme change, which would have erased all of
 * these settings the moment the user toggled dark mode.
 */
data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val preset: ThemePreset = ThemePreset.BRAND,
    val accentColorArgb: Int = AccentColor.DEFAULTArgb,
    val fontScale: Float = 1.0f,
    val bubbleRadiusDp: Int = 16,
    val density: ListDensity = ListDensity.STANDARD,
    val titleStyle: TitleStyle = TitleStyle.GRADIENT,
    val titleGradient: TitleGradientStyle = TitleGradientStyle.COCKTAIL,
    val specialPalette: SpecialPalette = SpecialPalette.VIVID,
) {
    companion object {
        val DEFAULT = AppearanceSettings()
    }
}

/** Curated accent swatches offered in Settings. */
enum class AccentColor(val argb: Int) {
    WECHAT_GREEN(0xFF07C160.toInt()),
    SKY_BLUE(0xFF2196F3.toInt()),
    PURPLE(0xFF9C27B0.toInt()),
    ORANGE(0xFFFF9800.toInt()),
    PINK(0xFFE91E63.toInt()),
    TEAL(0xFF00BCD4.toInt());

    companion object {
        const val DEFAULTArgb = 0xFF07C160.toInt()
        fun fromArgb(argb: Int): AccentColor = entries.find { it.argb == argb } ?: WECHAT_GREEN
    }
}

/**
 * DataStore access for [AppearanceSettings]. One repository, one place that
 * knows the key names; the theme layer and the Settings screen both read via
 * [flow]. The legacy boolean `pref_theme_mode` written by the pre-v1.6
 * ThemePreference is migrated on first read.
 */
object AppearanceRepository {

    private val KEY_MODE = androidx.datastore.preferences.core.stringPreferencesKey("pref_theme_mode_v2")
    private val KEY_DYNAMIC = androidx.datastore.preferences.core.booleanPreferencesKey("pref_dynamic_color")
    private val KEY_PRESET = androidx.datastore.preferences.core.stringPreferencesKey("pref_theme_preset")
    private val KEY_ACCENT = androidx.datastore.preferences.core.intPreferencesKey("pref_accent_color")
    private val KEY_FONT_SCALE = androidx.datastore.preferences.core.floatPreferencesKey("pref_font_scale")
    private val KEY_RADIUS = androidx.datastore.preferences.core.intPreferencesKey("pref_bubble_radius")
    private val KEY_DENSITY = androidx.datastore.preferences.core.stringPreferencesKey("pref_list_density")
    private val KEY_TITLE = androidx.datastore.preferences.core.stringPreferencesKey("pref_title_style")
    private val KEY_TITLE_GRADIENT = androidx.datastore.preferences.core.stringPreferencesKey("pref_title_gradient")
    private val KEY_SPECIAL = androidx.datastore.preferences.core.stringPreferencesKey("pref_special_palette")
    private val LEGACY_MODE = androidx.datastore.preferences.core.booleanPreferencesKey("pref_theme_mode")

    val flow: Flow<AppearanceSettings> = App.dataStore.data.map { prefs ->
        // One-time lift of the old boolean theme key into the new string key.
        val storedMode = prefs[KEY_MODE]
        val mode = when {
            storedMode != null -> storedMode.let { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM) }
            else -> when (prefs[LEGACY_MODE]) {
                true -> ThemeMode.DARK
                false -> ThemeMode.LIGHT
                null -> ThemeMode.SYSTEM
            }
        }
        AppearanceSettings(
            themeMode = mode,
            dynamicColor = prefs[KEY_DYNAMIC] ?: true,
            preset = prefs[KEY_PRESET]?.let { runCatching { ThemePreset.valueOf(it) }.getOrDefault(ThemePreset.BRAND) } ?: ThemePreset.BRAND,
            accentColorArgb = prefs[KEY_ACCENT] ?: AccentColor.DEFAULTArgb,
            fontScale = (prefs[KEY_FONT_SCALE] ?: 1f).coerceIn(0.85f, 1.30f),
            bubbleRadiusDp = (prefs[KEY_RADIUS] ?: 16).coerceIn(0, 28),
            density = prefs[KEY_DENSITY]?.let { runCatching { ListDensity.valueOf(it) }.getOrDefault(ListDensity.STANDARD) } ?: ListDensity.STANDARD,
            titleStyle = prefs[KEY_TITLE]?.let { runCatching { TitleStyle.valueOf(it) }.getOrDefault(TitleStyle.GRADIENT) } ?: TitleStyle.GRADIENT,
            titleGradient = prefs[KEY_TITLE_GRADIENT]?.let { runCatching { TitleGradientStyle.valueOf(it) }.getOrDefault(TitleGradientStyle.COCKTAIL) } ?: TitleGradientStyle.COCKTAIL,
            specialPalette = prefs[KEY_SPECIAL]?.let { runCatching { SpecialPalette.valueOf(it) }.getOrDefault(SpecialPalette.VIVID) } ?: SpecialPalette.VIVID,
        )
    }

    suspend fun write(transform: (AppearanceSettings) -> AppearanceSettings) {
        App.dataStore.edit { prefs ->
            val current = AppearanceSettings(
                themeMode = prefs[KEY_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM) }
                    ?: when (prefs[LEGACY_MODE]) {
                        true -> ThemeMode.DARK
                        false -> ThemeMode.LIGHT
                        null -> ThemeMode.SYSTEM
                    },
                dynamicColor = prefs[KEY_DYNAMIC] ?: true,
                preset = prefs[KEY_PRESET]?.let { runCatching { ThemePreset.valueOf(it) }.getOrDefault(ThemePreset.BRAND) } ?: ThemePreset.BRAND,
                accentColorArgb = prefs[KEY_ACCENT] ?: AccentColor.DEFAULTArgb,
                fontScale = (prefs[KEY_FONT_SCALE] ?: 1f).coerceIn(0.85f, 1.30f),
                bubbleRadiusDp = (prefs[KEY_RADIUS] ?: 16).coerceIn(0, 28),
                density = prefs[KEY_DENSITY]?.let { runCatching { ListDensity.valueOf(it) }.getOrDefault(ListDensity.STANDARD) } ?: ListDensity.STANDARD,
                titleStyle = prefs[KEY_TITLE]?.let { runCatching { TitleStyle.valueOf(it) }.getOrDefault(TitleStyle.GRADIENT) } ?: TitleStyle.GRADIENT,
                titleGradient = prefs[KEY_TITLE_GRADIENT]?.let { runCatching { TitleGradientStyle.valueOf(it) }.getOrDefault(TitleGradientStyle.COCKTAIL) } ?: TitleGradientStyle.COCKTAIL,
                specialPalette = prefs[KEY_SPECIAL]?.let { runCatching { SpecialPalette.valueOf(it) }.getOrDefault(SpecialPalette.VIVID) } ?: SpecialPalette.VIVID,
            )
            val next = transform(current)
            prefs[KEY_MODE] = next.themeMode.name
            prefs[KEY_DYNAMIC] = next.dynamicColor
            prefs[KEY_PRESET] = next.preset.name
            prefs[KEY_ACCENT] = next.accentColorArgb
            prefs[KEY_FONT_SCALE] = next.fontScale
            prefs[KEY_RADIUS] = next.bubbleRadiusDp
            prefs[KEY_DENSITY] = next.density.name
            prefs[KEY_TITLE] = next.titleStyle.name
            prefs[KEY_TITLE_GRADIENT] = next.titleGradient.name
            prefs[KEY_SPECIAL] = next.specialPalette.name
        }
    }
}
