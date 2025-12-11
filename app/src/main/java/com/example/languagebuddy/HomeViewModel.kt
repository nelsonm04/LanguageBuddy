package com.example.languagebuddy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.languagebuddy.data.AccountRepository
import com.example.languagebuddy.data.room.ChatRepository
import com.example.languagebuddy.data.room.MessageEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val accountRepository: AccountRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val activeEmail = MutableStateFlow<String?>(null)
    private val activeAccountId = MutableStateFlow<Int?>(null)

    val ratingState: StateFlow<Float> = activeEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(0f) else accountRepository.getRatingFlow(email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0f
        )

    val recentChats: StateFlow<List<MessageEntity>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else chatRepository.recentChats(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setActiveEmail(email: String?) {
        activeEmail.value = email
    }

    fun setActiveAccountId(id: Int?) {
        activeAccountId.value = id
    }

    fun updateRating(rating: Float) {
        val email = activeEmail.value ?: return
        viewModelScope.launch {
            accountRepository.updateRating(email, rating)
        }
    }

    companion object {
        fun provideFactory(
            accountRepository: AccountRepository,
            chatRepository: ChatRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(accountRepository, chatRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}
