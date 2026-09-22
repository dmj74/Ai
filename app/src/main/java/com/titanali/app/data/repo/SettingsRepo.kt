package com.titanali.app.data.repo

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App settings, persisted in SharedPreferences and exposed as a StateFlow.
 */
class SettingsRepo(context: Context) {

    data class Settings(
        val provider: String = "groq",
        val apiKey: String = "",
        val model: String = "llama-3.3-70b-versatile",
        val ollamaHost: String = "http://10.0.2.2:11434",
        val deepAnalysis: Boolean = true,
        val titanaliVoice: Boolean = true,
        val ttsLanguage: String = "fa",
        val ttsRate: Float = 1.0f,
        val sttLanguage: String = "fa-IR",
        val uiLanguage: String = "fa",
    )

    private val prefs = context.getSharedPreferences("titanali_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    fun update(transform: (Settings) -> Settings) {
        val next = transform(_settings.value)
        _settings.value = next
        save(next)
    }

    private fun load(): Settings = Settings(
        provider = prefs.getString("provider", "groq") ?: "groq",
        apiKey = prefs.getString("apiKey", "") ?: "",
        model = prefs.getString("model", "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile",
        ollamaHost = prefs.getString("ollamaHost", "http://10.0.2.2:11434") ?: "http://10.0.2.2:11434",
        deepAnalysis = prefs.getBoolean("deepAnalysis", true),
        titanaliVoice = prefs.getBoolean("titanaliVoice", true),
        ttsLanguage = prefs.getString("ttsLanguage", "fa") ?: "fa",
        ttsRate = prefs.getFloat("ttsRate", 1.0f),
        sttLanguage = prefs.getString("sttLanguage", "fa-IR") ?: "fa-IR",
        uiLanguage = prefs.getString("uiLanguage", "fa") ?: "fa",
    )

    private fun save(s: Settings) {
        prefs.edit()
            .putString("provider", s.provider)
            .putString("apiKey", s.apiKey)
            .putString("model", s.model)
            .putString("ollamaHost", s.ollamaHost)
            .putBoolean("deepAnalysis", s.deepAnalysis)
            .putBoolean("titanaliVoice", s.titanaliVoice)
            .putString("ttsLanguage", s.ttsLanguage)
            .putFloat("ttsRate", s.ttsRate)
            .putString("sttLanguage", s.sttLanguage)
            .putString("uiLanguage", s.uiLanguage)
            .apply()
    }
}
