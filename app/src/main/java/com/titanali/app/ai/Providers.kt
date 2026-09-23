package com.titanali.app.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Base implementation for OpenAI-compatible chat completions endpoints
 * with Server-Sent-Events streaming. Model lists are refreshed live from
 * the provider's `/models` endpoint whenever it exposes one.
 */
abstract class OpenAiCompatibleProvider(
    override val id: String,
    override val name: String,
    private val endpoint: String,
    override val needsKey: Boolean = true,
    override val keyOptional: Boolean = false,
    override val defaultModels: List<String> = emptyList(),
    override val keyUrl: String? = null,
    override val modelsUrl: String? = null,
    private val client: OkHttpClient,
) : AiProvider {

    override suspend fun streamChat(
        messages: List<AiMessage>,
        model: String,
        apiKey: String,
    ): Flow<String> = flow {
        val body = SseParser.json.encodeToString(
            OaiRequest.serializer(),
            OaiRequest(model = model, messages = messages),
        )
        val builder = Request.Builder()
            .url(endpoint)
            .post(body.toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
        if ((needsKey || keyOptional) && apiKey.isNotBlank()) {
            builder.addHeader("Authorization", "Bearer $apiKey")
        }
        val call = client.newCall(builder.build())
        try {
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    val detail = response.body?.string()?.take(300).orEmpty()
                    throw AiHttpException(response.code, detail)
                }
                val source = response.body!!.source()
                val raw = StringBuilder()
                var emitted = false
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    raw.append(line).append('\n')
                    val payload = SseParser.dataPayload(line) ?: continue
                    if (SseParser.isDone(payload)) break
                    // Most providers send delta chunks. A few free gateways ignore
                    // stream=true and return one complete JSON response instead;
                    // accepting both keeps the provider contract reliable.
                    val text = SseParser.openAiDelta(payload)
                        ?: SseParser.openAiMessage(payload)
                    if (text != null) {
                        emitted = true
                        emit(text)
                    }
                }
                if (!emitted) {
                    SseParser.openAiMessage(raw.toString())?.let {
                        emitted = true
                        emit(it)
                    }
                }
                if (!emitted) throw AiEmptyReplyException()
            }
        } catch (e: CancellationException) {
            throw e
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun listModels(apiKey: String): List<String> {
        val url = modelsUrl ?: return defaultModels
        return withContext(Dispatchers.IO) {
            try {
                val builder = Request.Builder().url(url).get()
                if ((needsKey || keyOptional) && apiKey.isNotBlank()) {
                    builder.addHeader("Authorization", "Bearer $apiKey")
                }
                client.newCall(builder.build()).execute().use { response ->
                    if (!response.isSuccessful) return@withContext defaultModels
                    val ids = SseParser.json
                        .decodeFromString(OaiModels.serializer(), response.body!!.string())
                        .data
                        .mapNotNull { it.id }
                        .filter { it.isNotBlank() }
                    ids.ifEmpty { defaultModels }
                }
            } catch (e: Exception) {
                defaultModels
            }
        }
    }
}

/** Groq — free tier on very fast LPU hardware. Key: https://console.groq.com/keys */
class GroqProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "groq",
        name = "Groq",
        endpoint = "https://api.groq.com/openai/v1/chat/completions",
        defaultModels = listOf(
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "openai/gpt-oss-safeguard-20b",
            "qwen/qwen3.8-27b",
        ),
        keyUrl = "https://console.groq.com/keys",
        modelsUrl = "https://api.groq.com/openai/v1/models",
        client = client,
    )

/** OpenRouter — one key for hundreds of models, `:free` ones cost nothing. Key: https://openrouter.ai/keys */
class OpenRouterProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "openrouter",
        name = "OpenRouter",
        endpoint = "https://openrouter.ai/api/v1/chat/completions",
        defaultModels = listOf(
            "openrouter/free",
            "nvidia/nemotron-3-super-120b-a12b:free",
            "qwen/qwen3.8-27b:free",
            "z-ai/glm-5.2:free",
        ),
        keyUrl = "https://openrouter.ai/keys",
        modelsUrl = "https://openrouter.ai/api/v1/models",
        client = client,
    )

/** Hugging Face Inference Providers router (OpenAI-compatible). Key: https://huggingface.co/settings/tokens */
class HuggingFaceProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "huggingface",
        name = "Hugging Face",
        endpoint = "https://router.huggingface.co/v1/chat/completions",
        defaultModels = listOf(
            "meta-llama/Llama-3.3-70B-Instruct",
            "deepseek-ai/DeepSeek-R1",
            "mistralai/Mistral-Small-24B-Instruct-2501",
        ),
        keyUrl = "https://huggingface.co/settings/tokens",
        modelsUrl = "https://router.huggingface.co/v1/models",
        client = client,
    )

