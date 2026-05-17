package com.gymlog.app.data.remote

import com.gymlog.app.domain.model.LLMProvider
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMApiClient @Inject constructor(
    private val gson: Gson
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun sendReviewRequest(
        prompt: String,
        apiKey: String,
        provider: LLMProvider,
        model: String,
        customBaseUrl: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = customBaseUrl ?: getBaseUrl(provider)
            val request = when (provider) {
                LLMProvider.OPENAI, LLMProvider.DEEPSEEK -> buildOpenAIRequest(baseUrl, apiKey, model, prompt)
                LLMProvider.ANTHROPIC -> buildAnthropicRequest(baseUrl, apiKey, model, prompt)
                LLMProvider.GEMINI -> buildGeminiRequest(baseUrl, apiKey, model, prompt)
            }

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(parseResponse(body, provider))
            } else {
                Result.failure(Exception("API Error ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getBaseUrl(provider: LLMProvider): String = when (provider) {
        LLMProvider.OPENAI -> "https://api.openai.com"
        LLMProvider.ANTHROPIC -> "https://api.anthropic.com"
        LLMProvider.DEEPSEEK -> "https://api.deepseek.com"
        LLMProvider.GEMINI -> "https://generativelanguage.googleapis.com"
    }

    private fun buildOpenAIRequest(baseUrl: String, apiKey: String, model: String, prompt: String): Request {
        val json = JsonObject().apply {
            addProperty("model", model)
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", "You are a professional strength training coach. You must respond in valid JSON format exactly as requested. Do not include markdown code fences.")
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", prompt)
                })
            })
            addProperty("temperature", 0.7)
            addProperty("max_tokens", 2048)
        }
        return Request.Builder()
            .url("$baseUrl/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun buildAnthropicRequest(baseUrl: String, apiKey: String, model: String, prompt: String): Request {
        val json = JsonObject().apply {
            addProperty("model", model)
            addProperty("max_tokens", 2048)
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", prompt + "\n\nYou must respond in valid JSON format exactly as requested. Do not include markdown code fences.")
                })
            })
            addProperty("temperature", 0.7)
        }
        return Request.Builder()
            .url("$baseUrl/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun buildGeminiRequest(baseUrl: String, apiKey: String, model: String, prompt: String): Request {
        val json = JsonObject().apply {
            add("contents", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    add("parts", JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("text", prompt + "\n\nYou must respond in valid JSON format exactly as requested. Do not include markdown code fences.")
                        })
                    })
                })
            })
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", 0.7)
                addProperty("maxOutputTokens", 2048)
            })
        }
        return Request.Builder()
            .url("$baseUrl/v1beta/models/$model:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun parseResponse(body: String, provider: LLMProvider): String {
        return try {
            val json = gson.fromJson(body, JsonObject::class.java)
            when (provider) {
                LLMProvider.OPENAI, LLMProvider.DEEPSEEK -> {
                    json.getAsJsonArray("choices")
                        ?.get(0)?.asJsonObject
                        ?.getAsJsonObject("message")
                        ?.get("content")?.asString ?: body
                }
                LLMProvider.ANTHROPIC -> {
                    json.getAsJsonArray("content")
                        ?.get(0)?.asJsonObject
                        ?.get("text")?.asString ?: body
                }
                LLMProvider.GEMINI -> {
                    json.getAsJsonArray("candidates")
                        ?.get(0)?.asJsonObject
                        ?.getAsJsonObject("content")
                        ?.getAsJsonArray("parts")
                        ?.get(0)?.asJsonObject
                        ?.get("text")?.asString ?: body
                }
            }
        } catch (_: Exception) {
            body
        }
    }
}
