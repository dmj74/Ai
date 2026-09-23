package com.titanali.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titanali.app.TitanaliApp
import com.titanali.app.ai.AiErrors
import com.titanali.app.ai.AiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Backs the Settings screen: live model list of the selected provider
 * and a "test connection" probe that sends one tiny chat request.
 */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    sealed interface TestState {
        object Idle : TestState
        object Running : TestState
        object Ok : TestState
        data class Failed(val errorKey: String) : TestState
    }

    data class State(
        val models: List<String> = emptyList(),
        val loadingModels: Boolean = false,
        val test: TestState = TestState.Idle,
    )

    private val tApp = app as TitanaliApp

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var modelsJob: Job? = null
    private var testJob: Job? = null

    /** Loads the model list of [providerId] from the network (falls back to defaults). */
    fun refreshModels(providerId: String) {
        modelsJob?.cancel()
        val provider = tApp.provider(providerId)
        val key = tApp.settings.keyFor(providerId)
        _state.update { it.copy(loadingModels = true, models = provider.defaultModels) }
        modelsJob = viewModelScope.launch {
            val models = try {
                provider.listModels(key).ifEmpty { provider.defaultModels }
            } catch (e: Exception) {
                provider.defaultModels
            }
            _state.update { it.copy(models = models, loadingModels = false) }
        }
    }

    /** Sends a minimal prompt to verify key + model + connectivity. */
    fun testConnection(providerId: String, model: String) {
        testJob?.cancel()
        val provider = tApp.provider(providerId)
        val key = tApp.settings.keyFor(providerId)
        _state.update { it.copy(test = TestState.Running) }
        testJob = viewModelScope.launch {
            val result = withTimeoutOrNull(45_000L) {
                runCatching {
                    provider.streamChat(
                        messages = listOf(AiMessage("user", "ping")),
                        model = model,
                        apiKey = key,
                    ).first()
                }
            }
            _state.update {
                it.copy(
                    test = when {
                        result == null -> TestState.Failed(AiErrors.TIMEOUT)
                        result.isSuccess -> TestState.Ok
                        else -> TestState.Failed(AiErrors.keyFor(result.exceptionOrNull() ?: RuntimeException()))
                    },
                )
            }
        }
    }

    fun clearTest() {
        _state.update { it.copy(test = TestState.Idle) }
    }
}
