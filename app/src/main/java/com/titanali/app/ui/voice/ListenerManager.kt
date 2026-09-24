package com.titanali.app.ui.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Wraps Android's SpeechRecognizer (free, on-device / Google) for listening.
 * Requires RECORD_AUDIO permission at runtime.
 *
 * Failure codes emitted through [onFailed]:
 *  - "no_speech"  : nothing heard / no match (safe to retry)
 *  - "permission" : microphone permission missing
 *  - "unavailable": no recognizer service on this device
 *  - "error_<n>"  : any other SpeechRecognizer error code
 */
class ListenerManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private var recognizer: SpeechRecognizer? = null

    val available: Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    var onPartial: ((String) -> Unit)? = null
    var onFinal: ((String) -> Unit)? = null
    var onFailed: ((String) -> Unit)? = null
    var stateChanged: ((listening: Boolean) -> Unit)? = null

    fun start(languageTag: String) {
        if (!available) {
            onFailed?.invoke("unavailable")
            return
        }
        try {
            val r = recognizer ?: SpeechRecognizer.createSpeechRecognizer(appContext).also {
                recognizer = it
                it.setRecognitionListener(listener)
            }
            r.startListening(recognizeIntent(languageTag))
            stateChanged?.invoke(true)
        } catch (e: Exception) {
            stateChanged?.invoke(false)
            onFailed?.invoke(e.message ?: "start_failed")
        }
    }

    fun cancel() {
        try {
            recognizer?.cancel()
        } catch (_: Exception) {
        }
        stateChanged?.invoke(false)
    }

    fun release() {
        try {
            recognizer?.destroy()
        } catch (_: Exception) {
        }
        recognizer = null
    }

    private fun recognizeIntent(languageTag: String): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
        }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            stateChanged?.invoke(false)
            onFailed?.invoke(
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                    SpeechRecognizer.ERROR_AUDIO,
                    -> "no_speech"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "permission"
                    else -> "error_$error"
                },
            )
        }

        override fun onResults(results: Bundle?) {
            stateChanged?.invoke(false)
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = list?.firstOrNull()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                onFinal?.invoke(text)
            } else {
                onFailed?.invoke("no_speech")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            list?.firstOrNull()?.let { onPartial?.invoke(it) }
        }

        @Deprecated("Deprecated in Java")
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
