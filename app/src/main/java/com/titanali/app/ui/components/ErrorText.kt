package com.titanali.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.titanali.app.R
import com.titanali.app.ai.AiErrors

/**
 * Turns a stable error key produced by [AiErrors.keyFor] into a localized,
 * human-friendly message. Unknown keys are shown verbatim as a last resort.
 */
@Composable
fun aiErrorText(key: String): String = when (key) {
    AiErrors.AUTH -> stringResource(R.string.err_auth)
    AiErrors.MODEL -> stringResource(R.string.err_model)
    AiErrors.RATE -> stringResource(R.string.err_rate)
    AiErrors.SERVER -> stringResource(R.string.err_server)
    AiErrors.NETWORK -> stringResource(R.string.err_network)
    AiErrors.TIMEOUT -> stringResource(R.string.err_timeout)
    AiErrors.UNKNOWN -> stringResource(R.string.unknown_error)
    else -> if (key.startsWith(AiErrors.HTTP_PREFIX)) {
        stringResource(
            R.string.err_http,
            key.removePrefix(AiErrors.HTTP_PREFIX).toIntOrNull() ?: 0,
        )
    } else {
        key
    }
}
