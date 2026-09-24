package com.titanali.app.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptsTest {

    @Test
    fun `analysis prompt is the only standalone deep-analysis prompt`() {
        assertTrue(Prompts.analysis().contains("آنالیز کامل"))
        assertFalse(Prompts.general(false).contains("آنالیز کامل"))
    }

    @Test
    fun `general prompt respects the deep analysis switch`() {
        assertTrue(Prompts.general(true).contains("آنالیز کامل"))
        assertFalse(Prompts.general(false).contains("آنالیز کامل"))
    }

    @Test
    fun `titanali persona is always present`() {
        assertTrue(Prompts.titanali(true).contains("تیتانلی"))
        assertTrue(Prompts.titanali(false, voiceMode = true).contains("تیتانلی"))
    }

    @Test
    fun `voice mode asks for short speakable answers`() {
        val voice = Prompts.titanali(true, voiceMode = true)
        assertTrue(voice.contains("مکالمهٔ صوتی"))
        assertFalse(voice.contains("آنالیز کامل"))
    }

    @Test
    fun `language tutor prompt mentions language and level`() {
        val prompt = Prompts.languageTutor("English", "A1")
        assertTrue(prompt.contains("English"))
        assertTrue(prompt.contains("A1"))
        assertTrue(prompt.contains("اصلاح"))
    }
}
