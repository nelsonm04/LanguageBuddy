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
import java.util.UUID

data class StoredSession(
    val id: String,
    val title: String,
    val host: String,
    val hostEmail: String?,
    val language: String,
    val day: String,
    val time: String,
    val duration: String,
    val rating: Double,
    val accentColor: Long,
    val description: String,
    val originalHostEventId: String? = null
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

    fun allSessionsFlow(): Flow<List<StoredSession>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            prefs.asMap()
                .filterKeys { it.name.startsWith("sessions_") }
                .values
                .mapNotNull { it as? String }
                .flatMap { it.toSessionList() }
        }

    fun sessionsFromOtherUsersFlow(activeEmail: String): Flow<List<StoredSession>> =
        allSessionsFlow().map { sessions ->
            sessions.filter { it.hostEmail != activeEmail }
        }

    suspend fun addToUserSchedule(activeEmail: String, session: StoredSession) {
        dataStore.edit { prefs ->
            val key = key(activeEmail)
            val current = prefs[key]?.toSessionList()?.toMutableList() ?: mutableListOf()
            val alreadyScheduled = current.any { it.originalHostEventId == session.id }
            if (alreadyScheduled) return@edit

            val copied = session.copy(
                id = UUID.randomUUID().toString(),
                originalHostEventId = session.id
            )
            current.add(copied)
            prefs[key] = current.serialize()
        }
    }

    private fun List<StoredSession>.serialize(): String =
        joinToString(separator = "\n") { s ->
            listOf(
                s.id,
                s.title,
                s.host,
                s.hostEmail ?: "",
                s.language,
                s.day,
                s.time,
                s.duration,
                s.rating.toString(),
                s.accentColor.toString(),
                s.description.replace("\n", " "),
                s.originalHostEventId ?: ""
            ).joinToString("||")
        }

    private fun String.toSessionList(): List<StoredSession> =
        lineSequence()
            .mapNotNull { line ->
                val parts = line.split("||")
                when (parts.size) {
                    12 -> StoredSession(
                        id = parts.getOrNull(0).orEmpty(),
                        title = parts.getOrNull(1).orEmpty(),
                        host = parts.getOrNull(2).orEmpty(),
                        hostEmail = parts.getOrNull(3).orEmpty().ifBlank { null },
                        language = parts.getOrNull(4).orEmpty(),
                        day = parts.getOrNull(5).orEmpty(),
                        time = parts.getOrNull(6).orEmpty(),
                        duration = parts.getOrNull(7).orEmpty(),
                        rating = parts.getOrNull(8)?.toDoubleOrNull() ?: 0.0,
                        accentColor = parts.getOrNull(9)?.toLongOrNull() ?: 0xFF8C7AE6,
                        description = parts.getOrNull(10).orEmpty(),
                        originalHostEventId = parts.getOrNull(11).orEmpty().ifBlank { null }
                    )
                    11 -> StoredSession( // legacy without originalHostEventId
                        id = parts.getOrNull(0).orEmpty(),
                        title = parts.getOrNull(1).orEmpty(),
                        host = parts.getOrNull(2).orEmpty(),
                        hostEmail = parts.getOrNull(3).orEmpty().ifBlank { null },
                        language = parts.getOrNull(4).orEmpty(),
                        day = parts.getOrNull(5).orEmpty(),
                        time = parts.getOrNull(6).orEmpty(),
                        duration = parts.getOrNull(7).orEmpty(),
                        rating = parts.getOrNull(8)?.toDoubleOrNull() ?: 0.0,
                        accentColor = parts.getOrNull(9)?.toLongOrNull() ?: 0xFF8C7AE6,
                        description = parts.getOrNull(10).orEmpty(),
                        originalHostEventId = null
                    )
                    10 -> StoredSession( // legacy without language and originalHostEventId
                        id = parts.getOrNull(0).orEmpty(),
                        title = parts.getOrNull(1).orEmpty(),
                        host = parts.getOrNull(2).orEmpty(),
                        hostEmail = parts.getOrNull(3).orEmpty().ifBlank { null },
                        language = "",
                        day = parts.getOrNull(4).orEmpty(),
                        time = parts.getOrNull(5).orEmpty(),
                        duration = parts.getOrNull(6).orEmpty(),
                        rating = parts.getOrNull(7)?.toDoubleOrNull() ?: 0.0,
                        accentColor = parts.getOrNull(8)?.toLongOrNull() ?: 0xFF8C7AE6,
                        description = parts.getOrNull(9).orEmpty(),
                        originalHostEventId = null
                    )
                    9 -> StoredSession( // legacy without hostEmail
                        id = parts.getOrNull(0).orEmpty(),
                        title = parts.getOrNull(1).orEmpty(),
                        host = parts.getOrNull(2).orEmpty(),
                        hostEmail = null,
                        language = "",
                        day = parts.getOrNull(3).orEmpty(),
                        time = parts.getOrNull(4).orEmpty(),
                        duration = parts.getOrNull(5).orEmpty(),
                        rating = parts.getOrNull(6)?.toDoubleOrNull() ?: 0.0,
                        accentColor = parts.getOrNull(7)?.toLongOrNull() ?: 0xFF8C7AE6,
                        description = parts.getOrNull(8).orEmpty(),
                        originalHostEventId = null
                    )
                    else -> null
                }
            }
            .toList()

    private fun key(email: String) = stringPreferencesKey("sessions_${email.safeKey()}")
}

private fun String.safeKey(): String = if (isBlank()) "default" else replace("[^A-Za-z0-9_]".toRegex(), "_")
