package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.core.model.AssistantState
import com.example.ui.theme.RainbowColors

@Composable
fun RainbowEdgeGlow(
    assistantState: AssistantState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edge_rainbow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "edge_rotation"
    )

    val targetAlpha = when (assistantState) {
        AssistantState.SPEAKING -> 0.95f
        AssistantState.LISTENING -> 0.75f
        AssistantState.THINKING -> 0.55f
        AssistantState.AUTHENTICATING -> 0.85f
        AssistantState.ERROR -> 0.6f
        else -> 0.0f
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 600),
        label = "edge_alpha"
    )

    if (animatedAlpha > 0.01f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val strokeWidthOuter = 7.dp.toPx()
            val strokeWidthInner = 2.5.dp.toPx()
            val cornerRadius = 32.dp.toPx()

            val center = Offset(size.width / 2f, size.height / 2f)

            // Dynamic rainbow sweep brush centered on screen
            val rainbowBrush = Brush.sweepGradient(
                colors = RainbowColors,
                center = center
            )

            // Outer soft bloom glow
            drawRoundRect(
                brush = rainbowBrush,
                topLeft = Offset(strokeWidthOuter / 2f, strokeWidthOuter / 2f),
                size = Size(
                    size.width - strokeWidthOuter,
                    size.height - strokeWidthOuter
                ),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidthOuter),
                alpha = animatedAlpha * 0.45f
            )

            // Inner sharp laser perimeter
            drawRoundRect(
                brush = rainbowBrush,
                topLeft = Offset(strokeWidthInner, strokeWidthInner),
                size = Size(
                    size.width - strokeWidthInner * 2f,
                    size.height - strokeWidthInner * 2f
                ),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidthInner),
                alpha = animatedAlpha * 0.85f
            )
        }
    }
}
