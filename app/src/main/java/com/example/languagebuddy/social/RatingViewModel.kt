package com.example.languagebuddy.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.languagebuddy.data.room.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class RatingViewModel(
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val raterEmail = MutableStateFlow<String?>(null)

    fun setRater(email: String?) {
        raterEmail.value = email
    }

    fun updateRating(friendEmail: String, rating: Float) {
        val rater = raterEmail.value ?: return
        viewModelScope.launch {
            friendRepository.rateFriend(friendEmail, rater, rating)
        }
    }

    fun observeAverage(friendEmail: String): StateFlow<Float> =
        if (friendEmail.isBlank()) {
            MutableStateFlow(0f)
        } else {
            friendRepository.averageRating(friendEmail)
                .map { it ?: 0f }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)
        }

    fun observeUserRating(friendEmail: String): StateFlow<Float?> =
        raterEmail.flatMapLatest { email ->
            if (email.isNullOrBlank() || friendEmail.isBlank()) flowOf(null) else friendRepository.userRatingForFriend(friendEmail, email)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        fun provideFactory(
            friendRepository: FriendRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RatingViewModel(friendRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}
