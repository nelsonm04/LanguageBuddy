package com.example.languagebuddy.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest

private const val ACCOUNTS_KEY = "accounts_json"
private const val CURRENT_EMAIL_KEY = "current_email"

data class Account(
    val name: String,
    val email: String,
    val passwordHash: String
)

class AccountRepository(private val dataStore: DataStore<Preferences>) {

    val accountFlow: Flow<Account?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val accounts = prefs[ACCOUNTS] ?: ""
            val currentEmail = prefs[CURRENT_EMAIL]
            val map = accounts.toAccountMap()
            if (currentEmail != null) map[currentEmail.lowercase()] else null
        }

    suspend fun registerAccount(name: String, email: String, password: String): Boolean {
        val normalized = email.lowercase()
        val hashed = password.sha256()
        val existingMap = dataStore.data.firstOrNull()?.let { (it[ACCOUNTS] ?: "").toAccountMap() } ?: emptyMap()
        if (existingMap.containsKey(normalized)) return false
        dataStore.edit { prefs ->
            val currentMap = (prefs[ACCOUNTS] ?: "").toAccountMap().toMutableMap()
            currentMap[normalized] = Account(name = name, email = normalized, passwordHash = hashed)
            prefs[ACCOUNTS] = currentMap.toPersistedString()
            prefs[CURRENT_EMAIL] = normalized
        }
        return true
    }

    suspend fun verifyCredentials(email: String, password: String): Boolean {
        val normalized = email.lowercase()
        val hashed = password.sha256()
        val prefs = dataStore.data.catch { emit(emptyPreferences()) }.map { it }.firstOrNull() ?: return false
        val account = (prefs[ACCOUNTS] ?: "").toAccountMap()[normalized] ?: return false
        val success = account.passwordHash == hashed
        if (success) {
            dataStore.edit { editPrefs -> editPrefs[CURRENT_EMAIL] = normalized }
        }
        return success
    }

    suspend fun setCurrentEmail(email: String?) {
        dataStore.edit { prefs ->
            if (email.isNullOrBlank()) {
                prefs.remove(CURRENT_EMAIL)
            } else {
                prefs[CURRENT_EMAIL] = email.lowercase()
            }
        }
    }

    suspend fun logout() {
        setCurrentEmail(null)
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    private fun Map<String, Account>.toPersistedString(): String =
        values.joinToString(separator = "\n") { account ->
            listOf(account.email, account.name, account.passwordHash).joinToString(separator = "|")
        }

    private fun String.toAccountMap(): Map<String, Account> {
        if (isBlank()) return emptyMap()
        return lineSequence()
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 3) {
                    val email = parts[0]
                    Account(name = parts[1], email = email, passwordHash = parts[2])
                } else null
            }
            .associateBy { it.email.lowercase() }
    }

    private companion object {
        val ACCOUNTS = stringPreferencesKey(ACCOUNTS_KEY)
        val CURRENT_EMAIL = stringPreferencesKey(CURRENT_EMAIL_KEY)
    }

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(toByteArray())
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
