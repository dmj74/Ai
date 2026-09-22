package com.titanali.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.titanali.app.R
import com.titanali.app.data.learn.LanguagePack
import com.titanali.app.data.learn.Lesson
import com.titanali.app.data.learn.LessonData
import com.titanali.app.data.learn.VocabItem
import com.titanali.app.ui.components.ChatInputBar
import com.titanali.app.ui.components.MessageBubble
import com.titanali.app.vm.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(lang: String, lessonId: String) {
    val pack = LessonData.pack(lang)
    val lesson = remember(lang, lessonId) {
        pack.lessons.firstOrNull { it.id == lessonId }
    }
    if (lesson == null) return

    val vm: LearnViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var flipped by remember { mutableStateOf<String?>(null) }
    var showPractice by remember { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        lesson.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showPractice) {
                                showPractice = false
                            } else {
                                (context as? Activity)?.finish()
                            }
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (showPractice) {
            PracticePanel(
                vm = vm,
                state = state,
                pack = pack,
                level = lesson.level,
                input = input,
                onInputChange = { input = it },
                onBack = { showPractice = false },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        lesson.titleFa,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                stringResource(R.string.learn_grammar),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(lesson.grammar, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.learn_vocab),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(lesson.vocab.size) { index ->
                    val vocab = lesson.vocab[index]
                    val wordId = "${pack.code}:${lesson.id}:$index"
                    val isKnown = state.knownWords.contains(wordId)
                    VocabCard(
                        item = vocab,
                        flipped = flipped == wordId,
                        isKnown = isKnown,
                        onFlip = {
                            flipped = if (flipped == wordId) null else wordId
                        },
                        onToggleKnown = {
                            vm.toggleKnown(wordId, "${pack.code}:${lesson.id}", !isKnown)
                        },
                    )
                }
                item {
                    Button(
                        onClick = {
                            showPractice = true
                            vm.practiceStart(pack, lesson.level)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.learn_practice))
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabCard(
    item: VocabItem,
    flipped: Boolean,
    isKnown: Boolean,
    onFlip: () -> Unit,
    onToggleKnown: () -> Unit,
) {
    ElevatedCard(onClick = onFlip, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (flipped) item.meaning else item.word,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (isKnown) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            if (flipped) {
                Spacer(Modifier.height(6.dp))
                if (item.example.isNotBlank()) {
                    Text(item.example, style = MaterialTheme.typography.bodyMedium)
                }
                if (item.exampleFa.isNotBlank()) {
                    Text(
                        item.exampleFa,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onToggleKnown,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        if (isKnown) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.learn_known))
                }
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.learn_vocab_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PracticePanel(
    vm: LearnViewModel,
    state: LearnViewModel.LearnState,
    pack: LanguagePack,
    level: String,
    input: String,
    onInputChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.practiceMessages.size, state.practiceStreaming, state.practiceStream) {
        val count = state.practiceMessages.size +
            if (state.practiceStreaming) 1 else 0
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.learn_practice_back))
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.practiceMessages.isEmpty() && !state.practiceStreaming) {
                item {
                    Text(
                        stringResource(R.string.learn_practice_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                }
            }
            items(state.practiceMessages, key = { idx -> idx }) { m ->
                MessageBubble(role = m.role, content = m.content)
            }
            if (state.practiceStreaming && state.practiceStream.isNotEmpty()) {
                item {
                    MessageBubble(role = "assistant", content = state.practiceStream)
                }
            }
        }
        val error = state.practiceError
        if (error != null) {
            Text(
                text = if (error == "unknown") {
                    stringResource(R.string.unknown_error)
                } else {
                    error
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        ChatInputBar(
            text = input,
            onTextChange = onInputChange,
            onSend = {
                vm.practiceSend(input, pack, level)
                onInputChange("")
            },
            hint = stringResource(R.string.learn_practice_hint),
            onStop = if (state.practiceStreaming) ({ vm.practiceStop() }) else null,
        )
    }
}
