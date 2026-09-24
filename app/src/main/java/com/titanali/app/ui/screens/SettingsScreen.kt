package com.titanali.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.titanali.app.BuildConfig
import com.titanali.app.R
import com.titanali.app.TitanaliApp
import com.titanali.app.ui.components.aiErrorText
import com.titanali.app.vm.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val app = LocalContext.current.applicationContext as TitanaliApp
    val context = LocalContext.current
    val vm: SettingsViewModel = viewModel()
    val vmState by vm.state.collectAsStateWithLifecycle()
    val s by app.settings.settings.collectAsStateWithLifecycle()
    var keyVisible by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    val currentProvider = app.providers[s.provider]
    var keyText by rememberSaveable { mutableStateOf("") }
    var modelText by rememberSaveable { mutableStateOf("") }
    val models = vmState.models.ifEmpty { currentProvider?.defaultModels ?: emptyList() }

    LaunchedEffect(s.provider) {
        keyText = app.settings.keyFor(s.provider)
        modelText = app.settings.modelFor(
            s.provider,
            currentProvider?.defaultModels?.firstOrNull().orEmpty(),
        )
        vm.refreshModels(s.provider)
        vm.clearTest()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SectionTitle(stringResource(R.string.settings_ai_title)) }

            item {
                Column {
                    Text(
                        stringResource(R.string.settings_provider),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    app.providers.values.toList().chunked(2).forEach { rowProviders ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowProviders.forEach { p ->
                                FilterChip(
                                    selected = s.provider == p.id,
                                    onClick = {
                                        app.settings.update { cur ->
                                            cur.copy(provider = p.id)
                                        }
                                        if (app.settings.modelFor(
                                                p.id,
                                                "",
                                            ).isBlank()
                                        ) {
                                            app.settings.setModel(
                                                p.id,
                                                p.defaultModels.firstOrNull().orEmpty(),
                                            )
                                        }
                                    },
                                    label = {
                                        Text(p.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowProviders.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            if (currentProvider?.needsKey == false) {
                item {
                    Text(
                        stringResource(
                            if (s.provider == "ollama") {
                                R.string.settings_local_no_key_desc
                            } else {
                                R.string.settings_no_key_desc
                            },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { newKey ->
                        keyText = newKey
                        app.settings.setKey(s.provider, newKey)
                    },
                    label = {
                        Text(
                            stringResource(
                                if (currentProvider?.keyOptional == true) {
                                    R.string.settings_api_key_optional
                                } else {
                                    R.string.settings_api_key
                                },
                            ),
                        )
                    },
                    placeholder = { Text(stringResource(R.string.settings_api_key_hint)) },
                    singleLine = true,
                    visualTransformation = if (keyVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                if (keyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = currentProvider?.needsKey == true || currentProvider?.keyOptional == true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = modelText,
                        onValueChange = { newModel ->
                            modelText = newModel
                            app.settings.setModel(s.provider, newModel)
                        },
                        label = { Text(stringResource(R.string.settings_model)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        OutlinedButton(onClick = { modelExpanded = true }) {
                            Icon(
                                Icons.Filled.ExpandMore,
                                contentDescription = stringResource(R.string.settings_model),
                            )
                        }
                        DropdownMenu(
                            expanded = modelExpanded,
                            onDismissRequest = { modelExpanded = false },
                        ) {
                            models.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Text(m, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    onClick = {
                                        modelText = m
                                        app.settings.setModel(s.provider, m)
                                        modelExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    IconButton(onClick = { vm.refreshModels(s.provider) }) {
                        if (vmState.loadingModels) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        } else {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.settings_refresh_models),
                            )
                        }
                    }
                }
            }

            if (s.provider == "ollama") {
                item {
                    Column {
                        OutlinedTextField(
                            value = s.ollamaHost,
                            onValueChange = { newHost ->
                                app.settings.update { it.copy(ollamaHost = newHost) }
                            },
                            label = { Text(stringResource(R.string.settings_ollama_host)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.settings_ollama_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { vm.testConnection(s.provider, modelText) },
                            enabled = vmState.test !is SettingsViewModel.TestState.Running,
                        ) {
                            Text(stringResource(R.string.settings_test_connection))
                        }
                        if (vmState.test is SettingsViewModel.TestState.Running) {
                            Text(
                                stringResource(R.string.settings_testing),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    when (val test = vmState.test) {
                        is SettingsViewModel.TestState.Ok -> Text(
                            stringResource(R.string.settings_test_ok),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        is SettingsViewModel.TestState.Failed -> Text(
                            aiErrorText(test.errorKey),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        else -> Unit
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.settings_voice_title)) }

            item {
                SwitchRow(
                    title = stringResource(R.string.settings_titanali_voice),
                    desc = null,
                    checked = s.titanaliVoice,
                    onChange = { checked ->
                        app.settings.update { cur -> cur.copy(titanaliVoice = checked) }
                    },
                )
            }

            item {
                Column {
                    Text(
                        stringResource(R.string.settings_tts_lang),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = s.ttsLanguage.startsWith("fa"),
                            onClick = { app.settings.update { it.copy(ttsLanguage = "fa") } },
                            label = { Text(stringResource(R.string.fa)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = s.ttsLanguage.startsWith("en"),
                            onClick = { app.settings.update { it.copy(ttsLanguage = "en-US") } },
                            label = { Text(stringResource(R.string.en)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                Column {
                    Text(
                        stringResource(R.string.settings_stt_lang),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = s.sttLanguage.startsWith("fa"),
                            onClick = { app.settings.update { it.copy(sttLanguage = "fa-IR") } },
                            label = { Text(stringResource(R.string.fa)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = s.sttLanguage.startsWith("en"),
                            onClick = { app.settings.update { it.copy(sttLanguage = "en-US") } },
                            label = { Text(stringResource(R.string.en)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                Column {
                    Text(
                        stringResource(R.string.settings_tts_rate),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = s.ttsRate,
                        onValueChange = { newRate -> app.settings.update { it.copy(ttsRate = newRate) } },
                        valueRange = 0.5f..2f,
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.settings_ui)) }

            item {
                Column {
                    Text(
                        stringResource(R.string.settings_ui_lang),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.settings_ui_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = s.uiLanguage == "fa",
                            onClick = {
                                if (s.uiLanguage != "fa") {
                                    app.settings.update { it.copy(uiLanguage = "fa") }
                                    (context as? Activity)?.recreate()
                                }
                            },
                            label = { Text(stringResource(R.string.fa)) },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = s.uiLanguage == "en",
                            onClick = {
                                if (s.uiLanguage != "en") {
                                    app.settings.update { it.copy(uiLanguage = "en") }
                                    (context as? Activity)?.recreate()
                                }
                            },
                            label = { Text(stringResource(R.string.en)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.settings_links)) }

            item {
                Column {
                    app.providers.values.filter { it.keyUrl != null }.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                p.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = {
                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(p.keyUrl)),
                                        )
                                    } catch (_: Exception) {
                                    }
                                },
                            ) {
                                Text(stringResource(R.string.open))
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.settings_version),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    desc: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (desc != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
