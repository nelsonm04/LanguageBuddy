package com.example.languagebuddy.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.languagebuddy.data.room.AccountEntity
import com.example.languagebuddy.data.room.FriendRequestEntity
import com.example.languagebuddy.data.room.FriendRepository
import com.example.languagebuddy.data.room.FriendshipEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModel(
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val activeEmail = MutableStateFlow<String?>(null)

    val users: StateFlow<List<AccountEntity>> = activeEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(emptyList()) else friendRepository.getAllUsers(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val incomingRequests: StateFlow<List<FriendRequestEntity>> = activeEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(emptyList()) else friendRepository.getIncomingRequests(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val outgoingRequests: StateFlow<List<FriendRequestEntity>> = activeEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(emptyList()) else friendRepository.getOutgoingRequests(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val friendships: StateFlow<List<FriendshipEntity>> = activeEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(emptyList()) else friendRepository.friends(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val averageRatings = friendRepository.averageRatings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val givenRatings: StateFlow<Map<String, Float>> = activeEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(emptyMap()) else friendRepository.ratingsByRater(email).map { list ->
            list.associate { it.friendEmail to it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setCurrentUser(email: String?) {
        activeEmail.value = email
    }

    fun sendRequest(toEmail: String) {
        val from = activeEmail.value ?: return
        viewModelScope.launch {
            friendRepository.sendRequest(from, toEmail)
        }
    }

    fun acceptRequest(request: FriendRequestEntity) {
        viewModelScope.launch {
            friendRepository.acceptFriendship(request)
        }
    }

    fun declineRequest(request: FriendRequestEntity) {
        viewModelScope.launch {
            friendRepository.declineFriendship(request.requestId)
        }
    }

    companion object {
        fun provideFactory(
            friendRepository: FriendRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FriendViewModel(friendRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}
