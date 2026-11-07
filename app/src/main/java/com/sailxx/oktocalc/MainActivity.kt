package com.sailxx.oktocalc

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private enum class Screen { CALC, HISTORY, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = SettingsStore(this)
        setContent { App(store) }
    }
}

@Composable
private fun App(store: SettingsStore) {
    val ctx = LocalView.current.context
    var settings by remember { mutableStateOf(store.load()) }
    val state = remember { CalcState(ctx) }
    var screen by rememberSaveable { mutableStateOf(Screen.CALC) }
    val theme = buildTheme(settings, isSystemInDarkTheme())
    val s = remember(settings.lang) { Strings(settings.lang) }
    val view = LocalView.current

    state.deg = settings.deg
    state.keepHistory = settings.keepHistory
    fun update(n: Settings) { settings = n; store.save(n) }

    SideEffect {
        val w = (view.context as ComponentActivity).window
        WindowCompat.getInsetsController(w, view).apply {
            isAppearanceLightStatusBars = !theme.c.dark
            isAppearanceLightNavigationBars = !theme.c.dark
        }
    }

    BackHandler(screen != Screen.CALC) { screen = Screen.CALC }

    OktoTheme(theme) {
        Box(Modifier.fillMaxSize().background(theme.c.bg).windowInsetsPadding(WindowInsets.safeDrawing)) {
            when (screen) {
                Screen.CALC -> CalcScreen(
                    state, settings, s,
                    onKey = { k ->
                        if (settings.haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        if (settings.sounds) view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                        state.press(k)
                    },
                    onToggleDeg = { update(settings.copy(deg = !settings.deg)) },
                    onHistory = { screen = Screen.HISTORY },
                    onSettings = { screen = Screen.SETTINGS },
                )
                Screen.HISTORY -> HistoryScreen(state, s) { screen = Screen.CALC }
                Screen.SETTINGS -> SettingsScreen(settings, s, ::update) { screen = Screen.CALC }
            }
        }
    }
}
