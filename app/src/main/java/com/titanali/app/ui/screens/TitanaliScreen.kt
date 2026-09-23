package com.titanali.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.titanali.app.R
import com.titanali.app.TitanaliApp
import com.titanali.app.ui.components.ChatInputBar
import com.titanali.app.ui.components.MessageBubble
import com.titanali.app.ui.components.aiErrorText
import com.titanali.app.ui.voice.ListenerManager
import com.titanali.app.ui.voice.TtsManager
import com.titanali.app.vm.TitanaliViewModel
import com.titanali.app.vm.TitanaliViewModel.VoiceState
import kotlinx.coroutines.launch

private const val MAX_SILENCE_RETRIES = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitanaliScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as TitanaliApp
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val s by app.settings.settings.collectAsStateWithLifecycle()
    val vm: TitanaliViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var voiceMode by remember { mutableStateOf(false) }
    var voiceActive by remember { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }
    var ttsAvailable by remember { mutableStateOf(true) }
    var silenceRetries by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    val tts = remember { TtsManager(context) }
    val listener = remember { ListenerManager(context) }
    val avatar = painterResource(R.drawable.titanali_avatar)

    fun notice(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    fun startVoice() {
        voiceActive = true
        silenceRetries = 0
        vm.setPartial("")
        listener.onPartial = { vm.setPartial(it) }
        listener.onFinal = { text ->
            silenceRetries = 0
            if (text.isNotBlank()) vm.send(text, voiceMode = true)
        }
        listener.onFailed = { code ->
            when (code) {
                "no_speech" -> {
                    if (voiceActive && silenceRetries < MAX_SILENCE_RETRIES) {
                        silenceRetries += 1
                        listener.start(s.sttLanguage)
                    } else if (voiceActive) {
                        voiceActive = false
                        vm.setVoiceState(VoiceState.Idle)
                        notice(context.getString(R.string.voice_no_speech))
                    }
                }
                "permission" -> {
                    voiceActive = false
                    vm.setVoiceState(VoiceState.Idle)
                    notice(context.getString(R.string.voice_mic_permission))
                }
                "unavailable" -> {
                    voiceActive = false
                    voiceMode = false
                    vm.setVoiceState(VoiceState.Idle)
                    notice(context.getString(R.string.voice_unavailable))
                }
                else -> {
                    if (voiceActive) {
                        listener.start(s.sttLanguage)
                    }
                }
            }
        }
        listener.stateChanged = { active ->
            if (active && voiceActive) {
                vm.setVoiceState(VoiceState.Listening)
            }
        }
        listener.start(s.sttLanguage)
    }

    fun stopVoice() {
        voiceActive = false
        listener.cancel()
        tts.stop()
        vm.setPartial("")
        vm.setVoiceState(VoiceState.Idle)
    }

    val permLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startVoice()
            } else {
                vm.setVoiceState(VoiceState.Idle)
                notice(context.getString(R.string.voice_mic_permission))
            }
        }

    DisposableEffect(Unit) {
        tts.onReady = { ok ->
            if (!ok) ttsAvailable = false
        }
        tts.init()
        tts.stateChanged = { speaking ->
            if (voiceActive) {
                vm.setVoiceState(if (speaking) VoiceState.Speaking else VoiceState.Idle)
            }
        }
        onDispose {
            tts.shutdown()
            listener.release()
        }
    }

    LaunchedEffect(Unit) {
        vm.speak.collect { text ->
            tts.speak(text, s.ttsLanguage, s.ttsRate, onDone = {
                if (voiceActive) {
                    vm.setVoiceState(VoiceState.Idle)
                    listener.start(s.sttLanguage)
                }
            })
        }
    }

    LaunchedEffect(state.messages.size, state.isStreaming, state.streamText) {
        val count = state.messages.size + if (state.isStreaming) 1 else 0
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titanali_name)) },
                actions = {
                    if (state.error != null) {
                        IconButton(onClick = { vm.dismissError() }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val pulsing = state.voiceState == VoiceState.Listening ||
                    state.voiceState == VoiceState.Thinking ||
                    state.voiceState == VoiceState.Speaking
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (pulsing) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val ringAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.45f,
                            targetValue = 0.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(850),
                                repeatMode = RepeatMode.Reverse,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha),
                                ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = avatar,
                            contentDescription = stringResource(R.string.titanali_name),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        stringResource(R.string.titanali_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        when (state.voiceState) {
                            VoiceState.Listening -> stringResource(R.string.voice_listening)
                            VoiceState.Thinking -> stringResource(R.string.voice_thinking)
                            VoiceState.Speaking -> stringResource(R.string.voice_speaking)
                            VoiceState.Idle -> stringResource(R.string.titanali_ready)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !voiceMode,
                    onClick = {
                        voiceMode = false
                        if (voiceActive) stopVoice()
                    },
                    label = { Text(stringResource(R.string.mode_text)) },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = voiceMode,
                    enabled = ttsAvailable && listener.available,
                    onClick = {
                        voiceMode = true
                        if (!voiceActive) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO,
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                startVoice()
                            } else {
                                permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.mode_voice)) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (state.error != null) {
                Text(
                    text = aiErrorText(state.error!!),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            Column(Modifier
                .weight(1f)
                .fillMaxWidth()) {
                if (voiceMode && voiceActive) {
                    VoicePanel(
                        state = state,
                        onCancel = { stopVoice() },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.messages, key = { it.id }) { m ->
                            MessageBubble(
                                role = m.role,
                                content = m.content,
                                assistantAvatar = avatar,
                            )
                        }
                        if (state.isStreaming && state.streamText.isNotEmpty()) {
                            item {
                                MessageBubble(
                                    role = "assistant",
                                    content = state.streamText,
                                    assistantAvatar = avatar,
                                )
                            }
                        }
                    }
                }
                if (!(voiceMode && voiceActive)) {
                    ChatInputBar(
                        text = input,
                        onTextChange = { input = it },
                        onSend = {
                            vm.send(input, voiceMode = false)
                            input = ""
                        },
                        hint = stringResource(R.string.titanali_hint),
                        onStop = if (state.isStreaming) ({ vm.stop() }) else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun VoicePanel(
    state: TitanaliViewModel.TitanaliState,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastAssistant = state.messages.lastOrNull { it.role == "assistant" }?.content
        ?: state.streamText
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (state.voiceState) {
            VoiceState.Listening -> {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.voice_listening),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (state.partialText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.partialText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            VoiceState.Thinking -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.voice_thinking),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            VoiceState.Speaking -> {
                Icon(
                    Icons.Filled.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.voice_speaking),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (lastAssistant.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        lastAssistant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            VoiceState.Idle -> {
                Text(
                    stringResource(R.string.titanali_ready),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        TextButton(onClick = onCancel) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.voice_cancel))
        }
    }
}
