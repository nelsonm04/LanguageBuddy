package com.example.languagebuddy.buddy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.languagebuddy.data.Account
import com.example.languagebuddy.data.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class BuddyViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val selectedTab = MutableStateFlow("All")
    private val searchQuery = MutableStateFlow("")
    private val currentEmail = MutableStateFlow<String?>(null)
    val selected: StateFlow<String> = selectedTab

    private val allAccounts: StateFlow<List<Account>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val results: StateFlow<List<Account>> = combine(allAccounts, selectedTab, searchQuery, currentEmail) { accounts, tab, query, email ->
        accounts
            .filter { it.email.isNotBlank() }
            .filter { acct -> email == null || acct.email != email }
            .filter { account ->
                tab == "All" || account.status.equals(tab.lowercase(), ignoreCase = true)
            }
            .filter { account ->
                if (query.isBlank()) return@filter true
                val q = query.lowercase()
                account.displayName?.lowercase()?.contains(q) == true ||
                        account.bio?.lowercase()?.contains(q) == true ||
                        account.languages?.lowercase()?.contains(q) == true ||
                        account.specialties?.lowercase()?.contains(q) == true
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setTab(tab: String) {
        selectedTab.value = when (tab.lowercase()) {
            "teachers" -> "teacher"
            "students" -> "student"
            "volunteers" -> "volunteer"
            else -> "All"
        }
    }

    fun setQuery(query: String) {
        searchQuery.value = query
    }

    fun setCurrentEmail(email: String?) {
        currentEmail.value = email
    }

    companion object {
        fun provideFactory(accountRepository: AccountRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(BuddyViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return BuddyViewModel(accountRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class $modelClass")
                }
            }
    }
}
