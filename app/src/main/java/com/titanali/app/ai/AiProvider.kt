package com.titanali.app.ai

import kotlinx.coroutines.flow.Flow

/**
 * Common interface for all AI backends.
 * [streamChat] emits the assistant answer incrementally (token by token).
 *
 * [keyUrl] is the page where the user can create a free API key for this provider
 * (shown in Settings). [modelsUrl] is the OpenAI-compatible `/models` listing
 * endpoint when the provider exposes one (used to refresh the model list live).
 *
 * [needsKey] = the provider refuses anonymous requests. [keyOptional] = works
 * without a key, but accepts one for higher limits (the key field stays enabled).
 */
interface AiProvider {

    val id: String
    val name: String
    val needsKey: Boolean
    val keyOptional: Boolean get() = false
    val defaultModels: List<String>
    val keyUrl: String?
    val modelsUrl: String?

    suspend fun streamChat(messages: List<AiMessage>, model: String, apiKey: String): Flow<String>

    suspend fun listModels(apiKey: String): List<String> = defaultModels
}
