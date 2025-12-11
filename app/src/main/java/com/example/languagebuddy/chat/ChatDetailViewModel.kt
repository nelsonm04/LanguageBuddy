package com.example.languagebuddy.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.languagebuddy.data.Account
import com.example.languagebuddy.data.AccountRepository
import com.example.languagebuddy.data.room.ChatRepository
import com.example.languagebuddy.data.room.MessageEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val chatId: Int,
    private val otherUserId: Int,
    private val chatRepository: ChatRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    val messages: StateFlow<List<MessageEntity>> =
        chatRepository.messagesForChat(chatId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val otherUser: StateFlow<Account?> =
        accountRepository.observeAccountById(otherUserId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun sendMessage(senderEmail: String, senderId: Int, receiverEmail: String, content: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(
                currentUserId = senderId,
                otherUserId = otherUserId,
                senderEmail = senderEmail,
                receiverEmail = receiverEmail,
                content = content
            )
        }
    }

    companion object {
        fun provideFactory(
            chatId: Int,
            otherUserId: Int,
            chatRepository: ChatRepository,
            accountRepository: AccountRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatDetailViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ChatDetailViewModel(chatId, otherUserId, chatRepository, accountRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}
