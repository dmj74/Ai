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

    private var observedProvider: String? = null

    init {
        // React to provider/model changes made in Settings while the chat is open.
        viewModelScope.launch {
            settings.settings.collect { s ->
                val provider = tApp.provider(s.provider)
                val stored = settings.modelFor(s.provider, provider.defaultModels.firstOrNull().orEmpty())
                _state.update { st ->
                    if (st.selectedModel != stored) {
                        st.copy(deepAnalysis = s.deepAnalysis, selectedModel = stored)
                    } else {
                        st.copy(deepAnalysis = s.deepAnalysis)
                    }
                }
                if (observedProvider != s.provider) {
                    observedProvider = s.provider
                    loadModels()
                }
            }
        }
    }

    fun openConversation(id: Long) {
        if (id == currentConvId) return
        currentConvId = id
        sendJob?.cancel()
        observeJob?.cancel()
        val s = settings.settings.value
        val provider = tApp.provider(s.provider)
        _state.update {
            it.copy(
                deepAnalysis = s.deepAnalysis,
                selectedModel = settings.modelFor(
                    s.provider,
                    provider.defaultModels.firstOrNull().orEmpty(),
                ),
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
                provider.listModels(settings.keyFor(s.provider))
            } catch (e: Exception) {
                provider.defaultModels
            }
            _state.update { it.copy(models = models.ifEmpty { provider.defaultModels }) }
        }
    }

    fun selectModel(model: String) {
        _state.update { it.copy(selectedModel = model) }
        settings.setModel(settings.settings.value.provider, model)
    }

    fun send(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty() || _state.value.isStreaming) return
        sendJob?.cancel()
        val s = settings.settings.value
        val provider = tApp.provider(s.provider)
        // Normal chat stays concise; detailed analysis belongs to the
        // dedicated Analysis tab instead of being injected into every reply.
        val systemPrompt = Prompts.general(false)
        val model = _state.value.selectedModel.ifBlank {
            settings.modelFor(s.provider, provider.defaultModels.firstOrNull().orEmpty())
        }
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
                }
            } catch (e: CancellationException) {
                // User pressed stop: keep whatever already arrived, no error banner.
                val partial = buffer.toString().trim()
                if (partial.isNotEmpty()) {
                    msgDao.insert(
                        MessageEntity(convId = convId, role = "assistant", content = partial),
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = AiErrors.keyFor(e)) }
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