/** Cerebras — free tier with extremely fast inference. Key: https://cloud.cerebras.ai/ */
class CerebrasProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "cerebras",
        name = "Cerebras",
        endpoint = "https://api.cerebras.ai/v1/chat/completions",
        defaultModels = listOf(
            "llama-3.3-70b",
            "qwen-3-32b",
            "gpt-oss-120b",
        ),
        keyUrl = "https://cloud.cerebras.ai/",
        modelsUrl = "https://api.cerebras.ai/v1/models",
        client = client,
    )

/** Mistral AI — free "Experiment" tier. Key: https://console.mistral.ai/api-keys */
class MistralProvider(client: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "mistral",
        name = "Mistral",
        endpoint = "https://api.mistral.ai/v1/chat/completions",
        defaultModels = listOf(
            "mistral-small-latest",
            "open-mistral-nemo",
            "magistral-small-latest",
        ),
        keyUrl = "https://console.mistral.ai/api-keys",
        modelsUrl = "https://api.mistral.ai/v1/models",
        client = client,
    )

/**
 * Pollinations — anonymous text generation; no account or API key is required
 * for the basic model. A personal key is optional and can improve limits.
 * The legacy endpoint is intentionally used here: it is the currently
 * documented keyless mobile-friendly endpoint, unlike the newer paid gateway.
 */
class PollinationsProvider(private val http: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "pollinations",
        name = "Pollinations (no key)",
        endpoint = "https://text.pollinations.ai/openai",
        needsKey = false,
        keyOptional = true,
        defaultModels = listOf("openai-fast"),
        keyUrl = "https://enter.pollinations.ai/keys",
        modelsUrl = "https://text.pollinations.ai/models",
        client = http,
    ) {

    /** The legacy model catalogue is a JSON array, not an OpenAI object. */
    override suspend fun listModels(apiKey: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val builder = Request.Builder().url(modelsUrl!!).get()
                if (apiKey.isNotBlank()) {
                    builder.addHeader("Authorization", "Bearer $apiKey")
                }
                http.newCall(builder.build()).execute().use { response ->
                    if (!response.isSuccessful) return@withContext defaultModels
                    val body = response.body?.string().orEmpty()
                    val root = SseParser.json.parseToJsonElement(body)
                    val array = root as? kotlinx.serialization.json.JsonArray
                    val names = array.orEmpty().mapNotNull { entry ->
                        try {
                            when (entry) {
                                is kotlinx.serialization.json.JsonObject ->
                                    entry["name"]?.let { value ->
                                        (value as? kotlinx.serialization.json.JsonPrimitive)?.content
                                    }
                                is kotlinx.serialization.json.JsonPrimitive -> entry.content
                                else -> null
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }.filter { it.isNotBlank() }
                    names.ifEmpty { defaultModels }
                }
            } catch (_: Exception) {
                defaultModels
            }
        }
}

/**
 * LLM7 — an OpenAI-compatible anonymous/turbo tier. A free token is optional;
 * the provider can be used without putting a secret in the app or repository.
 */
class Llm7Provider(private val http: OkHttpClient) :
    OpenAiCompatibleProvider(
        id = "llm7",
        name = "LLM7 (no signup)",
        endpoint = "https://api.llm7.io/v1/chat/completions",
        needsKey = false,
        keyOptional = true,
        defaultModels = listOf("mistral-Nemo-Instruct-2407", "minimax-m2.7"),
        keyUrl = "https://token.llm7.io/",
        modelsUrl = "https://api.llm7.io/v1/models",
        client = http,
    ) {

    override suspend fun listModels(apiKey: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val builder = Request.Builder().url(modelsUrl!!).get()
                if (apiKey.isNotBlank()) {
                    builder.addHeader("Authorization", "Bearer $apiKey")
                }
                http.newCall(builder.build()).execute().use { response ->
                    if (!response.isSuccessful) return@withContext defaultModels
                    val body = response.body?.string().orEmpty()
                    val payload = SseParser.json.decodeFromString(OaiModels.serializer(), body)
                    val freeChat = payload.data
                        .filter { it.modelType == "chat" && it.tier == "turbo" }
                        .mapNotNull { it.id }
                        .filter { it.isNotBlank() }
                    freeChat.ifEmpty { defaultModels }
                }
            } catch (_: Exception) {
                defaultModels
            }
        }
}

