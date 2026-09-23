package com.titanali.app.ui.screens

import android.content.Context
import android.text.format.DateFormat
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.titanali.app.R
import com.titanali.app.ui.components.EmptyState
import com.titanali.app.ui.components.Tag
import com.titanali.app.vm.HomeViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val vm: HomeViewModel = viewModel()
    val items by vm.items.collectAsStateWithLifecycle()
    val newId by vm.newConversationId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(newId) {
        newId?.let { id ->
            navController.navigate("chat/$id")
            vm.clearNewConversationId()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    TextButton(onClick = { vm.createConversation("general") }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.new_chat))
                    }
                },
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = painterResource(R.drawable.ic_launcher_foreground),
                    title = stringResource(R.string.chat_empty_title),
                    desc = stringResource(R.string.chat_empty_desc),
                    actionLabel = stringResource(R.string.new_chat),
                    onAction = { vm.createConversation("general") },
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    ConversationCard(
                        item = item,
                        context = context,
                        onClick = { navController.navigate("chat/${item.id}") },
                        onDelete = { vm.delete(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    item: HomeViewModel.ConvItem,
    context: Context,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = item.title.ifBlank { stringResource(R.string.chat_new_title) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Tag(text = modeLabel(item.mode))
                    Text(
                        text = relativeTime(context, item.updatedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun modeLabel(mode: String): String = when {
    mode == "titanali" -> stringResource(R.string.mode_titanali)
    mode.startsWith("learn_") -> stringResource(R.string.mode_learn)
    else -> stringResource(R.string.mode_general)
}

@Composable
private fun relativeTime(context: Context, ts: Long): String {
    val min = (System.currentTimeMillis() - ts) / 60_000L
    return when {
        min < 1 -> context.getString(R.string.just_now)
        min < 60 -> context.getString(R.string.min_ago, min.toInt())
        min < 60 * 24 -> context.getString(R.string.hour_ago, (min / 60).toInt())
        else -> java.text.DateFormat.getDateInstance(
            java.text.DateFormat.MEDIUM,
            context.resources.configuration.locale,
        ).format(Date(ts)).toString()
    }
}
