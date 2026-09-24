package com.titanali.app.data.repo

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App settings, persisted in SharedPreferences and exposed as a StateFlow.
 *
 * API keys and the chosen model are stored **per provider** (`key_<id>`,
 * `model_<id>`) so switching providers no longer loses anything. Legacy
 * single-key values (`apiKey`, `model`) are honoured as a migration fallback
 * for the provider that was selected when the app was upgraded.
 */
class SettingsRepo(context: Context) {

    data class Settings(
        val provider: String = "pollinations",
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

    /** The stored API key of [providerId] (empty when none was entered yet). */
    fun keyFor(providerId: String): String =
        prefs.getString("key_$providerId", null)?.takeIf { it.isNotBlank() }
            ?: legacyFor("apiKey", providerId)

    /** The stored model of [providerId], falling back to [default] of the provider. */
    fun modelFor(providerId: String, default: String): String =
        prefs.getString("model_$providerId", null)?.takeIf { it.isNotBlank() }
            ?: legacyFor("model", providerId).ifBlank { default }

    fun setKey(providerId: String, value: String) {
        prefs.edit().putString("key_$providerId", value.trim()).apply()
    }

    fun setModel(providerId: String, value: String) {
        prefs.edit().putString("model_$providerId", value.trim()).apply()
    }

    private fun legacyFor(prefName: String, providerId: String): String {
        val legacyProvider = prefs.getString("provider", "pollinations") ?: "pollinations"
        if (legacyProvider != providerId) return ""
        return prefs.getString(prefName, "") ?: ""
    }

    private fun load(): Settings = Settings(
        provider = prefs.getString("provider", "pollinations") ?: "pollinations",
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
