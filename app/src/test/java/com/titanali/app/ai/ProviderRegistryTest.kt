package com.titanali.app.ai

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class ProviderRegistryTest {

    private val providers = ProviderRegistry.create(OkHttpClient()) { "http://10.0.2.2:11434" }

    @Test
    fun `registry exposes all advertised backends`() {
        assertEquals(
            listOf(
                "pollinations",
                "llm7",
                "groq",
                "gemini",
                "openrouter",
                "huggingface",
                "cerebras",
                "mistral",
                "ollama",
            ),
            providers.keys.toList(),
        )
    }

    @Test
    fun `every provider has models and key metadata`() {
        providers.values.forEach { p ->
            assertTrue("${p.id} has default models", p.defaultModels.isNotEmpty())
            if (p.needsKey) {
                assertTrue("${p.id} exposes a key page", !p.keyUrl.isNullOrBlank())
            }
        }
    }

    @Test
    fun `anonymous providers do not require a key`() {
        assertFalse(providers.getValue("pollinations").needsKey)
        assertTrue(providers.getValue("pollinations").keyOptional)
        assertEquals("openai-fast", providers.getValue("pollinations").defaultModels.first())
        assertFalse(providers.getValue("llm7").needsKey)
        assertTrue(providers.getValue("llm7").keyOptional)
    }

    @Test
    fun `groq default model is a current free-tier model`() {
        // llama-3.3-70b-versatile became enterprise-only in 2026; the free
        // default must be one of the gpt-oss / qwen models.
        assertEquals("openai/gpt-oss-120b", providers.getValue("groq").defaultModels.first())
    }

    @Test
    fun `hugging face uses the router endpoint`() {
        // api-inference.huggingface.co no longer resolves.
        assertTrue(
            providers.getValue("huggingface").modelsUrl!!
                .startsWith("https://router.huggingface.co"),
        )
    }

    @Test
    fun `ollama needs no key`() {
        assertFalse(providers.getValue("ollama").needsKey)
    }
}
