package com.example.core.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("maya_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_LANGUAGE = "voice_language"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_VOICE_SPEED = "voice_speed"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_FACE_SECURITY_ENABLED = "face_security_enabled"
        private const val KEY_COMPANION_PAUSED = "companion_paused"
        private const val KEY_PROACTIVE_AI = "proactive_ai"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
    }

    private val _geminiApiKey = MutableStateFlow(prefs.getString(KEY_GEMINI_API_KEY, "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _geminiModel = MutableStateFlow(prefs.getString(KEY_GEMINI_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val geminiModel: StateFlow<String> = _geminiModel.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "auto") ?: "auto")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _voicePitch = MutableStateFlow(prefs.getFloat(KEY_VOICE_PITCH, 1.1f))
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(prefs.getFloat(KEY_VOICE_SPEED, 0.9f))
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK_ENABLED, false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true))
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _faceSecurityEnabled = MutableStateFlow(prefs.getBoolean(KEY_FACE_SECURITY_ENABLED, false))
    val faceSecurityEnabled: StateFlow<Boolean> = _faceSecurityEnabled.asStateFlow()

    private val _companionPaused = MutableStateFlow(prefs.getBoolean(KEY_COMPANION_PAUSED, false))
    val companionPaused: StateFlow<Boolean> = _companionPaused.asStateFlow()

    private val _proactiveAi = MutableStateFlow(prefs.getBoolean(KEY_PROACTIVE_AI, false))
    val proactiveAi: StateFlow<Boolean> = _proactiveAi.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_ENABLED, true))
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
        _geminiApiKey.value = key.trim()
    }

    fun setGeminiModel(model: String) {
        prefs.edit().putString(KEY_GEMINI_MODEL, model).apply()
        _geminiModel.value = model
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setVoicePitch(pitch: Float) {
        prefs.edit().putFloat(KEY_VOICE_PITCH, pitch).apply()
        _voicePitch.value = pitch
    }

    fun setVoiceSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_VOICE_SPEED, speed).apply()
        _voiceSpeed.value = speed
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
        _appLockEnabled.value = enabled
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _biometricEnabled.value = enabled
    }

    fun setFaceSecurityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FACE_SECURITY_ENABLED, enabled).apply()
        _faceSecurityEnabled.value = enabled
    }

    fun setCompanionPaused(paused: Boolean) {
        prefs.edit().putBoolean(KEY_COMPANION_PAUSED, paused).apply()
        _companionPaused.value = paused
    }

    fun setProactiveAi(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PROACTIVE_AI, enabled).apply()
        _proactiveAi.value = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
        _hapticEnabled.value = enabled
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
        _geminiApiKey.value = ""
        _appLockEnabled.value = false
        _faceSecurityEnabled.value = false
        _companionPaused.value = false
    }
}
