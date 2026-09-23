package com.titanali.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE mode = :mode ORDER BY id ASC LIMIT 1")
    suspend fun firstByMode(mode: String): ConversationEntity?

    @Insert
    suspend fun insert(c: ConversationEntity): Long

    @Update
    suspend fun update(c: ConversationEntity)

    @Query("UPDATE conversations SET updatedAt = :ts WHERE id = :id")
    suspend fun touch(id: Long, ts: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE convId = :convId ORDER BY ts ASC, id ASC")
    fun observe(convId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE convId = :convId ORDER BY ts ASC, id ASC")
    suspend fun list(convId: Long): List<MessageEntity>

    @Insert
    suspend fun insert(m: MessageEntity): Long

    @Query("DELETE FROM messages WHERE convId = :convId")
    suspend fun deleteByConversation(convId: Long)
}

@Dao
interface VocabDao {

    @Query("SELECT * FROM vocab_progress")
    fun observeAll(): Flow<List<VocabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(v: VocabEntity)
}
