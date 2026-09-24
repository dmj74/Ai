package com.titanali.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titanali.app.TitanaliApp
import com.titanali.app.ai.AiErrors
import com.titanali.app.ai.AiMessage
import com.titanali.app.ai.Prompts
import com.titanali.app.data.repo.SettingsRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the separate analysis workspace. Normal chat deliberately never uses
 * the long analysis prompt; this screen is the explicit place for it.
 */
class AnalysisViewModel(app: Application) : AndroidViewModel(app) {

    data class State(
        val result: String = "",
        val streamText: String = "",
        val isAnalyzing: Boolean = false,
        val error: String? = null,
    )

    private val tApp = app as TitanaliApp
    private val settings: SettingsRepo = tApp.settings
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var analysisJob: Job? = null

    fun analyze(rawText: String) {
        val question = rawText.trim()
        if (question.isEmpty() || _state.value.isAnalyzing) return
        analysisJob?.cancel()
        val current = settings.settings.value
        val provider = tApp.provider(current.provider)
        val model = settings.modelFor(
            current.provider,
            provider.defaultModels.firstOrNull().orEmpty(),
        )
        val messages = listOf(
            AiMessage("system", Prompts.analysis()),
            AiMessage("user", question),
        )
        _state.value = State(isAnalyzing = true)
        val buffer = StringBuilder()
        analysisJob = viewModelScope.launch {
            try {
                provider.streamChat(
                    messages = messages,
                    model = model,
                    apiKey = settings.keyFor(current.provider),
                ).collect { token ->
                    buffer.append(token)
                    _state.update { it.copy(streamText = buffer.toString()) }
                }
                val answer = buffer.toString().trim()
                _state.update { it.copy(result = answer) }
            } catch (e: CancellationException) {
                val partial = buffer.toString().trim()
                if (partial.isNotEmpty()) {
                    _state.update { it.copy(result = partial) }
                }
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = AiErrors.keyFor(e)) }
            } finally {
                _state.update { it.copy(isAnalyzing = false, streamText = "") }
            }
        }
    }

    fun stop() {
        analysisJob?.cancel()
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun clear() {
        analysisJob?.cancel()
        _state.value = State()
    }
}
