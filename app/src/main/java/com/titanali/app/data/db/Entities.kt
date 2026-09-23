package com.titanali.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A conversation (chat).
 * mode: "general" | "titanali" | "learn_<lang>"
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val mode: String = "general",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "messages",
    indices = [Index(value = ["convId"])],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val convId: Long,
    val role: String, // "user" | "assistant"
    val content: String,
    val ts: Long = System.currentTimeMillis(),
)

/** Progress of vocabulary words learned in the language section. */
@Entity(tableName = "vocab_progress")
data class VocabEntity(
    @PrimaryKey val wordId: String,
    val lessonKey: String,
    val known: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)
