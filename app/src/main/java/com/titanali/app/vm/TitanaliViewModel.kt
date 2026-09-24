package com.titanali.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titanali.app.TitanaliApp
import com.titanali.app.ai.AiErrors
import com.titanali.app.ai.AiMessage
import com.titanali.app.ai.Prompts
import com.titanali.app.data.db.ConversationEntity
import com.titanali.app.data.db.MessageEntity
import com.titanali.app.data.repo.SettingsRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TitanaliViewModel(app: Application) : AndroidViewModel(app) {

    enum class VoiceState { Idle, Listening, Thinking, Speaking }

    data class TitanaliState(
        val messages: List<ChatViewModel.UiMessage> = emptyList(),
        val streamText: String = "",
        val isStreaming: Boolean = false,
        val error: String? = null,
        val voiceState: VoiceState = VoiceState.Idle,
        val partialText: String = "",
        val deepAnalysis: Boolean = true,
        val voiceEnabled: Boolean = true,
    )

    private val tApp = app as TitanaliApp
    private val convDao = tApp.db.conversations()
    private val msgDao = tApp.db.messages()
    private val settings: SettingsRepo = tApp.settings

    private val _state = MutableStateFlow(TitanaliState())
    val state: StateFlow<TitanaliState> = _state.asStateFlow()

    /** Emitted with the final assistant text that should be spoken aloud. */
    private val _speak = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val speak: SharedFlow<String> = _speak.asSharedFlow()

    private var convId: Long = 0L
    private var sendJob: Job? = null

    init {
        val s = settings.settings.value
        _state.update {
            it.copy(deepAnalysis = s.deepAnalysis, voiceEnabled = s.titanaliVoice)
        }
        viewModelScope.launch {
            settings.settings.collect { st ->
                _state.update {
                    it.copy(deepAnalysis = st.deepAnalysis, voiceEnabled = st.titanaliVoice)
                }
            }
        }
        viewModelScope.launch {
            val existing = convDao.firstByMode("titanali")
            convId = existing?.id ?: convDao.insert(
                ConversationEntity(title = "", mode = "titanali"),
            )
            msgDao.observe(convId).collect { list ->
                _state.update { st ->
                    st.copy(
                        messages = list.map { m ->
                            ChatViewModel.UiMessage(m.id, m.role, m.content)
                        },
                    )
                }
            }
        }
    }

    fun refreshFromSettings() {
        val s = settings.settings.value
        _state.update {
            it.copy(deepAnalysis = s.deepAnalysis, voiceEnabled = s.titanaliVoice)
        }
    }

    fun setVoiceState(newState: VoiceState) {
        _state.update { it.copy(voiceState = newState) }
    }

    fun setPartial(text: String) {
        _state.update { it.copy(partialText = text) }
    }

    fun send(rawText: String, voiceMode: Boolean) {
        val text = rawText.trim()
        if (text.isEmpty() || _state.value.isStreaming) return
        sendJob?.cancel()
        val s = settings.settings.value
        val provider = tApp.provider(s.provider)
        val model = settings.modelFor(s.provider, provider.defaultModels.firstOrNull().orEmpty())
        // Titanali is a conversational character; long analysis is available
        // in the dedicated Analysis tab and is not forced into chat/voice.
        val system = Prompts.titanali(false, voiceMode = voiceMode)
        sendJob = viewModelScope.launch {
            if (convId == 0L) {
                convId = convDao.insert(
                    ConversationEntity(title = "", mode = "titanali"),
                )
            }
            msgDao.insert(MessageEntity(convId = convId, role = "user", content = text))
            convDao.touch(convId, System.currentTimeMillis())
            val history = msgDao.list(convId).takeLast(12)
            val messages = listOf(AiMessage("system", system)) +
                history.map { AiMessage(it.role, it.content) }
            _state.update {
                it.copy(
                    isStreaming = true,
                    streamText = "",
                    error = null,
                    voiceState = VoiceState.Thinking,
                )
            }
            val buffer = StringBuilder()
            try {
                provider.streamChat(messages, model, settings.keyFor(s.provider))
                    .collect { token ->
                        buffer.append(token)
                        _state.update { it.copy(streamText = buffer.toString()) }
                    }
                val finalText = buffer.toString().trim()
                if (finalText.isNotEmpty()) {
                    msgDao.insert(
                        MessageEntity(convId = convId, role = "assistant", content = finalText),
                    )
                    // Speak in voice mode, or whenever "Titanali reads answers aloud" is on.
                    if (voiceMode || s.titanaliVoice) {
                        _speak.tryEmit(finalText)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = AiErrors.keyFor(e)) }
            } finally {
                _state.update {
                    it.copy(
                        isStreaming = false,
                        streamText = "",
                        voiceState = VoiceState.Idle,
                        partialText = "",
                    )
                }
            }
        }
    }

    fun stop() {
        sendJob?.cancel()
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