/** Google Gemini — generous free tier on AI Studio. Key: https://aistudio.google.com/app/apikey */
class GeminiProvider(private val client: OkHttpClient) : AiProvider {

    override val id: String = "gemini"
    override val name: String = "Google Gemini"
    override val needsKey: Boolean = true
    override val defaultModels: List<String> = listOf(
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite",
        "gemini-2.5-pro",
        "gemini-3.8-flash",
    )
    override val keyUrl: String = "https://aistudio.google.com/app/apikey"
    override val modelsUrl: String = "https://generativelanguage.googleapis.com/v1beta/models"

    override suspend fun streamChat(
        messages: List<AiMessage>,
        model: String,
        apiKey: String,
    ): Flow<String> = flow {
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
        val url = "$modelsUrl/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val call = client.newCall(
            Request.Builder()
                .url(url)
                .post(
                    SseParser.json.encodeToString(GeminiRequest.serializer(), request)
                        .toRequestBody("application/json".toMediaType())
                )
                .header("Accept", "text/event-stream")
                .build(),
        )
        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                val detail = response.body?.string()?.take(300).orEmpty()
                throw AiHttpException(response.code, detail)
            }
            val source = response.body!!.source()
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                val payload = SseParser.dataPayload(line) ?: continue
                val text = SseParser.geminiText(payload)
                if (text != null) emit(text)
            }
        } catch (e: CancellationException) {
            throw e
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun listModels(apiKey: String): List<String> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext defaultModels
            try {
                client.newCall(
                    Request.Builder().url("$modelsUrl?key=$apiKey").get().build(),
                ).execute().use { response ->
                    if (!response.isSuccessful) return@withContext defaultModels
                    val names = SseParser.json
                        .decodeFromString(GeminiModels.serializer(), response.body!!.string())
                        .models
                        .mapNotNull { it.name?.removePrefix("models/") }
                        .filter { isChatModel(it) }
                    names.ifEmpty { defaultModels }
                }
            } catch (e: Exception) {
                defaultModels
            }
        }

    private fun isChatModel(name: String): Boolean =
        name.startsWith("gemini-") &&
            !name.contains("tts") &&
            !name.contains("image") &&
            !name.contains("live") &&
            !name.contains("embedding") &&
            !name.contains("transcribe") &&
            !name.contains("vision")
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
    override val keyUrl: String? = null
    override val modelsUrl: String? = null

    private fun baseUrl(): String = hostProvider().trim().trimEnd('/')

    override suspend fun listModels(apiKey: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(
                    Request.Builder().url("${baseUrl()}/api/tags").get().build(),
                ).execute().use { response ->
                    if (response.isSuccessful) {
                        SseParser.json.decodeFromString(OllamaTags.serializer(), response.body!!.string())
                            .models
                            .mapNotNull { it.name }
                    } else {
                        defaultModels
                    }
                }
            } catch (e: Exception) {
                defaultModels
            }
        }

    override suspend fun streamChat(
        messages: List<AiMessage>,
        model: String,
        apiKey: String,
    ): Flow<String> = flow {
        val request = OllamaChatRequest(model = model, messages = messages)
        val call = client.newCall(
            Request.Builder()
                .url("${baseUrl()}/api/chat")
                .post(
                    SseParser.json.encodeToString(OllamaChatRequest.serializer(), request)
                        .toRequestBody("application/json".toMediaType())
                )
                .build(),
        )
        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                val detail = response.body?.string()?.take(300).orEmpty()
                throw AiHttpException(response.code, detail)
            }
            val source = response.body!!.source()
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.isBlank()) continue
                val text = SseParser.ollamaText(line)
                if (text != null) emit(text)
                if (SseParser.ollamaDone(line)) break
            }
        } catch (e: CancellationException) {
            throw e
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)
}

object ProviderRegistry {
    fun create(http: OkHttpClient, ollamaHost: () -> String): Map<String, AiProvider> = linkedMapOf(
        "pollinations" to PollinationsProvider(http),
        "llm7" to Llm7Provider(http),
        "groq" to GroqProvider(http),
        "gemini" to GeminiProvider(http),
        "openrouter" to OpenRouterProvider(http),
        "huggingface" to HuggingFaceProvider(http),
        "cerebras" to CerebrasProvider(http),
        "mistral" to MistralProvider(http),
        "ollama" to OllamaProvider(http, ollamaHost),
    )
}
