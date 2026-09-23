package com.titanali.app.ui.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Thin wrapper around Android's built-in (free) TextToSpeech engine.
 * Used by Titanali to read answers aloud in voice conversation.
 *
 * UtteranceProgressListener callbacks arrive on a binder thread, so every
 * callback is re-posted to the main thread before touching app state.
 */
class TtsManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())

    private var tts: TextToSpeech? = null
    private var currentId: String? = null
    private var pendingOnDone: (() -> Unit)? = null

    var onReady: ((Boolean) -> Unit)? = null
    var stateChanged: ((speaking: Boolean) -> Unit)? = null

    fun init() {
        if (tts != null) return
        tts = TextToSpeech(appContext) { status ->
            val ok = status == TextToSpeech.SUCCESS
            if (ok) {
                tts?.setOnUtteranceProgressListener(listener)
            }
            main.post { onReady?.invoke(ok) }
        }
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {}

        override fun onDone(utteranceId: String?) = finish(utteranceId)

        override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = finish(utteranceId)

        private fun finish(utteranceId: String?) {
            main.post {
                if (utteranceId == currentId) {
                    currentId = null
                    stateChanged?.invoke(false)
                    val cb = pendingOnDone
                    pendingOnDone = null
                    cb?.invoke()
                }
            }
        }
    }

    /** Speaks [text]; [onDone] is called when the utterance finishes (or on stop/error). */
    fun speak(text: String, languageTag: String, rate: Float, onDone: () -> Unit) {
        val engine = tts
        if (engine == null) {
            onDone()
            return
        }
        engine.setSpeechRate(rate)
        val locale = Locale.forLanguageTag(languageTag)
        val fallback = Locale.forLanguageTag(languageTag.substringBefore("-"))
        val target = when {
            engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_COUNTRY_AVAILABLE -> locale
            engine.isLanguageAvailable(fallback) >= TextToSpeech.LANG_AVAILABLE -> fallback
            else -> locale
        }
        engine.setLanguage(target)
        currentId = "tts-${System.currentTimeMillis()}"
        pendingOnDone = onDone
        stateChanged?.invoke(true)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }
}
