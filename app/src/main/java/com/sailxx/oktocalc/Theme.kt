@file:OptIn(ExperimentalTextApi::class)

package com.sailxx.oktocalc

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.ColorUtils
import java.util.Locale

// ---------- Шрифты ----------

private fun variable(res: Int, vararg weights: Int) = FontFamily(
    weights.map { Font(res, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) },
)

val Golos = variable(R.font.golos, 400, 500, 600, 700, 800)
val JbMono = variable(R.font.jbmono, 400, 500, 600, 700, 800)

// ---------- Настройки ----------

/** Какая тема выбрана. */
enum class ThemeKind { SYSTEM, OKTO, OKTO_LIGHT }

enum class Lang {
    RU, EN;

    companion object {
        fun system(): Lang = if (Locale.getDefault().language in setOf("ru", "uk", "be", "kk")) RU else EN
    }
}

data class Settings(
    val kind: ThemeKind = ThemeKind.SYSTEM,
    val lang: Lang = Lang.EN,
    val orangeAccent: Boolean = false,
    val sounds: Boolean = false,
    val keepHistory: Boolean = true,
    val deg: Boolean = true,
    val haptics: Boolean = true,
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load() = Settings(
        kind = enumOr(prefs.getString("kind", null), ThemeKind.SYSTEM),
        lang = Lang.EN,
        orangeAccent = prefs.getBoolean("orangeAccent", false),
        sounds = prefs.getBoolean("sounds", false),
        keepHistory = prefs.getBoolean("keepHistory", true),
        deg = prefs.getBoolean("deg", true),
        haptics = prefs.getBoolean("haptics", true),
    )

    fun save(s: Settings) {
        prefs.edit()
            .putBoolean("orangeAccent", s.orangeAccent)
            .putBoolean("sounds", s.sounds)
            .putBoolean("keepHistory", s.keepHistory)
            .putString("kind", s.kind.name)
                        .putString("lang", s.lang.name).putBoolean("deg", s.deg).putBoolean("haptics", s.haptics)
            .apply()
    }

    private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default
}

// ---------- Палитра Okto ----------

@Immutable
data class Palette(
    val dark: Boolean,
    val bg: Color, val onBg: Color, val muted: Color,
    val surface: Color, val surfaceHi: Color, val outline: Color,
    val primary: Color, val onPrimary: Color,
    val container: Color, val onContainer: Color,
    val accent2: Color, val onAccent2: Color,
    /** Табло: цвета цифр и мелких кнопок на нём. */
    val well: Color, val wellInk: Color, val wellDim: Color, val wellGhost: Color,
    /** Клавиши: обычная, оператор (заливка и цвет знака). */
    val key: Color, val keyInk: Color, val opKey: Color, val opInk: Color,
    val danger: Color,
)

fun hsl(h: Float, s: Float, l: Float, a: Float = 1f): Color {
    val argb = ColorUtils.HSLToColor(floatArrayOf(((h % 360f) + 360f) % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f)))
    return Color(argb).copy(alpha = a)
}

/** Тёмная: графит и оранжевый акцент из иконки приложения. */
val OktoPalette = Palette(
    dark = true,
    bg = Color(0xFF0B0B0B), onBg = Color(0xFFEDEDED), muted = Color(0xFF8E8E8E),
    surface = Color(0xFF141414), surfaceHi = Color(0xFF1C1C1C), outline = Color(0xFF2A2A2A),
    primary = Color(0xFFFF7A1A), onPrimary = Color(0xFF140A00),
    container = Color(0xFF1C1C1C), onContainer = Color(0xFFEDEDED),
    accent2 = Color(0xFF1C1C1C), onAccent2 = Color(0xFFEDEDED),
    well = Color(0xFF0B0B0B), wellInk = Color(0xFFF2F2F2), wellDim = Color(0xFF8A8A8A), wellGhost = Color(0x14FFFFFF),
    key = Color(0xFF1C1C1C), keyInk = Color(0xFFEDEDED), opKey = Color(0xFF2A1A0D), opInk = Color(0xFFFF7A1A),
    danger = Color(0xFFEF5A5F),
)

/** Светлая: та же раскладка, оранжевый затемнён до читаемого на белом. */
val OktoLightPalette = Palette(
    dark = false,
    bg = Color(0xFFF6F6F6), onBg = Color(0xFF141414), muted = Color(0xFF6B6B6B),
    surface = Color.White, surfaceHi = Color(0xFFEBEBEB), outline = Color(0xFFDADADA),
    primary = Color(0xFFFF7A1A), onPrimary = Color(0xFF140A00),
    container = Color(0xFFEBEBEB), onContainer = Color(0xFF141414),
    accent2 = Color(0xFFEBEBEB), onAccent2 = Color(0xFF141414),
    well = Color(0xFFF6F6F6), wellInk = Color(0xFF141414), wellDim = Color(0xFF7A7A7A), wellGhost = Color(0x0F000000),
    key = Color.White, keyInk = Color(0xFF141414), opKey = Color(0xFFFFE8D5), opInk = Color(0xFFC85400),
    danger = Color(0xFFD33A3F),
)

