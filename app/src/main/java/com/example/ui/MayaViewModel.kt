package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.gemini.GeminiClient
import com.example.ai.gemini.GeminiResult
import com.example.commands.LocalCommandEngine
import com.example.core.model.ActionType
import com.example.core.model.AssistantState
import com.example.core.storage.ConversationEntity
import com.example.core.storage.ConversationRepository
import com.example.core.storage.MayaDatabase
import com.example.core.storage.PreferencesManager
import com.example.voice.MayaVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
    HOME,
    CONVERSATION,
    SECURITY,
    VISION,
    WEATHER,
    SETTINGS,
    APP_LOCK
}

class MayaViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = PreferencesManager(application)
    private val db = MayaDatabase.getInstance(application)
    val repository = ConversationRepository(db.conversationDao(), db.memoryDao())

    val localCommandEngine = LocalCommandEngine(application)

    val geminiClient = GeminiClient(
        apiKeyProvider = { prefs.geminiApiKey.value },
        modelProvider = { prefs.geminiModel.value }
    )

    private val _currentScreen = MutableStateFlow(ScreenState.HOME)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _statusText = MutableStateFlow("")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isQuickActionsOpen = MutableStateFlow(false)
    val isQuickActionsOpen: StateFlow<Boolean> = _isQuickActionsOpen.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(prefs.isFirstLaunch)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(!prefs.appLockEnabled.value)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    val conversationMessages: StateFlow<List<ConversationEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceManager: MayaVoiceManager = MayaVoiceManager(
        context = application,
        onSpeechRecognized = { recognizedText ->
            processUserInput(recognizedText, isVoice = true)
        },
        onStateChange = { newState ->
            _assistantState.value = newState
            if (newState == AssistantState.IDLE && _statusText.value == "Listening...") {
                _statusText.value = ""
            }
        }
    )

    init {
        voiceManager.pitch = prefs.voicePitch.value
        voiceManager.speechRate = prefs.voiceSpeed.value
        voiceManager.languageCode = prefs.language.value

        if (prefs.isFirstLaunch) {
            // First time launch greeting
            viewModelScope.launch {
                repository.addMessage(
                    sender = "MAYA",
                    text = "হাই, আমি Maya। বলো না, কী চলছে? আমি আছি, Boss।",
                    isVoice = true
                )
            }
        }
    }

    fun navigateTo(screen: ScreenState) {
        if (prefs.appLockEnabled.value && !_isAppUnlocked.value &&
            (screen == ScreenState.CONVERSATION || screen == ScreenState.SECURITY || screen == ScreenState.SETTINGS)
        ) {
            _currentScreen.value = ScreenState.APP_LOCK
            return
        }
        _currentScreen.value = screen
    }

    fun unlockApp() {
        _isAppUnlocked.value = true
        _currentScreen.value = ScreenState.HOME
    }

    fun dismissFirstLaunch() {
        prefs.isFirstLaunch = false
        _isFirstLaunch.value = false
        voiceManager.speak("হাই, আমি Maya। বলো না, কী চলছে? আমি আছি, Boss।")
    }

    fun openQuickActions() {
        _isQuickActionsOpen.value = true
    }

    fun closeQuickActions() {
        _isQuickActionsOpen.value = false
    }

    fun onMicClicked() {
        if (prefs.companionPaused.value) {
            _statusText.value = "Maya is paused in Companion settings."
            return
        }

        if (_assistantState.value == AssistantState.LISTENING) {
            voiceManager.stopListening()
            _assistantState.value = AssistantState.IDLE
        } else if (_assistantState.value == AssistantState.SPEAKING) {
            voiceManager.stopSpeaking()
            _assistantState.value = AssistantState.IDLE
        } else {
            _statusText.value = "Listening..."
            voiceManager.startListening()
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        _assistantState.value = AssistantState.IDLE
        _statusText.value = ""
    }

    fun processUserInput(input: String, isVoice: Boolean = false) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        _statusText.value = trimmed

        viewModelScope.launch {
            // 1. Record User Message
            repository.addMessage(sender = "USER", text = trimmed, isVoice = isVoice)

            // 2. Evaluate Local Commands first
            val localResult = localCommandEngine.evaluate(trimmed)
            if (localResult.isHandled) {
                val responseMsg = localResult.displayText ?: "Done, Boss."
                _statusText.value = responseMsg
                repository.addMessage(sender = "MAYA", text = responseMsg, isVoice = isVoice)

                // Execute action if needed
                handleLocalAction(localResult.actionType)

                if (!localResult.spokenResponse.isNullOrBlank()) {
                    voiceManager.speak(localResult.spokenResponse)
                }
                return@launch
            }

            // 3. Fallback to Gemini Brain
            _assistantState.value = AssistantState.THINKING
            _statusText.value = "Thinking..."

            val recentHistory = repository.getRecentTurns(4)
            when (val result = geminiClient.generateResponse(trimmed, recentHistory)) {
                is GeminiResult.Success -> {
                    _statusText.value = result.text
                    repository.addMessage(sender = "MAYA", text = result.text, isVoice = isVoice)
                    voiceManager.speak(result.text)
                }
                is GeminiResult.Error -> {
                    _statusText.value = result.message
                    repository.addMessage(sender = "MAYA", text = result.message, isVoice = isVoice)
                    voiceManager.speak(result.message)
                    _assistantState.value = AssistantState.ERROR
                }
            }
        }
    }

    private fun handleLocalAction(action: ActionType) {
        val context = getApplication<Application>()
        when (action) {
            ActionType.OPEN_CAMERA -> {
                navigateTo(ScreenState.VISION)
            }
            ActionType.OPEN_SETTINGS -> {
                navigateTo(ScreenState.SETTINGS)
            }
            ActionType.OPEN_CALCULATOR -> {
                try {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALCULATOR)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    _statusText.value = "Calculator is not available directly."
                }
            }
            ActionType.OPEN_BROWSER -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (ignored: Exception) {}
            }
            ActionType.OPEN_DIALER -> {
                try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (ignored: Exception) {}
            }
            ActionType.SHOW_HISTORY -> {
                navigateTo(ScreenState.CONVERSATION)
            }
            ActionType.SHOW_SECURITY -> {
                navigateTo(ScreenState.SECURITY)
            }
            ActionType.SHOW_WEATHER -> {
                navigateTo(ScreenState.WEATHER)
            }
            ActionType.PAUSE_MAYA -> {
                prefs.setCompanionPaused(true)
                _assistantState.value = AssistantState.PAUSED
            }
            ActionType.STOP_SPEAKING -> {
                stopSpeaking()
            }
            else -> {}
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun clearAllStoredData() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearMemories()
            prefs.clearAllData()
            _isAppUnlocked.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
