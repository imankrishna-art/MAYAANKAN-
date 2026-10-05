package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Pure AMOLED Black
val AmoledBlack = Color(0xFF000000)
val DarkBackground = Color(0xFF05070E)
val GlassSurface = Color(0xCC0D1322)
val GlassSurfaceVariant = Color(0x99141D33)
val GlassBorder = Color(0x4000E5FF)
val GlassBorderSubtle = Color(0x22FFFFFF)

// Neon & Full Spectrum Palette
val NeonCyan = Color(0xFF00E5FF)
val NeonAzure = Color(0xFF00B0FF)
val NeonBlue = Color(0xFF2979FF)
val NeonElectricBlue = Color(0xFF0066FF)
val NeonViolet = Color(0xFF7C4DFF)
val NeonPurple = Color(0xFF9D00FF)
val NeonMagenta = Color(0xFFE040FB)
val NeonPink = Color(0xFFFF007F)
val NeonRose = Color(0xFFFF4081)
val NeonRed = Color(0xFFFF1744)
val NeonOrange = Color(0xFFFF9100)
val NeonYellow = Color(0xFFFFEA00)
val NeonGreen = Color(0xFF00E676)

// Text Colors
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Status Colors
val StatusOnline = Color(0xFF00E5FF)
val StatusListening = Color(0xFF00E5FF)
val StatusSpeaking = Color(0xFFFF007F)
val StatusThinking = Color(0xFF7C4DFF)
val StatusWarning = Color(0xFFFF9100)
val StatusError = Color(0xFFFF1744)

// Rainbow Spectrum Gradients
val RainbowColors = listOf(
    NeonCyan,
    NeonAzure,
    NeonBlue,
    NeonViolet,
    NeonPurple,
    NeonMagenta,
    NeonPink,
    NeonRed,
    NeonOrange,
    NeonYellow,
    NeonGreen,
    NeonCyan
)

val MayaRainbowBrush = Brush.linearGradient(RainbowColors)
val MayaCyanPurpleBrush = Brush.linearGradient(listOf(NeonCyan, NeonBlue, NeonViolet, NeonPink))
val GlassGradient = Brush.verticalGradient(
    listOf(
        Color(0xCC131A2E),
        Color(0xEE090D17)
    )
)
