package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.core.model.AssistantState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class MayaVoiceManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onStateChange: (AssistantState) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _voiceAmplitude = MutableStateFlow(0f)
    val voiceAmplitude: StateFlow<Float> = _voiceAmplitude.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    var pitch: Float = 1.1f
        set(value) {
            field = value
            textToSpeech?.setPitch(value)
        }

    var speechRate: Float = 0.9f
        set(value) {
            field = value
            textToSpeech?.setSpeechRate(value)
        }

    var languageCode: String = "auto"

    init {
        initSpeechRecognizer()
        initTextToSpeech()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@MayaVoiceManager)
            }
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setPitch(pitch)
            textToSpeech?.setSpeechRate(speechRate)

            // Select natural voice or locale
            val defaultLocale = Locale.getDefault()
            val result = textToSpeech?.setLanguage(defaultLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.US)
            }

            // Prefer natural female voice where supported by the engine
            try {
                val voices = textToSpeech?.voices
                val femaleVoice = voices?.firstOrNull {
                    it.name.contains("female", ignoreCase = true) ||
                    it.name.contains("woman", ignoreCase = true) ||
                    it.name.contains("en-us-x-sfg", ignoreCase = true) ||
                    it.name.contains("bn-in-x-", ignoreCase = true)
                }
                if (femaleVoice != null) {
                    textToSpeech?.voice = femaleVoice
                }
            } catch (ignored: Exception) {
            }

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onStateChange(AssistantState.SPEAKING)
                }

                override fun onDone(utteranceId: String?) {
                    _voiceAmplitude.value = 0f
                    onStateChange(AssistantState.IDLE)
                }

                override fun onError(utteranceId: String?) {
                    _voiceAmplitude.value = 0f
                    onStateChange(AssistantState.IDLE)
                }
            })
        }
    }

    fun startListening() {
        stopSpeaking()
        _speechError.value = null

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val recognizer = speechRecognizer
        if (recognizer == null) {
            _speechError.value = "Speech recognition is not available on this device."
            onStateChange(AssistantState.ERROR)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            val targetLocale = when (languageCode) {
                "bn" -> "bn-BD"
                "hi" -> "hi-IN"
                "en" -> "en-US"
                else -> Locale.getDefault().toLanguageTag()
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, targetLocale)
        }

        try {
            recognizer.startListening(intent)
            onStateChange(AssistantState.LISTENING)
        } catch (e: Exception) {
            _speechError.value = e.localizedMessage ?: "Failed to start microphone"
            onStateChange(AssistantState.ERROR)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (ignored: Exception) {}
        _voiceAmplitude.value = 0f
    }

    fun speak(text: String, utteranceId: String = "MAYA_RESPONSE") {
        if (!isTtsInitialized || text.isBlank()) {
            return
        }

        // Clean out excessive markdown symbols for smooth audio recitation
        val cleanText = text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("[#*`_~]"), "")
            .trim()

        // Set language based on content
        val hasBengali = cleanText.any { it in '\u0980'..'\u09FF' }
        val hasHindi = cleanText.any { it in '\u0900'..'\u097F' }
        if (hasBengali) {
            textToSpeech?.setLanguage(Locale("bn", "BD"))
        } else if (hasHindi) {
            textToSpeech?.setLanguage(Locale("hi", "IN"))
        } else {
            textToSpeech?.setLanguage(Locale.US)
        }

        onStateChange(AssistantState.SPEAKING)
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
            _voiceAmplitude.value = 0f
            onStateChange(AssistantState.IDLE)
        }
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        onStateChange(AssistantState.LISTENING)
    }

    override fun onBeginningOfSpeech() {
        onStateChange(AssistantState.LISTENING)
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Map dB to normalized 0..1 scale for voice amplitude animation
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _voiceAmplitude.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _voiceAmplitude.value = 0f
        onStateChange(AssistantState.THINKING)
    }

    override fun onError(error: Int) {
        _voiceAmplitude.value = 0f
        val msg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap to try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
            SpeechRecognizer.ERROR_NETWORK -> "Network issue while recognizing voice."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
            else -> "Voice recognition paused."
        }
        _speechError.value = msg
        onStateChange(AssistantState.IDLE)
    }

    override fun onResults(results: Bundle?) {
        _voiceAmplitude.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val bestResult = matches?.firstOrNull()
        if (!bestResult.isNullOrBlank()) {
            onSpeechRecognized(bestResult)
        } else {
            onStateChange(AssistantState.IDLE)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull()
        if (!partial.isNullOrBlank()) {
            // Live speech updates
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (ignored: Exception) {}
    }
}
