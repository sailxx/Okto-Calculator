package com.sailxx.oktocalc

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class KeyKind { DIGIT, OP, SCI, EQ, CLEAR, DEL }

private fun colorsFor(kind: KeyKind, c: Palette): Pair<Color, Color> = when (kind) {
    KeyKind.DIGIT, KeyKind.SCI, KeyKind.DEL -> c.key to c.keyInk
    KeyKind.OP -> c.opKey to c.opInk
    KeyKind.EQ -> c.primary to c.onPrimary
    KeyKind.CLEAR -> c.key to c.muted
}

/** Terminal keys retain native focus, keyboard activation and long-press semantics. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalcKey(
    label: String,
    kind: KeyKind,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    small: Boolean = false,
    desc: String = label,
    onLong: (() -> Unit)? = null,
    icon: (@Composable (Color) -> Unit)? = null,
    onTap: () -> Unit,
) {
    val c = C
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if (pressed) .96f else 1f, tween(150), label = "keyPress")
    val (bg0, fg0) = colorsFor(kind, c)
    val bg = if (active) c.primary else bg0
    val fg = if (active) c.onPrimary else fg0
    val shape = CircleShape
    val description = when (label) {
        "÷" -> "Divide"; "×" -> "Multiply"; "−" -> "Subtract"; "+" -> "Add"
        "=" -> "Equals"; "±" -> "Change sign"; "AC" -> "Clear all"; "." -> "Decimal point"
        else -> desc
    }
    // Keep keys circular even when their grid cells are rectangular.
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
    val diameter = minOf(maxWidth, maxHeight)
    Box(
        Modifier.size(diameter)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape).background(bg)
            .semantics { contentDescription = description }
            .combinedClickable(interactionSource = interaction, indication = androidx.compose.foundation.LocalIndication.current,
                role = Role.Button, onLongClick = onLong, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) icon(fg) else Text(label, color = fg,
            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
            fontWeight = FontWeight.Normal, fontSize = if (small) 14.sp else 30.sp, maxLines = 1)
    }
    }
}

@Composable
fun BackspaceIcon(color: Color) {
    Canvas(Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val st = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val p = Path().apply {
            moveTo(w * 0.34f, h * 0.2f); lineTo(w * 0.9f, h * 0.2f); lineTo(w * 0.9f, h * 0.8f)
            lineTo(w * 0.34f, h * 0.8f); lineTo(w * 0.08f, h * 0.5f); close()
        }
        drawPath(p, color, style = st)
        drawLine(color, Offset(w * 0.48f, h * 0.38f), Offset(w * 0.72f, h * 0.62f), st.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.72f, h * 0.38f), Offset(w * 0.48f, h * 0.62f), st.width, StrokeCap.Round)
    }
}
