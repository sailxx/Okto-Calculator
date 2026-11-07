package com.sailxx.oktocalc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput

private const val GROUP_SEP = " "

/** Малая клавиша на табло: DEG/RAD, f(x), История, Настройки. */
@Composable
fun Chip(text: String, modifier: Modifier = Modifier, active: Boolean = false, onClick: () -> Unit) {
    val c = C
    val bg = if (active) c.primary else c.wellGhost
    val fg = if (active) c.onPrimary else c.wellDim
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = fg,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun DisplayLine(text: String, size: Float, color: androidx.compose.ui.graphics.Color, bold: Boolean) {
    val sz by animateFloatAsState(size, label = "sz")
    val scroll = rememberScrollState()
    LaunchedEffect(text, scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            text,
            modifier = Modifier.horizontalScroll(scroll),
            color = color,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (bold) FontWeight.Light else FontWeight.Normal,
            fontSize = sz.sp,
            lineHeight = (sz * 1.15f).sp,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.End,
        )
    }
}

private fun bigSize(len: Int): Float = when {
    len <= 7 -> 66f; len <= 10 -> 52f; len <= 14 -> 40f; else -> 30f
}

@Composable
fun Display(
    state: CalcState,
    s: Strings,
    sciOpen: Boolean,
    showSciToggle: Boolean,
    onToggleSci: () -> Unit,
    onToggleDeg: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = C
    val ctx = LocalContext.current
    val small = 19f

    val main: String
    val mainBig: Boolean
    val second: String
    var secondColor = c.wellDim
    if (state.evaluated) {
        main = CalcState.pretty(state.expr, GROUP_SEP); mainBig = true
        second = CalcState.pretty(state.prevExpr, GROUP_SEP) + " ="
    } else {
        main = CalcState.pretty(state.expr, GROUP_SEP).ifEmpty { "0" }; mainBig = false
        val err = state.error
        if (err != null) {
            secondColor = c.danger
            second = when (err) {
                CalcError.Kind.DIV0 -> s.errDiv0
                CalcError.Kind.FORMAT -> s.errFormat
                CalcError.Kind.DOMAIN -> s.errDomain
            }
        } else second = state.preview?.let { CalcState.pretty(it, GROUP_SEP) } ?: ""
    }

    Column(
        modifier
            .pointerInput(state.expr, state.evaluated) {
                detectTapGestures(onLongPress = {
                    val text = if (state.evaluated) state.expr else state.preview ?: state.expr
                    if (text.isNotEmpty()) {
                        (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("calc", text.replace('−', '-')))
                        Toast.makeText(ctx, s.copied, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            NavigationIcon(true, "Calculation history", onHistory)
            Spacer(Modifier.weight(1f))
            Chip(if (state.deg) s.deg else s.rad, onClick = onToggleDeg)
            if (showSciToggle) Chip("f(x)", active = sciOpen, onClick = onToggleSci)
            Spacer(Modifier.weight(1f))
            NavigationIcon(false, "Open settings", onSettings)
        }
        Spacer(Modifier.weight(1f))
        Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            if (state.evaluated) {
                DisplayLine(second, small, c.wellDim, false)
                DisplayLine(main, bigSize(main.length), c.wellInk, true)
            } else {
                DisplayLine(main, bigSize(main.length), c.wellInk, true)
                DisplayLine(second.ifEmpty { " " }, small, secondColor, false)
            }
        }
    }
}

// ---------- Клавиатура ----------

private class KeyDef(val id: String, val label: String, val kind: KeyKind, val desc: String = label)

private val mainRows = listOf(
    listOf(KeyDef("AC", "AC", KeyKind.CLEAR), KeyDef("±", "±", KeyKind.CLEAR), KeyDef("%", "%", KeyKind.CLEAR), KeyDef("÷", "÷", KeyKind.OP)),
    listOf(KeyDef("7", "7", KeyKind.DIGIT), KeyDef("8", "8", KeyKind.DIGIT), KeyDef("9", "9", KeyKind.DIGIT), KeyDef("×", "×", KeyKind.OP)),
    listOf(KeyDef("4", "4", KeyKind.DIGIT), KeyDef("5", "5", KeyKind.DIGIT), KeyDef("6", "6", KeyKind.DIGIT), KeyDef("−", "−", KeyKind.OP)),
    listOf(KeyDef("1", "1", KeyKind.DIGIT), KeyDef("2", "2", KeyKind.DIGIT), KeyDef("3", "3", KeyKind.DIGIT), KeyDef("+", "+", KeyKind.OP)),
    listOf(KeyDef("DEL", "⌫", KeyKind.DEL), KeyDef("0", "0", KeyKind.DIGIT), KeyDef(".", ".", KeyKind.DIGIT), KeyDef("=", "=", KeyKind.EQ)),
)

private fun sciKeys(inv: Boolean, deg: Boolean, s: Strings): List<KeyDef> = listOf(
    KeyDef("inv", "Inv", KeyKind.SCI),
    KeyDef("sin", if (inv) "sin⁻¹" else "sin", KeyKind.SCI),
    KeyDef("cos", if (inv) "cos⁻¹" else "cos", KeyKind.SCI),
    KeyDef("tan", if (inv) "tan⁻¹" else "tan", KeyKind.SCI),
    KeyDef("deg", if (deg) s.deg else s.rad, KeyKind.SCI),
    KeyDef("π", "π", KeyKind.SCI),
    KeyDef("e", "e", KeyKind.SCI),
    KeyDef("ln", if (inv) "eˣ" else "ln", KeyKind.SCI),
    KeyDef("log", if (inv) "10ˣ" else "log", KeyKind.SCI),
    KeyDef("√", "√", KeyKind.SCI),
    KeyDef("^", "xʸ", KeyKind.SCI),
    KeyDef("!", "x!", KeyKind.SCI),
    KeyDef("x²", if (inv) "x³" else "x²", KeyKind.SCI),
    KeyDef("x⁻¹", "x⁻¹", KeyKind.SCI),
    KeyDef("abs", "|x|", KeyKind.SCI),
    KeyDef("()", "( )", KeyKind.SCI),
    KeyDef("DEL", "⌫", KeyKind.DEL),
)

@Composable
private fun KeyRow(keys: List<KeyDef>, gap: Dp, state: CalcState, s: Strings, onKey: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(gap)) {
        for (k in keys) {
            val mod = Modifier.weight(1f).fillMaxHeight()
            when (k.id) {
                "DEL" -> CalcKey(k.label, k.kind, mod, desc = s.backspace, icon = { BackspaceIcon(it) },
                    onLong = { onKey("AC") }) { onKey("DEL") }
                "inv" -> CalcKey(k.label, k.kind, mod, active = state.inv, small = true) { onKey(k.id) }
                else -> CalcKey(k.label, k.kind, mod, small = k.kind == KeyKind.SCI) { onKey(k.id) }
            }
        }
    }
}

@Composable
private fun MainPad(state: CalcState, s: Strings, gap: Dp, onKey: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        for (row in mainRows) KeyRow(row, gap, state, s, onKey, Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
fun CalcScreen(
    state: CalcState,
    settings: Settings,
    s: Strings,
    onKey: (String) -> Unit,
    onToggleDeg: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    var sciOpen by rememberSaveable { mutableStateOf(false) }
    val gap = 10.dp
    val sci = sciKeys(state.inv, state.deg, s)
    val handle: (String) -> Unit = { id -> if (id == "deg") onToggleDeg() else onKey(id) }

    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
        if (maxWidth > maxHeight) {
            // Альбомная: инженерные клавиши всегда слева от основных.
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                Display(state, s, true, false, {}, onToggleDeg, onHistory, onSettings, Modifier.fillMaxWidth().weight(0.9f))
                Row(Modifier.fillMaxWidth().weight(2.4f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    Column(Modifier.weight(3f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(gap)) {
                        for (row in sci.chunked(3)) KeyRow(row, gap, state, s, handle, Modifier.weight(1f).fillMaxWidth())
                    }
                    MainPad(state, s, gap, onKey, Modifier.weight(4f).fillMaxHeight())
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                Display(state, s, sciOpen, true, { sciOpen = !sciOpen }, onToggleDeg, onHistory, onSettings,
                    Modifier.fillMaxWidth().weight(1f))
                AnimatedVisibility(sciOpen, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Column(Modifier.fillMaxWidth().height(minOf(200.dp, this@BoxWithConstraints.maxHeight * 0.29f)), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in sci.chunked(5)) KeyRow(row, 8.dp, state, s, handle, Modifier.weight(1f).fillMaxWidth())
                    }
                }
                // Reserve room for the display when the scientific panel is expanded.
                val side = (this@BoxWithConstraints.maxWidth - gap * 3) / 4
                val available = this@BoxWithConstraints.maxHeight
                val padHeight = minOf(side * 5 + gap * 4, available * if (sciOpen) 0.43f else 0.64f)
                MainPad(state, s, gap, onKey, Modifier.fillMaxWidth().height(padHeight))
            }
        }
    }
}
