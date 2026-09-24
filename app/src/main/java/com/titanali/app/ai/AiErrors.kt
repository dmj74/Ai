package com.titanali.app.ai

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Thrown when a provider answers with a non-2xx HTTP status. */
class AiHttpException(val status: Int, detail: String) :
    RuntimeException("HTTP $status $detail")

/** Thrown when a provider closes the stream without producing any text. */
class AiEmptyReplyException : RuntimeException("empty reply")

/**
 * Maps transport/HTTP failures to stable, localizable error keys.
 * The UI turns these keys into translated strings (see `R.string.err_*`);
 * an unmapped raw message may also be stored as-is as a last resort.
 */
object AiErrors {

    const val AUTH = "err_auth"
    const val MODEL = "err_model"
    const val RATE = "err_rate"
    const val SERVER = "err_server"
    const val NETWORK = "err_network"
    const val TIMEOUT = "err_timeout"
    const val EMPTY = "err_empty"
    const val UNKNOWN = "unknown"
    const val HTTP_PREFIX = "http:"

    fun keyFor(throwable: Throwable): String = when (throwable) {
        is AiHttpException -> keyForStatus(throwable.status)
        is AiEmptyReplyException -> EMPTY
        is SocketTimeoutException -> TIMEOUT
        is UnknownHostException -> NETWORK
        is IOException -> NETWORK
        else -> UNKNOWN
    }

    fun keyForStatus(status: Int): String = when (status) {
        401, 403 -> AUTH
        404 -> MODEL
        429 -> RATE
        in 500..599 -> SERVER
        else -> HTTP_PREFIX + status
    }
}
