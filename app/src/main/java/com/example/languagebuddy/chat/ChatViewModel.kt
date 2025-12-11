package com.example.languagebuddy.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.languagebuddy.data.room.ChatRepository
import com.example.languagebuddy.data.room.MessageEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val currentUserId: Int
) : ViewModel() {

    val chats: StateFlow<List<MessageEntity>> = chatRepository.chatsWithLastMessage(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun startNewChat(otherUserId: Int): Int =
        chatRepository.startNewChat(currentUserId, otherUserId)

    fun deleteChat(chatId: Int) {
        viewModelScope.launch {
            chatRepository.deleteChatCompletely(chatId)
        }
    }

    companion object {
        fun provideFactory(chatRepository: ChatRepository, currentUserId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ChatViewModel(chatRepository, currentUserId) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class $modelClass")
                }
            }
    }
}
