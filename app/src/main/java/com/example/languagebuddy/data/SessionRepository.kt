package com.example.languagebuddy.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class StoredSession(
    val id: String,
    val title: String,
    val host: String,
    val hostEmail: String?,
    val day: String,
    val time: String,
    val duration: String,
    val rating: Double,
    val accentColor: Long,
    val description: String
)

class SessionRepository(private val dataStore: DataStore<Preferences>) {

    fun sessionsFlow(email: String): Flow<List<StoredSession>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            prefs[key(email)]?.toSessionList() ?: emptyList()
        }

    suspend fun upsert(email: String, session: StoredSession) {
        dataStore.edit { prefs ->
            val current = prefs[key(email)]?.toSessionList()?.toMutableList() ?: mutableListOf()
            val existingIndex = current.indexOfFirst { it.id == session.id }
            if (existingIndex >= 0) {
                current[existingIndex] = session
            } else {
                current.add(session)
            }
            prefs[key(email)] = current.serialize()
        }
    }

    suspend fun delete(email: String, sessionId: String) {
        dataStore.edit { prefs ->
            val current = prefs[key(email)]?.toSessionList()?.toMutableList() ?: mutableListOf()
            prefs[key(email)] = current.filterNot { it.id == sessionId }.serialize()
        }
    }

    private fun List<StoredSession>.serialize(): String =
        joinToString(separator = "\n") { s ->
            listOf(
                s.id,
                s.title,
                s.host,
                s.hostEmail ?: "",
                s.day,
                s.time,
                s.duration,
                s.rating.toString(),
                s.accentColor.toString(),
                s.description.replace("\n", " ")
            ).joinToString("||")
        }

    private fun String.toSessionList(): List<StoredSession> =
        lineSequence()
            .mapNotNull { line ->
                val parts = line.split("||")
                when (parts.size) {
                    10 -> StoredSession(
                        id = parts[0],
                        title = parts[1],
                        host = parts[2],
                        hostEmail = parts[3].ifBlank { null },
                        day = parts[4],
                        time = parts[5],
                        duration = parts[6],
                        rating = parts[7].toDoubleOrNull() ?: 0.0,
                        accentColor = parts[8].toLongOrNull() ?: 0xFF8C7AE6,
                        description = parts[9]
                    )
                    9 -> StoredSession( // legacy without hostEmail
                        id = parts[0],
                        title = parts[1],
                        host = parts[2],
                        hostEmail = null,
                        day = parts[3],
                        time = parts[4],
                        duration = parts[5],
                        rating = parts[6].toDoubleOrNull() ?: 0.0,
                        accentColor = parts[7].toLongOrNull() ?: 0xFF8C7AE6,
                        description = parts[8]
                    )
                    else -> null
                }
            }
            .toList()

    private fun key(email: String) = stringPreferencesKey("sessions_${email.safeKey()}")
}

private fun String.safeKey(): String = if (isBlank()) "default" else replace("[^A-Za-z0-9_]".toRegex(), "_")
