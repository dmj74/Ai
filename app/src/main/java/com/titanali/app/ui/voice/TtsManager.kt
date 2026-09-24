package com.titanali.app.ui.voice

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Reliable wrapper around Android's built-in (free) TextToSpeech engine.
 *
 * The engine initializes asynchronously. Voice chat can therefore receive an
 * answer before the engine is ready; the latest answer is queued instead of
 * being silently dropped. All callbacks are marshalled to the main thread so
 * Compose state and the microphone loop remain safe.
 */
class TtsManager(context: Context) {

    private data class Request(
        val text: String,
        val languageTag: String,
        val rate: Float,
        val onDone: () -> Unit,
    )

    private val appContext: Context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())

    private var tts: TextToSpeech? = null
    private var initRequested = false
    private var ready = false
    private var pending: Request? = null
    private var currentId: String? = null
    private var pendingOnDone: (() -> Unit)? = null

    var onReady: ((Boolean) -> Unit)? = null
    var onError: (() -> Unit)? = null
    var stateChanged: ((speaking: Boolean) -> Unit)? = null

    fun init() {
        if (initRequested) return
        initRequested = true
        tts = TextToSpeech(appContext) { status ->
            val ok = status == TextToSpeech.SUCCESS
            main.post {
                ready = ok
                if (ok) {
                    tts?.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    tts?.setOnUtteranceProgressListener(listener)
                }
                onReady?.invoke(ok)
                if (ok) {
                    pending?.also {
                        pending = null
                        speakNow(it)
                    }
                } else {
                    failPending()
                }
            }
        }
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            main.post {
                if (utteranceId == currentId) stateChanged?.invoke(true)
            }
        }

        override fun onDone(utteranceId: String?) = finish(utteranceId, failed = false)

        override fun onStop(utteranceId: String?, interrupted: Boolean) =
            finish(utteranceId, failed = false)

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = finish(utteranceId, failed = true)
    }

    private fun finish(utteranceId: String?, failed: Boolean) {
        main.post {
            if (utteranceId != currentId) return@post
            currentId = null
            stateChanged?.invoke(false)
            if (failed) onError?.invoke()
            val callback = pendingOnDone
            pendingOnDone = null
            callback?.invoke()
        }
    }

    /**
     * Speaks [text]. Markdown markers and URLs are removed so the result sounds
     * natural instead of reading formatting symbols aloud.
     */
    fun speak(text: String, languageTag: String, rate: Float, onDone: () -> Unit) {
        val clean = cleanForSpeech(text)
        if (clean.isBlank()) {
            main.post { onDone() }
            return
        }
        val request = Request(clean, languageTag, rate.coerceIn(0.5f, 2f), onDone)
        if (!ready || tts == null) {
            pending?.let { main.post { it.onDone() } }
            pending = request
            if (!initRequested) init()
            return
        }
        main.post { speakNow(request) }
    }

    private fun speakNow(request: Request) {
        val engine = tts ?: run {
            request.onDone()
            return
        }

        // A new answer always replaces an unfinished one, like a live voice
        // assistant rather than stacking several old replies in the queue.
        currentId = null
        pendingOnDone = null
        try {
            engine.stop()
        } catch (_: Exception) {
        }
        stateChanged?.invoke(false)

        val requested = Locale.forLanguageTag(request.languageTag)
        val languageOnly = Locale.forLanguageTag(request.languageTag.substringBefore("-"))
        val target = when {
            engine.isLanguageAvailable(requested) >= TextToSpeech.LANG_COUNTRY_AVAILABLE -> requested
            engine.isLanguageAvailable(languageOnly) >= TextToSpeech.LANG_AVAILABLE -> languageOnly
            else -> null
        }
        if (target == null) {
            onError?.invoke()
            request.onDone()
            return
        }

        val languageResult = engine.setLanguage(target)
        if (languageResult < TextToSpeech.LANG_AVAILABLE) {
            onError?.invoke()
            request.onDone()
            return
        }

        val id = "tts-${System.currentTimeMillis()}"
        currentId = id
        pendingOnDone = request.onDone
        engine.setSpeechRate(request.rate)
        stateChanged?.invoke(true)
        val result = engine.speak(request.text, TextToSpeech.QUEUE_FLUSH, null, id)
        if (result == TextToSpeech.ERROR) {
            finish(id, failed = true)
        }
    }

    fun stop() {
        pending = null
        pendingOnDone = null
        currentId = null
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        stateChanged?.invoke(false)
    }

    fun shutdown() {
        stop()
        ready = false
        initRequested = false
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
    }

    private fun failPending() {
        val request = pending
        pending = null
        if (request != null) {
            onError?.invoke()
            request.onDone()
        }
    }

    private fun cleanForSpeech(value: String): String = value
        .replace(Regex("(?s)```.*?```"), " ")
        .replace(Regex("https?://\\S+"), " ")
        .replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1")
        .replace(Regex("[*_`#]"), "")
        .replace(Regex("(?m)^\\s*[-•]\\s+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}
