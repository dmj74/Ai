package com.titanali.app.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Groq — free tier with very fast Llama models. Key: https://console.groq.com/keys */
class GroqProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "groq",
        name = "Groq",
        endpoint = "https://api.groq.com/openai/v1/chat/completions",
        defaultModels = listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "mixtral-8x7b-softmax",
            "gemma2-9b-it",
        ),
        client = client,
    )

/** OpenRouter — access to many models, some completely free (":free" suffix). Key: https://openrouter.ai/keys */
class OpenRouterProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "openrouter",
        name = "OpenRouter",
        endpoint = "https://openrouter.ai/api/v1/chat/completions",
        defaultModels = listOf(
            "meta-llama/llama-3.3-70b-instruct:free",
            "meta-llama/llama-3.1-8b-instruct:free",
            "mistralai/mistral-7b-instruct:free",
            "google/gemini-2.0-flash-exp:free",
        ),
        client = client,
    )

/** Hugging Face — free inference endpoint. Key: https://huggingface.co/settings/tokens */
class HuggingFaceProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "huggingface",
        name = "Hugging Face",
        endpoint = "https://api-inference.huggingface.co/v1/chat/completions",
        defaultModels = listOf(
            "meta-llama/Meta-Llama-3.1-8B-Instruct",
            "mistralai/Mistral-7B-Instruct-v0.3",
            "google/gemma-2-9b-it",
        ),
        client = client,
    )

/** Google Gemini — generous free tier on AI Studio. Key: https://aistudio.google.com/app/apikey */
class GeminiProvider(private val client: OkHttpClient) : AiProvider {

    override val id: String = "gemini"
    override val name: String = "Google Gemini"
    override val needsKey: Boolean = true
    override val defaultModels: List<String> =
        listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro")

    private val json: Json = Json { ignoreUnknownKeys = true }

    override suspend fun streamChat(
        messages: List<AiMessage>,
        model: String,
        apiKey: String,
    ): Flow<String> = callbackFlow {
        val system = messages.firstOrNull { it.role == "system" }?.content
        val rest = messages.filter { it.role != "system" }
        val request = GeminiRequest(
            contents = rest.map {
                GeminiContent(
                    parts = listOf(GeminiTextPart(it.content)),
                    role = if (it.role == "assistant") "model" else "user",
                )
            },
            systemInstruction = system?.let { GeminiSystemInstruction(listOf(GeminiTextPart(it))) },
            generationConfig = GeminiGenConfig(0.7),
        )
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val call = client.newCall(
            Request.Builder()
                .url(url)
                .post(
                    json.encodeToString(GeminiRequest.serializer(), request)
                        .toRequestBody("application/json".toMediaType())
                )
                .header("Accept", "text/event-stream")
                .build(),
        )
        val job = launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        val detail = response.body?.string()?.take(300).orEmpty()
                        close(RuntimeException("HTTP ${response.code}: $detail"))
                        return@use
                    }
                    val source = response.body!!.source()
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (!line.startsWith("data:")) continue
                        val data = line.removePrefix("data:").trim()
                        if (data.isEmpty()) continue
                        val chunk = json.decodeFromString(GeminiStreamChunk.serializer(), data)
                        val text = chunk.candidates.firstOrNull()
                            ?.content
                            ?.parts
                            ?.firstOrNull()
                            ?.text
                        if (!text.isNullOrEmpty()) emit(text)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!call.isCanceled) close(e)
            }
        }
        awaitClose {
            job.cancel()
            call.cancel()
        }
    }
}

/** Ollama — fully local & free, no API key. Runs on PC/emulator network. */
class OllamaProvider(
    private val client: OkHttpClient,
    private val hostProvider: () -> String,
) : AiProvider {

    override val id: String = "ollama"
    override val name: String = "Ollama (local)"
    override val needsKey: Boolean = false
    override val defaultModels: List<String> =
        listOf("llama3.1", "llama3.2", "mistral", "gemma2")

    private val json: Json = Json { ignoreUnknownKeys = true }

    private fun baseUrl(): String = hostProvider().trim().trimEnd('/')

    override suspend fun listModels(apiKey: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(
                    Request.Builder().url("$baseUrl()/api/tags").get().build(),
                ).execute()
                if (response.isSuccessful) {
                    json.decodeFromString(OllamaTags.serializer(), response.body!!.string())
                        .models
                        .mapNotNull { it.name }
                } else {
                    defaultModels
                }
            } catch (e: Exception) {
                defaultModels
            }
        }

    override suspend fun streamChat(
        messages: List<AiMessage>,
        model: String,
        apiKey: String,
    ): Flow<String> = callbackFlow {
        val request = OllamaChatRequest(model = model, messages = messages)
        val call = client.newCall(
            Request.Builder()
                .url("$baseUrl()/api/chat")
                .post(
                    json.encodeToString(OllamaChatRequest.serializer(), request)
                        .toRequestBody("application/json".toMediaType())
                )
                .build(),
        )
        val job = launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        close(RuntimeException("HTTP ${response.code} — Ollama را در نشانی تنظیمات روشن کن"))
                        return@use
                    }
                    val source = response.body!!.source()
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.isBlank()) continue
                        val chunk = json.decodeFromString(OllamaChunk.serializer(), line)
                        val text = chunk.message?.content
                        if (!text.isNullOrEmpty()) emit(text)
                        if (chunk.done) break
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!call.isCanceled) close(e)
            }
        }
        awaitClose {
            job.cancel()
            call.cancel()
        }
    }
}

object ProviderRegistry {
    fun create(http: OkHttpClient, ollamaHost: () -> String): Map<String, AiProvider> = linkedMapOf(
        "groq" to GroqProvider(http),
        "gemini" to GeminiProvider(http),
        "huggingface" to HuggingFaceProvider(http),
        "openrouter" to OpenRouterProvider(http),
        "ollama" to OllamaProvider(http, ollamaHost),
    )
}
