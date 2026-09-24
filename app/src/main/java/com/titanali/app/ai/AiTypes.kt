package com.titanali.app.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Chat message sent to / received from any provider. */
@Serializable
data class AiMessage(
    val role: String,
    val content: String,
)

// ---------- OpenAI-compatible (Groq / OpenRouter / Hugging Face / Cerebras / Mistral) ----------

@Serializable
data class OaiRequest(
    val model: String,
    val messages: List<AiMessage>,
    val stream: Boolean = true,
    val temperature: Double = 0.7,
)

@Serializable
data class OaiStreamChunk(
    val choices: List<OaiChoice> = emptyList(),
)

@Serializable
data class OaiChoice(
    val delta: OaiDelta = OaiDelta(),
)

@Serializable
data class OaiDelta(
    val content: String? = null,
    @SerialName("reasoning_content") val reasoningContent: String? = null,
)

/** A complete, non-streaming chat completion (fallback path). */
@Serializable
data class OaiCompletion(
    val choices: List<OaiCompletionChoice> = emptyList(),
)

@Serializable
data class OaiCompletionChoice(
    val message: OaiDelta? = null,
)

/** Response of a `GET /models` listing endpoint. */
@Serializable
data class OaiModels(
    val data: List<OaiModelInfo> = emptyList(),
)

@Serializable
data class OaiModelInfo(
    val id: String? = null,
    /** LLM7: `turbo` models are the anonymous/free ones. */
    val tier: String? = null,
    /** LLM7: `chat`, `image`, `video`… */
    @SerialName("model_type") val modelType: String? = null,
)

// ---------- Pollinations (keyless) ----------

/** One entry of `GET https://text.pollinations.ai/models` (a bare JSON array). */
@Serializable
data class PollinationsModel(
    val name: String? = null,
    val tier: String? = null,
    val aliases: List<String> = emptyList(),
)

// ---------- Google Gemini ----------

@Serializable
data class GeminiTextPart(val text: String)

@Serializable
data class GeminiContent(
    val parts: List<GeminiTextPart>,
    val role: String? = null,
)

@Serializable
data class GeminiSystemInstruction(val parts: List<GeminiTextPart>)

@Serializable
data class GeminiGenConfig(val temperature: Double = 0.7)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null,
    val generationConfig: GeminiGenConfig? = null,
)

@Serializable
data class GeminiStreamChunk(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
)

@Serializable
data class GeminiModels(
    val models: List<GeminiModelInfo> = emptyList(),
)

@Serializable
data class GeminiModelInfo(
    val name: String? = null,
)

// ---------- Ollama (local) ----------

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<AiMessage>,
    val stream: Boolean = true,
)

@Serializable
data class OllamaChunk(
    val message: OllamaMessage? = null,
    val done: Boolean = false,
)

@Serializable
data class OllamaMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class OllamaTags(
    val models: List<OllamaModelInfo> = emptyList(),
)

@Serializable
data class OllamaModelInfo(
    val name: String? = null,
)
