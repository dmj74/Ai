package com.titanali.app.ai

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Pure, unit-testable helpers for the streaming (SSE / NDJSON) protocols
 * used by the providers. Kept free of Android/IO dependencies so the
 * parsing rules can be verified by JVM unit tests.
 */
object SseParser {

    /**
     * Lenient JSON: providers keep adding fields, we only read what we need.
     *
     * `encodeDefaults` is essential: without it `stream = true` (a default
     * value) is silently dropped from requests, providers answer with one
     * non-streaming JSON body and the SSE reader shows an empty reply.
     * `explicitNulls = false` keeps optional fields (e.g. Gemini's
     * `systemInstruction`) out of the payload instead of sending `null`.
     */
    @OptIn(ExperimentalSerializationApi::class)
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    /**
     * Returns the payload of a Server-Sent-Events `data:` line, trimmed,
     * or null when the line carries no payload (comment / event / empty).
     */
    fun dataPayload(line: String): String? {
        if (!line.startsWith("data:")) return null
        val payload = line.removePrefix("data:").trim()
        return payload.ifEmpty { null }
    }

    /** True for the OpenAI-style stream terminator. */
    fun isDone(payload: String): Boolean = payload == "[DONE]"

    /** Extracts `choices[0].delta.content` from an OpenAI-compatible chunk. */
    fun openAiDelta(payload: String): String? = try {
        json.decodeFromString(OaiStreamChunk.serializer(), payload)
            .choices.firstOrNull()?.delta?.let { it.content ?: it.reasoningContent }
            ?.takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }

    /**
     * Extracts `choices[0].message.content` from a complete (non-streaming)
     * OpenAI-compatible response body. Used as a fallback when a server
     * ignores `stream = true`.
     */
    fun openAiMessage(body: String): String? = try {
        json.decodeFromString(OaiCompletion.serializer(), body.trim())
            .choices.firstOrNull()?.message?.let { it.content ?: it.reasoningContent }
            ?.trim()?.takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }

    /** Extracts `candidates[0].content.parts[0].text` from a Gemini chunk. */
    fun geminiText(payload: String): String? = try {
        json.decodeFromString(GeminiStreamChunk.serializer(), payload)
            .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?.takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }

    /** Extracts the text of one Ollama NDJSON chat line (`message.content`). */
    fun ollamaText(line: String): String? = try {
        json.decodeFromString(OllamaChunk.serializer(), line)
            .message?.content?.takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }

    /** True when an Ollama NDJSON line signals the end of the stream. */
    fun ollamaDone(line: String): Boolean = try {
        json.decodeFromString(OllamaChunk.serializer(), line).done
    } catch (e: Exception) {
        false
    }
}
