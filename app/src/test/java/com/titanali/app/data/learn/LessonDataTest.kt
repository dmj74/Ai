package com.titanali.app.data.learn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonDataTest {

    @Test
    fun `three language packs exist`() {
        assertEquals(listOf("en", "de", "ar"), LessonData.languages.map { it.code })
    }

    @Test
    fun `every pack has lessons with unique ids and vocabulary`() {
        LessonData.languages.forEach { pack ->
            assertTrue("${pack.code} has lessons", pack.lessons.isNotEmpty())
            val ids = pack.lessons.map { it.id }
            assertEquals("${pack.code} lesson ids unique", ids.toSet().size, ids.size)
            pack.lessons.forEach { lesson ->
                assertTrue("${lesson.id} has vocab", lesson.vocab.isNotEmpty())
                assertTrue("${lesson.id} has grammar notes", lesson.grammar.isNotBlank())
                assertTrue("${lesson.id} has a level", lesson.level.isNotBlank())
            }
            assertTrue("${pack.code} has a starter prompt", pack.starter.isNotBlank())
        }
    }

    @Test
    fun `pack lookup falls back to the first language`() {
        assertEquals(LessonData.languages.first(), LessonData.pack("zz"))
        assertEquals("de", LessonData.pack("de").code)
    }
}
