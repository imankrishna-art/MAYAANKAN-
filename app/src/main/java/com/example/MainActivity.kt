package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.core.model.AssistantState
import com.example.security.BiometricHelper
import com.example.ui.MayaViewModel
import com.example.ui.ScreenState
import com.example.ui.components.RainbowEdgeGlow
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.FirstLaunchDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuickActionsDialog
import com.example.ui.screens.SecurityScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    private val viewModel: MayaViewModel by viewModels()

    private val requestRecordAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onMicClicked()
        } else {
            Toast.makeText(this, "Microphone permission required for voice commands.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.navigateTo(ScreenState.VISION)
        } else {
            Toast.makeText(this, "Camera permission needed for Vision Mode.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle System Voice Assistant Trigger Intent
        handleAssistantIntent(intent)

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val assistantState by viewModel.assistantState.collectAsState()
                val voiceAmplitude by viewModel.voiceManager.voiceAmplitude.collectAsState()
                val statusText by viewModel.statusText.collectAsState()
                val isQuickActionsOpen by viewModel.isQuickActionsOpen.collectAsState()
                val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
                val messages by viewModel.conversationMessages.collectAsState()

                val isMicPermitted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                val isCameraPermitted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                val isLocationPermitted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AmoledBlack)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Main Navigation Flow
                        when (currentScreen) {
                            ScreenState.HOME -> {
                                HomeScreen(
                                    assistantState = assistantState,
                                    voiceAmplitude = voiceAmplitude,
                                    statusText = statusText,
                                    onMicClick = {
                                        if (isMicPermitted) {
                                            viewModel.onMicClicked()
                                        } else {
                                            requestRecordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    onStopSpeakingClick = { viewModel.stopSpeaking() },
                                    onNavigateToConversation = { viewModel.navigateTo(ScreenState.CONVERSATION) },
                                    onNavigateToSecurity = { viewModel.navigateTo(ScreenState.SECURITY) },
                                    onNavigateToVision = {
                                        if (isCameraPermitted) {
                                            viewModel.navigateTo(ScreenState.VISION)
                                        } else {
                                            requestCameraPermission.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    onNavigateToHistory = { viewModel.navigateTo(ScreenState.CONVERSATION) },
                                    onNavigateToSettings = { viewModel.navigateTo(ScreenState.SETTINGS) },
                                    onOpenQuickActions = { viewModel.openQuickActions() },
                                    isMicPermitted = isMicPermitted,
                                    isCompanionActive = !viewModel.prefs.companionPaused.value
                                )
                            }
                            ScreenState.CONVERSATION -> {
                                ConversationScreen(
                                    messages = messages,
                                    onSendMessage = { prompt -> viewModel.processUserInput(prompt, isVoice = false) },
                                    onSpeakMessage = { text -> viewModel.voiceManager.speak(text) },
                                    onMicClick = {
                                        if (isMicPermitted) {
                                            viewModel.onMicClicked()
                                        } else {
                                            requestRecordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    onClearHistory = { viewModel.clearAllConversations() },
                                    onBack = { viewModel.navigateTo(ScreenState.HOME) }
                                )
                            }
                            ScreenState.SECURITY -> {
                                SecurityScreen(
                                    isBiometricEnabled = viewModel.prefs.biometricEnabled.value,
                                    onToggleBiometric = { viewModel.prefs.setBiometricEnabled(it) },
                                    isAppLockEnabled = viewModel.prefs.appLockEnabled.value,
                                    onToggleAppLock = { viewModel.prefs.setAppLockEnabled(it) },
                                    isFaceSecurityEnabled = viewModel.prefs.faceSecurityEnabled.value,
                                    onToggleFaceSecurity = { viewModel.prefs.setFaceSecurityEnabled(it) },
                                    isMicPermitted = isMicPermitted,
                                    isCameraPermitted = isCameraPermitted,
                                    isLocationPermitted = isLocationPermitted,
                                    onClearAllData = { viewModel.clearAllStoredData() },
                                    onBack = { viewModel.navigateTo(ScreenState.HOME) }
                                )
                            }
                            ScreenState.VISION -> {
                                VisionScreen(
                                    geminiClient = viewModel.geminiClient,
                                    onSpeakText = { text -> viewModel.voiceManager.speak(text) },
                                    isCameraPermitted = isCameraPermitted,
                                    onRequestCameraPermission = {
                                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                                    },
                                    onBack = { viewModel.navigateTo(ScreenState.HOME) }
                                )
                            }
                            ScreenState.WEATHER -> {
                                WeatherScreen(
                                    onBack = { viewModel.navigateTo(ScreenState.HOME) }
                                )
                            }
                            ScreenState.SETTINGS -> {
                                SettingsScreen(
                                    prefs = viewModel.prefs,
                                    geminiClient = viewModel.geminiClient,
                                    onTestSpeak = { text -> viewModel.voiceManager.speak(text) },
                                    onNavigateToSecurity = { viewModel.navigateTo(ScreenState.SECURITY) },
                                    onBack = { viewModel.navigateTo(ScreenState.HOME) }
                                )
                            }
                            ScreenState.APP_LOCK -> {
                                AppLockScreen(
                                    isAppLockEnabled = viewModel.prefs.appLockEnabled.value,
                                    onToggleAppLock = { viewModel.prefs.setAppLockEnabled(it) },
                                    onTriggerBiometric = {
                                        BiometricHelper.authenticate(
                                            activity = this@MainActivity,
                                            title = "Unlock Maya",
                                            subtitle = "Confirm identity to access protected features",
                                            onSuccess = { viewModel.unlockApp() },
                                            onError = { err -> Toast.makeText(this@MainActivity, err, Toast.LENGTH_SHORT).show() }
                                        )
                                    },
                                    onBack = { viewModel.navigateTo(ScreenState.HOME) }
                                )
                            }
                        }

                        // Living Full-Spectrum Rainbow Edge Glow Overlay
                        RainbowEdgeGlow(assistantState = assistantState)

                        // Quick Actions Dialog Overlay
                        if (isQuickActionsOpen) {
                            QuickActionsDialog(
                                onDismiss = { viewModel.closeQuickActions() },
                                onVisionClick = {
                                    viewModel.closeQuickActions()
                                    if (isCameraPermitted) {
                                        viewModel.navigateTo(ScreenState.VISION)
                                    } else {
                                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                onWeatherClick = {
                                    viewModel.closeQuickActions()
                                    viewModel.navigateTo(ScreenState.WEATHER)
                                },
                                onContactsClick = {
                                    viewModel.closeQuickActions()
                                    try {
                                        val contactsIntent = Intent(Intent.ACTION_VIEW, android.provider.ContactsContract.Contacts.CONTENT_URI)
                                        startActivity(contactsIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(this@MainActivity, "Contacts app unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onOpenAppsClick = {
                                    viewModel.closeQuickActions()
                                    try {
                                        val settingsIntent = Intent(android.provider.Settings.ACTION_SETTINGS)
                                        startActivity(settingsIntent)
                                    } catch (ignored: Exception) {}
                                }
                            )
                        }

                        // First Launch Greeting & Explanation Dialog
                        if (isFirstLaunch) {
                            FirstLaunchDialog(
                                onDismiss = { viewModel.dismissFirstLaunch() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAssistantIntent(intent)
    }

    private fun handleAssistantIntent(intent: Intent?) {
        if (intent == null) return
        val isVoiceTrigger = intent.getBooleanExtra("EXTRA_TRIGGER_VOICE", false)
        val isAssist = intent.action == Intent.ACTION_ASSIST || intent.action == Intent.ACTION_VOICE_COMMAND
        if (isVoiceTrigger || isAssist) {
            val isMicPermitted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (isMicPermitted) {
                viewModel.onMicClicked()
            }
        }
    }
}
