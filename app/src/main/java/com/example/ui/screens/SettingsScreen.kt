package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.gemini.GeminiClient
import com.example.ai.gemini.GeminiResult
import com.example.assistant.AssistantRoleHelper
import com.example.core.storage.PreferencesManager
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    geminiClient: GeminiClient,
    onTestSpeak: (String) -> Unit,
    onNavigateToSecurity: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf(prefs.geminiApiKey.value) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var connectionTestState by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    var pitch by remember { mutableFloatStateOf(prefs.voicePitch.value) }
    var speed by remember { mutableFloatStateOf(prefs.voiceSpeed.value) }
    var selectedLanguage by remember { mutableStateOf(prefs.language.value) }

    val isDefaultAssistant = remember { AssistantRoleHelper.isDefaultAssistant(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Default Assistant Card ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    highlightCyan = isDefaultAssistant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3300E5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assistant,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Android Voice Assistant", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isDefaultAssistant) "Maya is your default assistant" else "Set Maya as your default device assistant",
                                    color = if (isDefaultAssistant) NeonGreen else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { AssistantRoleHelper.openDefaultAssistantSettings(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDefaultAssistant) Color(0x331E293B) else NeonCyan
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isDefaultAssistant) "Assistant Settings" else "Set Maya as Default Assistant",
                                color = if (isDefaultAssistant) NeonCyan else Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // --- Voice Settings ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Voice Settings", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Pitch: ${String.format("%.1f", pitch)}", color = TextSecondary, fontSize = 12.sp)
                        Slider(
                            value = pitch,
                            onValueChange = {
                                pitch = it
                                prefs.setVoicePitch(it)
                            },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            )
                        )

                        Text("Speech Rate: ${String.format("%.1f", speed)}", color = TextSecondary, fontSize = 12.sp)
                        Slider(
                            value = speed,
                            onValueChange = {
                                speed = it
                                prefs.setVoiceSpeed(it)
                            },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonPink,
                                activeTrackColor = NeonPink,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { onTestSpeak("হাই, আমি Maya। বলো না, কী চলছে?") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Voice Speech", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- Language Settings ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = NeonViolet)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Language", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LanguageOptionChip("Auto", "auto", selectedLanguage == "auto") {
                                selectedLanguage = "auto"
                                prefs.setLanguage("auto")
                            }
                            LanguageOptionChip("বাংলা", "bn", selectedLanguage == "bn") {
                                selectedLanguage = "bn"
                                prefs.setLanguage("bn")
                            }
                            LanguageOptionChip("English", "en", selectedLanguage == "en") {
                                selectedLanguage = "en"
                                prefs.setLanguage("en")
                            }
                            LanguageOptionChip("हिंदी", "hi", selectedLanguage == "hi") {
                                selectedLanguage = "hi"
                                prefs.setLanguage("hi")
                            }
                        }
                    }
                }
            }

            // --- Gemini AI Settings ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), highlightCyan = true) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Gemini AI Intelligence", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("API Key (Private & Secure)", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                prefs.setGeminiApiKey(it)
                            },
                            placeholder = { Text("Enter your Gemini API Key", color = TextMuted, fontSize = 13.sp) },
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gemini_api_key_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Current Model: gemini-3.5-flash", color = TextMuted, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isTestingConnection = true
                                        connectionTestState = "Testing..."
                                        when (val result = geminiClient.testConnection()) {
                                            is GeminiResult.Success -> {
                                                connectionTestState = "Connected Successfully!"
                                            }
                                            is GeminiResult.Error -> {
                                                connectionTestState = "Error: ${result.message}"
                                            }
                                        }
                                        isTestingConnection = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Test Connection", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Button(
                                onClick = {
                                    apiKeyInput = ""
                                    prefs.setGeminiApiKey("")
                                    connectionTestState = "Key removed"
                                    Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF1744)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Remove", color = Color(0xFFFF5252), fontSize = 12.sp)
                            }
                        }

                        if (connectionTestState != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = connectionTestState ?: "",
                                color = if (connectionTestState?.contains("Connected") == true) NeonGreen else Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- Companion Mode Controls ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = NeonPink)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Companion Mode", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Proactive Assistance", color = Color.White, fontSize = 13.sp)
                                Text("Let Maya offer contextual assistance when active", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = prefs.proactiveAi.value,
                                onCheckedChange = { prefs.setProactiveAi(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = Color(0xFF004D5A)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pause Maya", color = Color.White, fontSize = 13.sp)
                                Text("Temporarily mute voice interactions", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = prefs.companionPaused.value,
                                onCheckedChange = { prefs.setCompanionPaused(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonPink,
                                    checkedTrackColor = Color(0xFF4A0033)
                                )
                            )
                        }
                    }
                }
            }

            // --- Security & Privacy Shortcut ---
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSecurity() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Security & Privacy", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Biometric, Face Security, Permissions", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            // --- About Maya ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MAYA FREE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("Your Voice. Your Security. Your AI.", color = NeonCyan, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Version 1.0.0", color = TextMuted, fontSize = 11.sp)
                        Text("ANKAN CORPORATION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Maya Free is an AI assistant and companion. Gemini-powered intelligence operates securely with least-privilege permissions.",
                            color = TextMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LanguageOptionChip(
    label: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x3300E5FF) else Color(0x1A1E293B))
            .border(
                1.dp,
                if (isSelected) NeonCyan else Color(0x22FFFFFF),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) NeonCyan else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
