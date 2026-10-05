package com.example.core.model

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    INTERRUPTED,
    ERROR,
    AUTHENTICATING,
    PAUSED
}

data class WeatherData(
    val city: String,
    val country: String,
    val temperatureCelsius: Int,
    val condition: String,
    val highCelsius: Int,
    val lowCelsius: Int,
    val summary: String,
    val hourly: List<HourlyForecast>
)

data class HourlyForecast(
    val timeLabel: String,
    val tempCelsius: Int,
    val iconType: String // "sunny", "cloudy", "rain"
)

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String
)

enum class ActionType {
    NONE,
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    OPEN_CAMERA,
    OPEN_SETTINGS,
    OPEN_CALCULATOR,
    OPEN_BROWSER,
    OPEN_DIALER,
    OPEN_MESSAGES,
    SHOW_HISTORY,
    SHOW_SECURITY,
    SHOW_WEATHER,
    PAUSE_MAYA,
    STOP_SPEAKING
}

data class LocalCommandResult(
    val isHandled: Boolean,
    val spokenResponse: String? = null,
    val displayText: String? = null,
    val actionType: ActionType = ActionType.NONE,
    val extraData: String? = null
)

data class SecurityStatus(
    val isBiometricAvailable: Boolean,
    val isBiometricEnabled: Boolean,
    val isFaceSecurityConfigured: Boolean,
    val isAppLockActive: Boolean,
    val isMicPermitted: Boolean,
    val isCameraPermitted: Boolean,
    val isLocationPermitted: Boolean,
    val isContactsPermitted: Boolean
)
