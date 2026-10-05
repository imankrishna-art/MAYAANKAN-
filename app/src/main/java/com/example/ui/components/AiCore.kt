package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AssistantState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RainbowColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AiCore(
    assistantState: AssistantState,
    voiceAmplitude: Float = 0f,
    size: Dp = 270.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_core_anim")

    // Dynamic rotation speeds based on state
    val rotationSpeed = when (assistantState) {
        AssistantState.THINKING -> 3000
        AssistantState.SPEAKING -> 4500
        AssistantState.LISTENING -> 5500
        else -> 10000
    }

    val rotationCW by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cw_rotation"
    )

    val rotationCCW by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (rotationSpeed * 1.4).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ccw_rotation"
    )

    // Breathing pulse for idle & speaking
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_breath"
    )

    val ampScale = (voiceAmplitude * 0.22f)
    val effectiveScale = when (assistantState) {
        AssistantState.SPEAKING -> breathScale + ampScale
        AssistantState.LISTENING -> 1.0f + (voiceAmplitude * 0.15f)
        AssistantState.THINKING -> breathScale * 0.98f
        else -> breathScale
    }

    val animatedEffectiveScale by animateFloatAsState(
        targetValue = effectiveScale,
        animationSpec = tween(durationMillis = 150),
        label = "scale_smooth"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(animatedEffectiveScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer concentric rings and orbital particle Canvas
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // Radial background glow aura
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    when (assistantState) {
                        AssistantState.SPEAKING -> NeonPink.copy(alpha = 0.35f + voiceAmplitude * 0.25f)
                        AssistantState.LISTENING -> NeonCyan.copy(alpha = 0.38f + voiceAmplitude * 0.25f)
                        AssistantState.THINKING -> NeonViolet.copy(alpha = 0.32f)
                        AssistantState.ERROR -> Color(0xFFFF1744).copy(alpha = 0.3f)
                        else -> NeonCyan.copy(alpha = 0.18f)
                    },
                    Color.Transparent
                ),
                center = center,
                radius = radius * 0.95f
            )
            drawCircle(brush = glowBrush, radius = radius * 0.95f, center = center)

            // Outer Rainbow Ring (Clockwise)
            rotate(rotationCW, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(RainbowColors, center = center),
                    radius = radius * 0.88f,
                    center = center,
                    style = Stroke(width = 3.2.dp.toPx())
                )
            }

            // Intermediate Segmented Arc Ring (Counter-Clockwise)
            rotate(rotationCCW, pivot = center) {
                val intermediateRadius = radius * 0.74f
                val strokeWidth = 2.4.dp.toPx()
                val cyanVioletBrush = Brush.sweepGradient(
                    listOf(NeonCyan, NeonPurple, NeonMagenta, NeonCyan),
                    center = center
                )
                drawCircle(
                    brush = cyanVioletBrush,
                    radius = intermediateRadius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Segmented dash dots on intermediate orbit
                for (i in 0 until 6) {
                    val angleRad = Math.toRadians((i * 60.0))
                    val dotCenter = Offset(
                        center.x + (intermediateRadius * cos(angleRad)).toFloat(),
                        center.y + (intermediateRadius * sin(angleRad)).toFloat()
                    )
                    drawCircle(
                        color = if (i % 2 == 0) NeonCyan else NeonPink,
                        radius = 2.5.dp.toPx(),
                        center = dotCenter
                    )
                }
            }

            // Inner Ring with soft glow
            rotate(rotationCW * 0.7f, pivot = center) {
                val innerRadius = radius * 0.58f
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(NeonCyan, NeonViolet, NeonPink, NeonCyan),
                        center = center
                    ),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 2.0.dp.toPx())
                )
            }

            // Orbital particle satellites revolving around outer orbit
            val particleAngle1 = Math.toRadians((rotationCW * 1.2).toDouble())
            val particlePos1 = Offset(
                center.x + (radius * 0.88f * cos(particleAngle1)).toFloat(),
                center.y + (radius * 0.88f * sin(particleAngle1)).toFloat()
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = particlePos1
            )

            val particleAngle2 = Math.toRadians((rotationCCW * 0.9 + 180).toDouble())
            val particlePos2 = Offset(
                center.x + (radius * 0.74f * cos(particleAngle2)).toFloat(),
                center.y + (radius * 0.74f * sin(particleAngle2)).toFloat()
            )
            drawCircle(
                color = NeonCyan,
                radius = 3.0.dp.toPx(),
                center = particlePos2
            )
        }

        // Central Luminous Core
        val centerCoreSize = size * 0.44f
        Box(
            modifier = Modifier
                .size(centerCoreSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF14192A),
                            Color(0xFF04060C)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MAYA",
                color = when (assistantState) {
                    AssistantState.SPEAKING -> NeonPink
                    AssistantState.LISTENING -> NeonCyan
                    AssistantState.THINKING -> NeonViolet
                    else -> Color.White
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
