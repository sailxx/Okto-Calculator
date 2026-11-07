package com.sailxx.oktocalc

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun NavigationIcon(history: Boolean, label: String, onClick: () -> Unit) {
    val c = C
    Box(Modifier.size(48.dp).clip(CircleShape)
        .semantics { contentDescription = label }.clickable(role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(22.dp)) {
            val u = size.width / 24f
            val stroke = 1.7f * u
            fun line(x: Float, y: Float, x2: Float, y2: Float) = drawLine(c.muted, Offset(x*u,y*u), Offset(x2*u,y2*u), stroke, StrokeCap.Round)
            if (history) {
                drawArc(c.muted, 205f, 300f, false, Offset(4*u,4*u), Size(16*u,16*u), style = Stroke(stroke, cap = StrokeCap.Round))
                line(4f,4f,4f,9f); line(4f,9f,9f,9f)
                line(12f,8f,12f,12f); line(12f,12f,15f,14f)
            } else {
                listOf(7f to 8f,12f to 16f,17f to 10f).forEach { (y,x) ->
                    line(4f,y,20f,y)
                    drawCircle(c.bg,2*u,Offset(x*u,y*u))
                    drawCircle(c.muted,2*u,Offset(x*u,y*u),style=Stroke(stroke))
                }
            }
        }
    }
}
