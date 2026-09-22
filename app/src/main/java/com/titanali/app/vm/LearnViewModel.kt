package com.titanali.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titanali.app.TitanaliApp
import com.titanali.app.ai.AiMessage
import com.titanali.app.ai.Prompts
import com.titanali.app.data.db.VocabEntity
import com.titanali.app.data.learn.LanguagePack
import com.titanali.app.data.learn.VocabItem
import com.titanali.app.data.repo.SettingsRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class LearnViewModel(app: Application) : AndroidViewModel(app) {

    private val tApp = app as TitanaliApp
    private val vocabDao = tApp.db.vocab()
    private val settings: SettingsRepo = tApp.settings

    data class PracticeMsg(
        val role: String,
        val content: String,
    )

    data class LearnState(
        val knownWords: Set<String> = emptySet(),
        val practiceMessages: List<PracticeMsg> = emptyList(),
        val practiceStream: String = "",
        val practiceStreaming: Boolean = false,
        val practiceError: String? = null,
    )

    private val _state = MutableStateFlow(LearnState())
    val state: StateFlow<LearnState> = _state.asStateFlow()

    private var practiceJob: Job? = null

    init {
        viewModelScope.launch {
            vocabDao.observeAll().collect { list ->
                _state.update {
                    it.copy(
                        knownWords = list.filter { v -> v.known }.map { v -> v.wordId }.toSet(),
                    )
                }
            }
        }
    }

    /** Deterministic "word of the day" from all vocab of the pack. */
    fun wordOfTheDay(pack: LanguagePack): VocabItem? {
        val all = pack.lessons.flatMap { it.vocab }
        if (all.isEmpty()) return null
        val day = LocalDate.now().dayOfYear
        return all[day % all.size]
    }

    fun toggleKnown(wordId: String, lessonKey: String, known: Boolean) {
        viewModelScope.launch {
            vocabDao.upsert(
                VocabEntity(wordId = wordId, lessonKey = lessonKey, known = known),
            )
        }
    }

    /** Starts a fresh practice session: the AI tutor greets the student. */
    fun practiceStart(pack: LanguagePack, level: String) {
        _state.update {
            it.copy(
                practiceMessages = emptyList(),
                practiceStream = "",
                practiceStreaming = false,
                practiceError = null,
            )
        }
        practiceSend(pack.starter, pack, level)
    }

    fun practiceSend(rawText: String, pack: LanguagePack, level: String) {
        val text = rawText.trim()
        if (text.isEmpty() || _state.value.practiceStreaming) return
        practiceJob?.cancel()
        val s = settings.settings.value
        val provider = tApp.provider(s.provider)
        val system = Prompts.languageTutor(pack.name, level)
        val history = _state.value.practiceMessages
        val messages = listOf(AiMessage("system", system)) +
            history.takeLast(12).map { AiMessage(it.role, it.content) } +
            AiMessage("user", text)
        _state.update {
            it.copy(
                practiceMessages = history + PracticeMsg("user", text),
                practiceStreaming = true,
                practiceStream = "",
                practiceError = null,
            )
        }
        val buffer = StringBuilder()
        practiceJob = viewModelScope.launch {
            try {
                provider.streamChat(messages, s.model, s.apiKey).collect { token ->
                    buffer.append(token)
                    _state.update { it.copy(practiceStream = buffer.toString()) }
                }
                val finalText = buffer.toString().trim()
                if (finalText.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            practiceMessages = it.practiceMessages + PracticeMsg("assistant", finalText),
                            practiceStream = "",
                        )
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _state.update {
                    it.copy(practiceError = if (msg.isBlank()) "unknown" else msg)
                }
            } finally {
                _state.update { it.copy(practiceStreaming = false) }
            }
        }
    }

    fun practiceStop() {
        practiceJob?.cancel()
    }

    fun clearPractice() {
        _state.update {
            it.copy(
                practiceMessages = emptyList(),
                practiceStream = "",
                practiceStreaming = false,
                practiceError = null,
            )
        }
    }
}
