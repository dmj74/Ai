package com.titanali.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titanali.app.TitanaliApp
import com.titanali.app.ai.AiMessage
import com.titanali.app.ai.Prompts
import com.titanali.app.data.db.ConversationEntity
import com.titanali.app.data.db.MessageEntity
import com.titanali.app.data.repo.SettingsRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val tApp = app as TitanaliApp
    private val convDao = tApp.db.conversations()
    private val msgDao = tApp.db.messages()
    private val settings: SettingsRepo = tApp.settings

    data class UiMessage(
        val id: Long,
        val role: String,
        val content: String,
    )

    data class ChatState(
        val title: String = "",
        val mode: String = "general",
        val messages: List<UiMessage> = emptyList(),
        val streamText: String = "",
        val isStreaming: Boolean = false,
        val error: String? = null,
        val models: List<String> = emptyList(),
        val selectedModel: String = "",
        val deepAnalysis: Boolean = true,
    )

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var sendJob: Job? = null
    private var observeJob: Job? = null
    private var currentConvId: Long = 0L

    fun openConversation(id: Long) {
        if (id == currentConvId) return
        currentConvId = id
        sendJob?.cancel()
        observeJob?.cancel()
        val s = settings.settings.value
        _state.update {
            it.copy(
                deepAnalysis = s.deepAnalysis,
                selectedModel = s.model,
            )
        }
        viewModelScope.launch {
            val conv = convDao.getById(id)
            _state.update { st ->
                st.copy(title = conv?.title.orEmpty(), mode = conv?.mode ?: "general")
            }
        }
        observeJob = viewModelScope.launch {
            msgDao.observe(id).collect { list ->
                _state.update { st ->
                    st.copy(messages = list.map { m -> UiMessage(m.id, m.role, m.content) })
                }
            }
        }
        loadModels()
    }

    private fun loadModels() {
        val s = settings.settings.value
        viewModelScope.launch {
            val provider = tApp.provider(s.provider)
            val models = try {
                provider.listModels(s.apiKey)
            } catch (e: Exception) {
                provider.defaultModels
            }
            _state.update { it.copy(models = models.ifEmpty { provider.defaultModels }) }
        }
    }

    fun selectModel(model: String) {
        _state.update { it.copy(selectedModel = model) }
        settings.update { it.copy(model = model) }
    }

    fun send(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty() || _state.value.isStreaming) return
        sendJob?.cancel()
        val s = settings.settings.value
        val provider = tApp.provider(s.provider)
        val systemPrompt = Prompts.general(s.deepAnalysis)
        sendJob = viewModelScope.launch {
            var convId = currentConvId
            if (convId == 0L) {
                val newId = convDao.insert(
                    ConversationEntity(title = text.take(42), mode = "general"),
                )
                convId = newId
                currentConvId = newId
                _state.update { it.copy(title = text.take(42)) }
            }
            val before = msgDao.list(convId).size
            msgDao.insert(MessageEntity(convId = convId, role = "user", content = text))
            if (before == 0) {
                convDao.getById(convId)?.let {
                    if (it.title.isBlank()) {
                        convDao.update(it.copy(title = text.take(42)))
                    }
                }
            }
            convDao.touch(convId, System.currentTimeMillis())
            val history = msgDao.list(convId).takeLast(20)
            val messages = listOf(AiMessage("system", systemPrompt)) +
                history.map { AiMessage(it.role, it.content) }
            _state.update { it.copy(isStreaming = true, streamText = "", error = null) }
            val buffer = StringBuilder()
            try {
                provider.streamChat(messages, s.model, s.apiKey).collect { token ->
                    buffer.append(token)
                    _state.update { it.copy(streamText = buffer.toString()) }
                }
                val finalText = buffer.toString().trim()
                if (finalText.isNotEmpty()) {
                    msgDao.insert(
                        MessageEntity(convId = convId, role = "assistant", content = finalText),
                    )
                }
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _state.update { it.copy(error = if (msg.isBlank()) "unknown" else msg) }
                val partial = buffer.toString().trim()
                if (partial.isNotEmpty()) {
                    msgDao.insert(
                        MessageEntity(convId = convId, role = "assistant", content = partial),
                    )
                }
            } finally {
                _state.update { it.copy(isStreaming = false, streamText = "") }
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
