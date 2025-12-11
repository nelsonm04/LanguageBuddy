package com.example.languagebuddy.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.languagebuddy.data.AccountRepository
import com.example.languagebuddy.data.UserPreferencesRepository
import com.example.languagebuddy.data.room.AccountEntity
import com.example.languagebuddy.data.room.FriendRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val accountRepository: AccountRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {

    val account: StateFlow<AccountEntity?> = accountRepository.accountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val rating: StateFlow<Float> = account.flatMapLatest { acct ->
        val email = acct?.email ?: return@flatMapLatest flowOf(0f)
        friendRepository.averageRating(email)
    }
        .map { it ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    fun saveProfile(
        displayName: String,
        languages: String,
        specialties: String,
        status: String,
        bio: String,
        availability: String,
        location: String
    ) {
        val email = account.value?.email ?: return
        viewModelScope.launch {
            accountRepository.updateAccountProfile(
                email = email,
                name = displayName,
                displayName = displayName,
                languages = languages,
                specialties = specialties,
                status = status,
                bio = bio,
                availability = availability,
                timeZone = null,
                location = location
            )
            preferencesRepository.updateProfile(email, displayName, languages, location)
        }
    }

    fun setNotifications(email: String, enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNotificationsEnabled(email, enabled) }
    }

    fun setDailyReminder(email: String, enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDailyReminder(email, enabled) }
    }

    companion object {
        fun provideFactory(
            accountRepository: AccountRepository,
            preferencesRepository: UserPreferencesRepository,
            friendRepository: FriendRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProfileViewModel(accountRepository, preferencesRepository, friendRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}
