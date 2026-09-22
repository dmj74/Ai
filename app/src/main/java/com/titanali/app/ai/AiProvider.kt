package com.titanali.app.ai

import kotlinx.coroutines.flow.Flow

/**
 * Common interface for all AI backends.
 * [streamChat] emits the assistant answer incrementally (token by token).
 */
interface AiProvider {

    val id: String
    val name: String
    val needsKey: Boolean
    val defaultModels: List<String>

    suspend fun streamChat(messages: List<AiMessage>, model: String, apiKey: String): Flow<String>

    suspend fun listModels(apiKey: String): List<String> = defaultModels
}
