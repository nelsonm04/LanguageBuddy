package com.example.languagebuddy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class UserPreferences(
    val displayName: String = "",
    val languages: String = "",
    val timeZone: String = "",
    val notificationsEnabled: Boolean = true,
    val dailyReminder: Boolean = false
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    fun preferencesFlow(email: String): Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            UserPreferences(
                displayName = prefs[key("display_name", email)] ?: UserPreferences().displayName,
                languages = prefs[key("languages", email)] ?: UserPreferences().languages,
                timeZone = prefs[key("time_zone", email)] ?: UserPreferences().timeZone,
                notificationsEnabled = prefs[keyBool("notifications_enabled", email)] ?: UserPreferences().notificationsEnabled,
                dailyReminder = prefs[keyBool("daily_reminder", email)] ?: UserPreferences().dailyReminder
            )
        }

    suspend fun updateProfile(email: String, displayName: String, languages: String, timeZone: String) {
        dataStore.edit { prefs ->
            prefs[key("display_name", email)] = displayName
            prefs[key("languages", email)] = languages
            prefs[key("time_zone", email)] = timeZone
        }
    }

    suspend fun setNotificationsEnabled(email: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[keyBool("notifications_enabled", email)] = enabled
        }
    }

    suspend fun setDailyReminder(email: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[keyBool("daily_reminder", email)] = enabled
        }
    }

    private fun key(base: String, email: String) = stringPreferencesKey("${base}_${email.safeKey()}")
    private fun keyBool(base: String, email: String) = booleanPreferencesKey("${base}_${email.safeKey()}")

    private fun String.safeKey(): String = if (isBlank()) "default" else replace("[^A-Za-z0-9_]".toRegex(), "_")
}
