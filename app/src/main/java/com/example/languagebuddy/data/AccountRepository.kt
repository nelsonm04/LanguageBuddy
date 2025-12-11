package com.example.languagebuddy.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.languagebuddy.data.room.AccountDao
import com.example.languagebuddy.data.room.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest

private const val CREDENTIALS_KEY = "account_credentials"
private const val CURRENT_EMAIL_KEY = "current_email"

data class Account(
    val accountId: Int,
    val email: String,
    val name: String?,
    val displayName: String?,
    val status: String = "student",
    val bio: String?,
    val languages: String?,
    val specialties: String?,
    val timeZone: String?,
    val location: String?,
    val createdAt: Long,
    val availability: String?,
    val rating: Float = 0f,
    val ratingCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class AccountRepository(
    private val accountDao: AccountDao,
    private val credentialStore: DataStore<Preferences>
) {

    private val currentEmailFlow: Flow<String?> = credentialStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[CURRENT_EMAIL] }

    val accountFlow: Flow<AccountEntity?> = currentEmailFlow.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(null) else accountDao.observeAccount(email)
    }

    fun observeOtherUsers(currentEmail: String): Flow<List<Account>> =
        accountDao.observeOtherAccounts(currentEmail).map { list ->
            list.map { it.toAccount() }
        }

    fun getAllAccounts(): Flow<List<Account>> =
        accountDao.getAllAccounts().map { list -> list.map { it.toAccount() } }

    fun getAccountsByStatus(status: String): Flow<List<Account>> =
        accountDao.getAccountsByStatus(status).map { list -> list.map { it.toAccount() } }

    fun searchAccounts(query: String): Flow<List<Account>> =
        accountDao.searchAccounts(query).map { list -> list.map { it.toAccount() } }

    fun observeAccountById(accountId: Int): Flow<Account?> =
        accountDao.observeAllAccounts().map { list ->
            list.firstOrNull { it.accountId == accountId }?.toAccount()
        }

    suspend fun registerAccount(name: String, email: String, password: String): Boolean {
        val normalized = email.lowercase()
        val existingCreds = credentialStore.data.firstOrNull()?.let { (it[CREDENTIALS] ?: "").toCredentialMap() } ?: emptyMap()
        if (existingCreds.containsKey(normalized)) return false
        val now = System.currentTimeMillis()
        accountDao.insertOrUpdate(
            AccountEntity(
                accountId = normalized.hashCode(),
                email = normalized,
                name = name,
                displayName = name,
                status = "student",
                bio = null,
                languages = null,
                specialties = null,
                timeZone = null,
                location = null,
                availability = null,
                createdAt = now,
                rating = 0f,
                ratingCount = 0
            )
        )
        credentialStore.edit { prefs ->
            val currentMap = (prefs[CREDENTIALS] ?: "").toCredentialMap().toMutableMap()
            currentMap[normalized] = password.sha256()
            prefs[CREDENTIALS] = currentMap.toPersistedString()
            prefs[CURRENT_EMAIL] = normalized
        }
        return true
    }

    suspend fun verifyCredentials(email: String, password: String): Boolean {
        val normalized = email.lowercase()
        val hashed = password.sha256()
        val prefs = credentialStore.data.catch { emit(emptyPreferences()) }.map { it }.firstOrNull() ?: return false
        val storedHash = (prefs[CREDENTIALS] ?: "").toCredentialMap()[normalized] ?: return false
        val success = storedHash == hashed
        if (success) {
            credentialStore.edit { editPrefs -> editPrefs[CURRENT_EMAIL] = normalized }
        }
        return success
    }

    suspend fun updateAccountProfile(
        email: String,
        name: String?,
        displayName: String?,
        languages: String?,
        specialties: String?,
        status: String,
        bio: String?,
        availability: String?,
        timeZone: String?,
        location: String?
    ) {
        val existing = accountDao.getAccount(email)
        val entity = AccountEntity(
            accountId = existing?.accountId ?: email.hashCode(),
            email = email,
            name = name,
            displayName = displayName ?: existing?.displayName,
            status = status,
            bio = bio,
            languages = languages,
            specialties = specialties,
            timeZone = timeZone,
            location = location,
            availability = availability,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            rating = existing?.rating ?: 0f,
            ratingCount = existing?.ratingCount ?: 0
        )
        accountDao.insertOrUpdate(entity)
    }

    suspend fun setCurrentEmail(email: String?) {
        credentialStore.edit { prefs ->
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
        credentialStore.edit { prefs -> prefs.clear() }
    }

    suspend fun updateRating(email: String, rating: Float) {
        accountDao.updateRating(email, rating)
    }

    fun getRatingFlow(email: String): Flow<Float> = accountDao.getRatingFlow(email)

    private fun Map<String, String>.toPersistedString(): String =
        entries.joinToString(separator = "\n") { entry ->
            listOf(entry.key, entry.value).joinToString(separator = "|")
        }

    private fun String.toCredentialMap(): Map<String, String> {
        if (isBlank()) return emptyMap()
        return lineSequence()
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
    }

    private companion object {
        val CREDENTIALS = stringPreferencesKey(CREDENTIALS_KEY)
        val CURRENT_EMAIL = stringPreferencesKey(CURRENT_EMAIL_KEY)
    }

    private fun AccountEntity.toAccount(): Account = Account(
        email = email,
        accountId = accountId,
        name = name,
        displayName = displayName,
        status = status,
        bio = bio,
        languages = languages,
        specialties = specialties,
        timeZone = timeZone,
        location = location,
        availability = availability,
        createdAt = createdAt,
        rating = rating,
        ratingCount = ratingCount
    )

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(toByteArray())
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