// ---------- Тема приложения ----------

@Immutable
class AppTheme(val c: Palette) {
    val font: FontFamily get() = FontFamily.SansSerif
}

val LocalAppTheme = staticCompositionLocalOf { AppTheme(OktoPalette) }

val C: Palette
    @Composable @ReadOnlyComposable get() = LocalAppTheme.current.c

/** [systemDark] — тёмная ли сейчас тема Android; учитывается только для [ThemeKind.SYSTEM]. */
fun buildTheme(s: Settings, systemDark: Boolean): AppTheme {
    val dark = s.kind == ThemeKind.OKTO || (s.kind == ThemeKind.SYSTEM && systemDark)
    val base = if (dark) OktoPalette else OktoLightPalette
    return AppTheme(base.copy(
        bg = if (dark) Color.Black else Color.White,
        onBg = if (dark) Color(0xFFF5F5F7) else Color(0xFF111111),
        muted = if (dark) Color(0xFFA1A1A6) else Color(0xFF68686D),
        surface = if (dark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        surfaceHi = if (dark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
        key = if (dark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        keyInk = if (dark) Color(0xFFF5F5F7) else Color(0xFF111111),
        opKey = if (dark) Color(0xFF303033) else Color(0xFFE5E5EA),
        opInk = if (dark) Color.White else Color(0xFF111111),
        well = if (dark) Color.Black else Color.White,
        wellInk = if (dark) Color(0xFFF5F5F7) else Color(0xFF111111),
        wellDim = if (dark) Color(0xFFA1A1A6) else Color(0xFF68686D),
        wellGhost = Color.Transparent,
        primary = if (s.orangeAccent) Color(0xFFFF971E) else if (dark) Color(0xFFF5F5F7) else Color(0xFF1C1C1E),
        onPrimary = if (s.orangeAccent || dark) Color.Black else Color.White,
        outline = if (dark) Color(0xFF38383A) else Color(0xFFDEDEE3),
    ))
}

@Composable
fun OktoTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val c = theme.c
    val scheme = if (c.dark) {
        darkColorScheme(primary = c.primary, onPrimary = c.onPrimary, background = c.bg, onBackground = c.onBg, surface = c.surface, onSurface = c.onBg, surfaceVariant = c.surfaceHi, outline = c.outline)
    } else {
        lightColorScheme(primary = c.primary, onPrimary = c.onPrimary, background = c.bg, onBackground = c.onBg, surface = c.surface, onSurface = c.onBg, surfaceVariant = c.surfaceHi, outline = c.outline)
    }
    CompositionLocalProvider(LocalAppTheme provides theme) {
        MaterialTheme(colorScheme = scheme) {
            ProvideTextStyle(TextStyle(fontFamily = theme.font, color = c.onBg), content)
        }
    }
}

// ---------- Строки ----------

class Strings(val lang: Lang) {
    private fun t(ru: String, en: String) = if (lang == Lang.RU) ru else en
    val history get() = t("История", "History")
    val settings get() = t("Настройки", "Settings")
    val clear get() = t("Очистить", "Clear")
    val back get() = t("Назад", "Back")
    val emptyHistory get() = t("Пока пусто", "Nothing here yet")
    val copied get() = t("Скопировано", "Copied")
    val deg get() = t("DEG", "DEG")
    val rad get() = t("RAD", "RAD")
    val errDiv0 get() = t("Нельзя делить на 0", "Can't divide by 0")
    val errFormat get() = t("Ошибка формата", "Format error")
    val errDomain get() = t("Недопустимое значение", "Invalid input")
    val theme get() = t("Тема", "Theme")
    val themeSystem get() = t("Системная", "System")
    val themeDark get() = t("Тёмная", "Dark")
    val themeLight get() = t("Светлая", "Light")
    val language get() = t("Язык", "Language")
    val angles get() = t("Углы", "Angles")
    val degrees get() = t("Градусы", "Degrees")
    val radians get() = t("Радианы", "Radians")
    val haptics get() = t("Вибро-отклик", "Haptic feedback")
    val sciOpen get() = t("Инженерные функции", "Scientific functions")
    val backspace get() = t("Стереть", "Delete")
    val locale: Locale get() = if (lang == Lang.RU) Locale("ru") else Locale.ENGLISH
}
