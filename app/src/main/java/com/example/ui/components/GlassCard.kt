package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonCyan

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 1.dp,
    highlightCyan: Boolean = false,
    content: @Composable () -> Unit
) {
    val finalBorder = if (highlightCyan) {
        BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.6f), Color(0x337C4DFF))))
    } else {
        BorderStroke(borderWidth, borderColor)
    }

    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = Color(0xCC0B101D),
        border = finalBorder
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x221E293B),
                        Color(0x000F172A)
                    )
                )
            )
        ) {
            content()
        }
    }
}
