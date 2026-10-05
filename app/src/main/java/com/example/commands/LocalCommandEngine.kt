package com.example.commands

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import com.example.core.model.ActionType
import com.example.core.model.LocalCommandResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LocalCommandEngine(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    fun evaluate(rawInput: String): LocalCommandResult {
        val query = rawInput.trim().lowercase()

        // 1. Time query
        if (query.contains("কয়টা বাজে") || query.contains("time") || query.contains("সময় কত") || query.contains("what time")) {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val formattedTime = timeFormat.format(Date())
            val isBn = query.contains("বাজে") || query.contains("সময়")
            val response = if (isBn) "এখন সময় $formattedTime, Boss।" else "It is currently $formattedTime, Boss."
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = response,
                displayText = response
            )
        }

        // 2. Date query
        if (query.contains("কত তারিখ") || query.contains("আজকের তারিখ") || query.contains("what date") || query.contains("today's date")) {
            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date())
            val isBn = query.contains("তারিখ")
            val response = if (isBn) "আজ $formattedDate, Boss।" else "Today is $formattedDate, Boss."
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = response,
                displayText = response
            )
        }

        // 3. Battery percentage
        if (query.contains("ব্যাটারি") || query.contains("battery")) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val isBn = query.contains("ব্যাটারি")
            val response = if (level >= 0) {
                if (isBn) "বর্তমানে ব্যাটারি লেভেল $level শতাংশ, Boss।" else "Your battery is at $level percent, Boss."
            } else {
                if (isBn) "ব্যাটারি তথ্য পাওয়া যায়নি, Boss।" else "Battery level couldn't be detected, Boss."
            }
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = response,
                displayText = response
            )
        }

        // 4. Flashlight on/off
        if (query.contains("ফ্ল্যাশলাইট অন") || query.contains("turn on flashlight") || query.contains("torch on")) {
            val success = toggleFlashlight(true)
            val isBn = query.contains("ফ্ল্যাশলাইট")
            val msg = if (success) {
                if (isBn) "ফ্ল্যাশলাইট চালু করেছি, Boss।" else "Flashlight turned on, Boss."
            } else {
                if (isBn) "দুঃখিত Boss, ফ্ল্যাশলাইট চালু করা সম্ভব হয়নি।" else "Sorry Boss, couldn't turn on the flashlight."
            }
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = msg,
                displayText = msg,
                actionType = ActionType.FLASHLIGHT_ON
            )
        }

        if (query.contains("ফ্ল্যাশলাইট অফ") || query.contains("turn off flashlight") || query.contains("torch off")) {
            val success = toggleFlashlight(false)
            val isBn = query.contains("ফ্ল্যাশলাইট")
            val msg = if (success) {
                if (isBn) "ফ্ল্যাশলাইট বন্ধ করেছি, Boss।" else "Flashlight turned off, Boss."
            } else {
                if (isBn) "দুঃখিত Boss, ফ্ল্যাশলাইট বন্ধ করা সম্ভব হয়নি।" else "Sorry Boss, couldn't turn off the flashlight."
            }
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = msg,
                displayText = msg,
                actionType = ActionType.FLASHLIGHT_OFF
            )
        }

        // 5. Open Apps / Navigation
        if (query.contains("ক্যামেরা খোলো") || query.contains("open camera") || query.contains("camera on")) {
            val isBn = query.contains("ক্যামেরা")
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = if (isBn) "ক্যামেরা খুলছি, Boss।" else "Opening camera, Boss.",
                displayText = "Opening Camera...",
                actionType = ActionType.OPEN_CAMERA
            )
        }

        if (query.contains("সেটিংস খোলো") || query.contains("open settings")) {
            val isBn = query.contains("সেটিংস")
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = if (isBn) "সেটিংস খুলছি, Boss।" else "Opening settings, Boss.",
                displayText = "Opening Settings...",
                actionType = ActionType.OPEN_SETTINGS
            )
        }

        if (query.contains("ক্যালকুলেটর") || query.contains("calculator")) {
            val isBn = query.contains("ক্যালকুলেটর")
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = if (isBn) "ক্যালকুলেটর খুলছি, Boss।" else "Opening calculator, Boss.",
                displayText = "Opening Calculator...",
                actionType = ActionType.OPEN_CALCULATOR
            )
        }

        if (query.contains("ব্রাউজার") || query.contains("open browser") || query.contains("browser খোলো")) {
            val isBn = query.contains("ব্রাউজার")
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = if (isBn) "ব্রাউজার ওপেন করছি, Boss।" else "Opening web browser, Boss.",
                displayText = "Opening Browser...",
                actionType = ActionType.OPEN_BROWSER
            )
        }

        if (query.contains("ডায়াল") || query.contains("phone dialer") || query.contains("ফোন খোলো")) {
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = "Opening Phone dialer, Boss.",
                displayText = "Opening Dialer...",
                actionType = ActionType.OPEN_DIALER
            )
        }

        // 6. Stop / Pause / Mute
        if (query.contains("কথা বন্ধ করো") || query.contains("stop speaking") || query.contains("থামো") || query.contains("চুপ")) {
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = null,
                displayText = "Maya stopped speaking.",
                actionType = ActionType.STOP_SPEAKING
            )
        }

        if (query.contains("pause maya") || query.contains("বন্ধ হও maya") || query.contains("নিজেকে বন্ধ করে দাও")) {
            val isBn = query.contains("বন্ধ")
            val msg = if (isBn) "ঠিক আছে Boss, আমি সাময়িকভাবে বিরত থাকছি।" else "Paused Maya. Tap the microphone when you need me again, Boss."
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = msg,
                displayText = msg,
                actionType = ActionType.PAUSE_MAYA
            )
        }

        // 7. In-App Navigation triggers
        if (query.contains("history খোলো") || query.contains("open history") || query.contains("ইতিহাস")) {
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = "Opening Conversation History, Boss.",
                displayText = "Opening History...",
                actionType = ActionType.SHOW_HISTORY
            )
        }

        if (query.contains("security খোলো") || query.contains("open security") || query.contains("সিকিউরিটি")) {
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = "Opening Security Center, Boss.",
                displayText = "Opening Security...",
                actionType = ActionType.SHOW_SECURITY
            )
        }

        if (query.contains("আজকের weather") || query.contains("weather কেমন") || query.contains("weather update") || query.contains("আবহাওয়া")) {
            return LocalCommandResult(
                isHandled = true,
                spokenResponse = "Let's check the latest weather updates, Boss.",
                displayText = "Fetching Weather details...",
                actionType = ActionType.SHOW_WEATHER
            )
        }

        return LocalCommandResult(isHandled = false)
    }

    private fun toggleFlashlight(on: Boolean): Boolean {
        return try {
            val manager = cameraManager ?: return false
            val cameraId = manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return false
            manager.setTorchMode(cameraId, on)
            true
        } catch (e: Exception) {
            false
        }
    }
}
