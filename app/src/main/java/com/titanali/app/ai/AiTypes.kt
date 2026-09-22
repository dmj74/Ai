package com.titanali.app.ai

import kotlinx.serialization.Serializable

/** Chat message sent to / received from any provider. */
@Serializable
data class AiMessage(
    val role: String,
    val content: String,
)

// ---------- OpenAI-compatible (Groq / OpenRouter / Hugging Face) ----------

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
