package com.titanali.app.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Base implementation for OpenAI-compatible chat completions endpoints
 * with Server-Sent-Events streaming (Groq, OpenRouter, Hugging Face).
 */
abstract class OpenAiCompatibleProvider(
    override val id: String,
    override val name: String,
    private val endpoint: String,
    override val needsKey: Boolean = true,
    override val defaultModels: List<String> = emptyList(),
    private val client: OkHttpClient,
) : AiProvider {

    protected val json: Json = Json { ignoreUnknownKeys = true }

    override suspend fun streamChat(
        messages: List<AiMessage>,
        model: String,
        apiKey: String,
    ): Flow<String> = callbackFlow {
        val body = json.encodeToString(
            OaiRequest.serializer(),
            OaiRequest(model = model, messages = messages),
        )
        val builder = Request.Builder()
            .url(endpoint)
            .post(body.toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
        if (needsKey && apiKey.isNotBlank()) {
            builder.addHeader("Authorization", "Bearer $apiKey")
        }
        val call = client.newCall(builder.build())
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
                        if (data == "[DONE]") break
                        val chunk = json.decodeFromString(OaiStreamChunk.serializer(), data)
                        val delta = chunk.choices.firstOrNull()?.delta?.content
                        if (!delta.isNullOrEmpty()) emit(delta)
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
