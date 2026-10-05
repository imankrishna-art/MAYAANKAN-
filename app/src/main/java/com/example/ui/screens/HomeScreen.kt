package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AssistantState
import com.example.ui.components.AiCore
import com.example.ui.components.LiveWaveform
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StatusListening
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusSpeaking
import com.example.ui.theme.StatusThinking
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    assistantState: AssistantState,
    voiceAmplitude: Float,
    statusText: String,
    onMicClick: () -> Unit,
    onStopSpeakingClick: () -> Unit,
    onNavigateToConversation: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToVision: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenQuickActions: () -> Unit,
    isMicPermitted: Boolean,
    isCompanionActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenQuickActions,
                modifier = Modifier.testTag("quick_actions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "Quick Actions",
                    tint = NeonCyan
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MAYA",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "AI ASSISTANT",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Assistant state indicator badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (assistantState) {
                                    AssistantState.SPEAKING -> StatusSpeaking
                                    AssistantState.LISTENING -> StatusListening
                                    AssistantState.THINKING -> StatusThinking
                                    AssistantState.PAUSED -> Color(0xFFFF9100)
                                    else -> StatusOnline
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (assistantState) {
                            AssistantState.SPEAKING -> "Speaking..."
                            AssistantState.LISTENING -> "Listening..."
                            AssistantState.THINKING -> "Thinking..."
                            AssistantState.PAUSED -> "Paused"
                            AssistantState.ERROR -> "Service Alert"
                            else -> "Ready"
                        },
                        color = when (assistantState) {
                            AssistantState.SPEAKING -> NeonPink
                            AssistantState.LISTENING -> NeonCyan
                            AssistantState.THINKING -> NeonViolet
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.testTag("settings_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary
                )
            }
        }

        // --- Center AI Core & Voice Visualization ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            AiCore(
                assistantState = assistantState,
                voiceAmplitude = voiceAmplitude,
                size = 260.dp,
                modifier = Modifier.clickable {
                    if (assistantState == AssistantState.SPEAKING) {
                        onStopSpeakingClick()
                    } else {
                        onMicClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle / Spoken text display
            Text(
                text = if (statusText.isNotBlank()) statusText else when (assistantState) {
                    AssistantState.LISTENING -> "Listening to your voice..."
                    AssistantState.THINKING -> "Thinking..."
                    AssistantState.SPEAKING -> "Speaking..."
                    AssistantState.PAUSED -> "Maya is currently paused"
                    else -> "How can I help you today?"
                },
                color = when (assistantState) {
                    AssistantState.SPEAKING -> Color.White
                    AssistantState.LISTENING -> NeonCyan
                    AssistantState.THINKING -> NeonViolet
                    else -> TextPrimary
                },
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable { onNavigateToConversation() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Audio Waveform
            LiveWaveform(
                assistantState = assistantState,
                voiceAmplitude = voiceAmplitude,
                width = 220.dp,
                height = 28.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Glowing Microphone / Stop Button
            if (assistantState == AssistantState.SPEAKING) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFF1744), Color(0xFFFF007F)))
                        )
                        .clickable { onStopSpeakingClick() }
                        .testTag("stop_speaking_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speaking",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to stop",
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(12.dp, CircleShape, spotColor = NeonCyan)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF003D4D),
                                    Color(0xFF021722)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            if (assistantState == AssistantState.LISTENING) NeonCyan else Color(0x5500E5FF),
                            CircleShape
                        )
                        .clickable { onMicClick() }
                        .testTag("mic_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (assistantState == AssistantState.LISTENING) Icons.Default.Mic else Icons.Default.Mic,
                        contentDescription = "Tap to speak",
                        tint = if (assistantState == AssistantState.LISTENING) NeonCyan else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (assistantState == AssistantState.LISTENING) "Listening..." else "Tap to speak",
                    color = if (assistantState == AssistantState.LISTENING) NeonCyan else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- Bottom Controls & Status ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            // Quick Action Row (Voice, Security, Vision, History, Settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC0C1220))
                    .border(1.dp, Color(0x2200E5FF), RoundedCornerShape(20.dp))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionItem(
                    icon = Icons.Default.Mic,
                    label = "Voice",
                    selected = assistantState == AssistantState.LISTENING,
                    onClick = onMicClick
                )
                QuickActionItem(
                    icon = Icons.Default.Security,
                    label = "Security",
                    selected = false,
                    onClick = onNavigateToSecurity
                )
                QuickActionItem(
                    icon = Icons.Default.Visibility,
                    label = "Vision",
                    selected = false,
                    onClick = onNavigateToVision
                )
                QuickActionItem(
                    icon = Icons.Default.History,
                    label = "History",
                    selected = false,
                    onClick = onNavigateToHistory
                )
                QuickActionItem(
                    icon = Icons.Default.Chat,
                    label = "Chat",
                    selected = false,
                    onClick = onNavigateToConversation
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Indicators Bar (MIC, CAMERA, SCREEN)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    label = if (assistantState == AssistantState.LISTENING) "MIC ON" else "MIC READY",
                    isActive = assistantState == AssistantState.LISTENING
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatusPill(label = "CAMERA OFF", isActive = false)
                Spacer(modifier = Modifier.width(10.dp))
                StatusPill(label = "SCREEN OFF", isActive = false)
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("action_${label.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) Color(0x3300E5FF) else Color(0x1A1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) NeonCyan else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) NeonCyan else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusPill(label: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF080D18))
            .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (isActive) NeonCyan else Color(0xFF64748B))
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = if (isActive) NeonCyan else Color(0xFF94A3B8),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
