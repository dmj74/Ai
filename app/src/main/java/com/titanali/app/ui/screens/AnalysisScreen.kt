package com.titanali.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.titanali.app.R
import com.titanali.app.TitanaliApp
import com.titanali.app.ui.components.ChatInputBar
import com.titanali.app.ui.components.aiErrorText
import com.titanali.app.vm.AnalysisViewModel

/** The only screen that asks the model for long, structured analysis. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen() {
    val vm: AnalysisViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val app = LocalContext.current.applicationContext as TitanaliApp
    val settings by app.settings.settings.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val provider = app.provider(settings.provider)
    val needKey = provider.needsKey && app.settings.keyFor(settings.provider).isBlank()

    DisposableEffect(Unit) {
        onDispose { vm.stop() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analysis_title)) },
                actions = {
                    IconButton(
                        onClick = vm::clear,
                        enabled = !state.isAnalyzing && (state.result.isNotBlank() || state.error != null),
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.analysis_clear),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.analysis_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (needKey) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.need_ai_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            if (state.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        aiErrorText(state.error!!),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.result.isBlank() && !state.isAnalyzing) {
                    item {
                        Text(
                            stringResource(R.string.analysis_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }
                if (state.isAnalyzing || state.streamText.isNotBlank()) {
                    item {
                        AnalysisCard(
                            text = state.streamText.ifBlank { stringResource(R.string.analysis_running) },
                            streaming = true,
                        )
                    }
                } else if (state.result.isNotBlank()) {
                    item { AnalysisCard(text = state.result, streaming = false) }
                }
            }

            ChatInputBar(
                text = input,
                onTextChange = { input = it },
                onSend = {
                    vm.analyze(input)
                    input = ""
                },
                hint = stringResource(R.string.analysis_hint),
                onStop = if (state.isAnalyzing) vm::stop else null,
            )
        }
    }
}

@Composable
private fun AnalysisCard(text: String, streaming: Boolean) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.analysis_result),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (streaming) {
                    Spacer(Modifier.weight(1f))
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            SelectionContainer { Text(text, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
