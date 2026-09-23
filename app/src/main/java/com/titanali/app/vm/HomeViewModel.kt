package com.titanali.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titanali.app.TitanaliApp
import com.titanali.app.data.db.ConversationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val tApp = app as TitanaliApp
    private val convDao = tApp.db.conversations()
    private val msgDao = tApp.db.messages()

    data class ConvItem(
        val id: Long,
        val title: String,
        val mode: String,
        val updatedAt: Long,
    )

    private val _items = MutableStateFlow<List<ConvItem>>(emptyList())
    val items: StateFlow<List<ConvItem>> = _items.asStateFlow()

    /** Emitted after a new conversation is created so the UI can navigate. */
    private val _newConversationId = MutableStateFlow<Long?>(null)
    val newConversationId: StateFlow<Long?> = _newConversationId.asStateFlow()

    init {
        viewModelScope.launch {
            convDao.observeAll().collect { list ->
                _items.value = list.map {
                    ConvItem(it.id, it.title, it.mode, it.updatedAt)
                }
            }
        }
    }

    fun createConversation(mode: String = "general") {
        viewModelScope.launch {
            val id = convDao.insert(
                ConversationEntity(title = "", mode = mode),
            )
            _newConversationId.value = id
        }
    }

    fun clearNewConversationId() {
        _newConversationId.value = null
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            msgDao.deleteByConversation(id)
            convDao.delete(id)
        }
    }
}
