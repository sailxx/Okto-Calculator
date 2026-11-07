package com.sailxx.oktocalc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun PanelHeader(title: String, onBack: () -> Unit, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(end = 12.dp)) {
            Text("‹", fontSize = 32.sp, color = C.onBg)
            Spacer(Modifier.width(6.dp))
            Text("Back", color = C.onBg, fontSize = 16.sp)
        }
        Spacer(Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action, color = C.onBg) }
    }
    Text(title, Modifier.padding(top = 24.dp, bottom = 24.dp), color = C.onBg,
        fontSize = 34.sp, fontWeight = FontWeight.Medium, letterSpacing = (-1).sp)
}

@Composable
private fun Caption(text: String) {
    Text(text, Modifier.padding(top = 24.dp, bottom = 8.dp), color = C.muted,
        fontSize = 14.sp, fontWeight = FontWeight.Normal)
}

@Composable
private fun SettingRow(label: String, value: String? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Column {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label, Modifier.weight(1f), color = C.onBg, fontSize = 16.sp)
            if (trailing != null) trailing() else {
                if (value != null) Text(value, color = C.muted, fontSize = 14.sp)
                if (onClick != null) Text("›", color = C.muted, fontSize = 24.sp)
            }
        }
        HorizontalDivider(color = C.outline, thickness = .5.dp)
    }
}

@Composable
private fun PreferenceSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    SettingRow(label, trailing = {
        Switch(checked, onChange, modifier = Modifier.semantics { contentDescription = label }, colors = SwitchDefaults.colors(
            checkedTrackColor = C.primary, checkedThumbColor = C.onPrimary,
            uncheckedTrackColor = C.surfaceHi, uncheckedThumbColor = C.muted,
            uncheckedBorderColor = C.outline))
    })
}

@Composable
fun SettingsScreen(settings: Settings, s: Strings, onChange: (Settings) -> Unit, onBack: () -> Unit) {
    var dialog by remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp)) {
            PanelHeader(s.settings, onBack)
            Caption("Appearance")
            SettingRow(s.theme, when (settings.kind) {
                ThemeKind.SYSTEM -> s.themeSystem; ThemeKind.OKTO -> s.themeDark; ThemeKind.OKTO_LIGHT -> s.themeLight
            }, { dialog = "theme" })
            SettingRow("Accent color", if (settings.orangeAccent) "Orange" else "White", { dialog = "accent" })
            Caption("Preferences")
            PreferenceSwitch(s.haptics, settings.haptics) { onChange(settings.copy(haptics = it)) }
            PreferenceSwitch("Key sounds", settings.sounds) { onChange(settings.copy(sounds = it)) }
            PreferenceSwitch("Calculation history", settings.keepHistory) { onChange(settings.copy(keepHistory = it)) }
            Text("Keep calculations to revisit them later. Turning this off does not delete saved calculations.",
                Modifier.padding(top = 8.dp), color = C.muted, fontSize = 12.sp, lineHeight = 18.sp)
            SettingRow(s.angles, if (settings.deg) s.degrees else s.radians, { dialog = "angles" })
            Caption("App")
            SettingRow("About", onClick = { dialog = "about" })
            Spacer(Modifier.height(32.dp))
        }
    }
    if (dialog != null) {
        val choices: List<Pair<String, () -> Unit>> = when (dialog) {
            "theme" -> listOf(s.themeSystem to { onChange(settings.copy(kind = ThemeKind.SYSTEM)) },
                s.themeDark to { onChange(settings.copy(kind = ThemeKind.OKTO)) },
                s.themeLight to { onChange(settings.copy(kind = ThemeKind.OKTO_LIGHT)) })
            "accent" -> listOf("White" to { onChange(settings.copy(orangeAccent = false)) }, "Orange" to { onChange(settings.copy(orangeAccent = true)) })
            "angles" -> listOf(s.degrees to { onChange(settings.copy(deg = true)) }, s.radians to { onChange(settings.copy(deg = false)) })
            else -> emptyList()
        }
        AlertDialog(onDismissRequest = { dialog = null },
            title = { Text(when (dialog) { "theme" -> s.theme; "accent" -> "Accent color"; "angles" -> s.angles; else -> "Okto Calculator" }) },
            text = {
                Column {
                    if (dialog == "about") Text("Version ${BuildConfig.VERSION_NAME}\n\nA quieter way to calculate.\nWorks offline. No ads or tracking.")
                    choices.forEach { (name, action) ->
                        TextButton(onClick = { action(); dialog = null }, modifier = Modifier.fillMaxWidth()) {
                            Text(name, Modifier.fillMaxWidth(), color = C.onBg, textAlign = TextAlign.Start)
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("Done", color = C.onBg) } })
    }
}

@Composable
fun HistoryScreen(state: CalcState, s: Strings, onBack: () -> Unit) {
    var confirmClear by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)) {
            PanelHeader(s.history, onBack, if (state.history.isEmpty()) null else s.clear) { confirmClear = true }
            if (state.history.isEmpty()) {
                Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.emptyHistory, color = C.onBg, fontSize = 22.sp)
                    Text("Your calculations will appear here.", Modifier.padding(top = 8.dp), color = C.muted)
                    TextButton(onClick = onBack) { Text("Start calculating", color = C.onBg) }
                }
            } else LazyColumn {
                items(state.history) { item ->
                    Column(Modifier.fillMaxWidth().clickable(role = Role.Button) { state.insert(item.result); onBack() }.padding(vertical = 16.dp), horizontalAlignment = Alignment.End) {
                        Text(CalcState.pretty(item.expr, " ") + " =", color = C.muted, fontSize = 16.sp, textAlign = TextAlign.End)
                        Text(CalcState.pretty(item.result, " "), color = C.onBg, fontSize = 30.sp, textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = C.outline, thickness = .5.dp)
                }
            }
        }
    }
    if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false },
        title = { Text("Clear history?") }, text = { Text("This removes all saved calculations from this device.") },
        confirmButton = { TextButton(onClick = { state.clearHistory(); confirmClear = false }) { Text("Clear", color = C.danger) } },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel", color = C.onBg) } })
}
