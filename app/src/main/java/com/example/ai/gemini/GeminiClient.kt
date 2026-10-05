package com.example.ai.gemini

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val text: String) : GeminiResult()
    data class Error(val message: String, val isQuotaError: Boolean = false, val isAuthError: Boolean = false) : GeminiResult()
}

class GeminiClient(
    private val apiKeyProvider: () -> String,
    private val modelProvider: () -> String = { "gemini-3.5-flash" }
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        You are Maya, an intelligent, calm, friendly, respectful, and confident personal AI companion and assistant for the Android app Maya Free ("Your Voice. Your Security. Your AI.").
        
        Personality & Communication rules:
        - Natural, intelligent, respectful, empathetic, and warm. You frequently address the user politely (e.g., as "Boss" or naturally).
        - When the user communicates in Bengali, respond in fluent, natural Bengali.
        - When the user communicates in English, respond in English.
        - When the user communicates in Hindi, respond in Hindi.
        - Keep responses concise, spoken-friendly, and engaging, unless the user asks for deep analysis or detailed step-by-step instructions.
        - Avoid robotic disclaimers. Always identify as Maya the AI companion; never claim to be a biological human being.
        - Do not use heavy markdown formatting (avoid nested tables or asterisk-heavy bullet points) because your text is read aloud by Text-To-Speech.
    """.trimIndent()

    private fun resolveApiKey(): String {
        val userKey = apiKeyProvider().trim()
        if (userKey.isNotEmpty()) return userKey
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") return buildKey
        return ""
    }

    suspend fun generateResponse(
        prompt: String,
        recentHistory: List<Pair<String, String>> = emptyList() // List of (userPrompt, mayaResponse)
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext GeminiResult.Error(
                message = "Gemini isn't configured yet, Boss. Please add your Gemini API Key in Settings.",
                isAuthError = true
            )
        }

        val contentsList = mutableListOf<GeminiContent>()

        // Add previous conversation turns for follow-up context
        for ((userMsg, assistantMsg) in recentHistory.takeLast(4)) {
            contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMsg))))
            contentsList.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = assistantMsg))))
        }

        // Add current user prompt
        contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                maxOutputTokens = 1024
            )
        )

        executeRequest(apiKey, request)
    }

    suspend fun analyzeImage(
        bitmap: Bitmap,
        prompt: String
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext GeminiResult.Error(
                message = "Gemini isn't configured yet, Boss. Please add your Gemini API Key in Settings.",
                isAuthError = true
            )
        }

        val base64Image = bitmapToBase64(bitmap)
        val contents = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(
                    GeminiPart(text = if (prompt.isBlank()) "Describe what you see in this image in detail and highlight key objects or text." else prompt),
                    GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image))
                )
            )
        )

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.4f,
                maxOutputTokens = 1024
            )
        )

        executeRequest(apiKey, request)
    }

    suspend fun testConnection(): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext GeminiResult.Error(
                message = "API Key is missing or empty.",
                isAuthError = true
            )
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Hello Maya, respond with a quick 5-word confirmation."))
                )
            ),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 60)
        )

        executeRequest(apiKey, request)
    }

    private fun executeRequest(apiKey: String, geminiRequest: GeminiRequest): GeminiResult {
        return try {
            val jsonPayload = requestAdapter.toJson(geminiRequest)
            val modelName = modelProvider().ifEmpty { "gemini-3.5-flash" }
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val httpRequest = Request.Builder()
                .url(url)
                .post(jsonPayload.toRequestBody(mediaType))
                .build()

            val httpResponse = okHttpClient.newCall(httpRequest).execute()
            val responseBody = httpResponse.body?.string() ?: ""

            if (!httpResponse.isSuccessful) {
                if (httpResponse.code == 429) {
                    return GeminiResult.Error(
                        message = "Sorry Boss, the AI service rate limit has been reached. Please try again shortly.",
                        isQuotaError = true
                    )
                } else if (httpResponse.code == 400 || httpResponse.code == 403) {
                    return GeminiResult.Error(
                        message = "Invalid or restricted Gemini API Key. Please verify your key in Settings.",
                        isAuthError = true
                    )
                }
                return GeminiResult.Error("Service error: HTTP ${httpResponse.code}")
            }

            val parsedResponse = responseAdapter.fromJson(responseBody)
            val candidateText = parsedResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!candidateText.isNullOrBlank()) {
                GeminiResult.Success(candidateText.trim())
            } else if (parsedResponse?.error != null) {
                GeminiResult.Error(parsedResponse.error.message ?: "Unknown API response error")
            } else {
                GeminiResult.Error("Received empty response from AI core.")
            }
        } catch (e: Exception) {
            GeminiResult.Error(
                e.localizedMessage ?: "Network connection failed. Please check internet access."
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize down if excessively large to save bandwidth while retaining high clarity
        val scaledBitmap = if (bitmap.width > 1280 || bitmap.height > 1280) {
            val ratio = 1280f / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
