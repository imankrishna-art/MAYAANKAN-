package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.AssistantState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import kotlin.math.sin

@Composable
fun LiveWaveform(
    assistantState: AssistantState,
    voiceAmplitude: Float = 0f,
    width: Dp = 220.dp,
    height: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.width(width).height(height)) {
        val barCount = 28
        val spacing = size.width / (barCount * 1.5f)
        val barWidth = spacing * 0.55f
        val centerY = size.height / 2f

        val waveformBrush = Brush.horizontalGradient(
            listOf(NeonCyan, NeonViolet, NeonPink)
        )

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val baseSine = sin(progress * 3.14f * 2f + phase)

            val dynamicHeight = when (assistantState) {
                AssistantState.SPEAKING -> {
                    val amp = (voiceAmplitude * 0.8f + 0.2f)
                    (size.height * 0.15f) + (size.height * 0.75f * ((baseSine + 1f) / 2f) * amp)
                }
                AssistantState.LISTENING -> {
                    val amp = (voiceAmplitude * 0.9f + 0.15f)
                    (size.height * 0.12f) + (size.height * 0.7f * ((baseSine + 1f) / 2f) * amp)
                }
                AssistantState.THINKING -> {
                    (size.height * 0.15f) + (size.height * 0.35f * ((sin(phase * 2f + i) + 1f) / 2f))
                }
                else -> {
                    // Gentle subtle resting wave
                    (size.height * 0.12f) + (size.height * 0.2f * ((baseSine + 1f) / 2f))
                }
            }

            val x = i * (barWidth + spacing)
            val top = centerY - dynamicHeight / 2f

            drawRoundRect(
                brush = waveformBrush,
                topLeft = Offset(x, top),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
