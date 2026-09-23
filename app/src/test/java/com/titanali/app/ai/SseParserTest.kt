package com.titanali.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SseParserTest {

    @Test
    fun `dataPayload extracts trimmed payload`() {
        assertEquals("{\"a\":1}", SseParser.dataPayload("data: {\"a\":1}"))
        assertEquals("{\"a\":1}", SseParser.dataPayload("data:{\"a\":1}"))
        assertNull(SseParser.dataPayload(": comment"))
        assertNull(SseParser.dataPayload("event: message"))
        assertNull(SseParser.dataPayload("data:"))
    }

    @Test
    fun `isDone detects stream terminator`() {
        assertTrue(SseParser.isDone("[DONE]"))
        assertFalse(SseParser.isDone("{\"choices\":[]}"))
    }

    @Test
    fun `openAiDelta reads delta content`() {
        val chunk = """{"id":"x","choices":[{"delta":{"content":"سلام"}}]}"""
        assertEquals("سلام", SseParser.openAiDelta(chunk))
    }

    @Test
    fun `openAiDelta ignores empty and malformed chunks`() {
        assertNull(SseParser.openAiDelta("""{"choices":[{"delta":{}}]}"""))
        assertNull(SseParser.openAiDelta("not json"))
    }

    @Test
    fun `geminiText reads candidate text`() {
        val chunk = """{"candidates":[{"content":{"parts":[{"text":"hello"}],"role":"model"}}]}"""
        assertEquals("hello", SseParser.geminiText(chunk))
        assertNull(SseParser.geminiText("""{"candidates":[]}"""))
    }

    @Test
    fun `ollama lines parse text and done flag`() {
        assertEquals("hi", SseParser.ollamaText("""{"message":{"role":"assistant","content":"hi"},"done":false}"""))
        assertTrue(SseParser.ollamaDone("""{"message":{"role":"assistant","content":""},"done":true}"""))
        assertFalse(SseParser.ollamaDone("""{"message":{"role":"assistant","content":"hi"},"done":false}"""))
        assertNull(SseParser.ollamaText("garbage"))
    }
}
