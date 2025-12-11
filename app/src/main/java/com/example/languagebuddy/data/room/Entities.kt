package com.example.languagebuddy.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    val accountId: Int = 0,
    @PrimaryKey val email: String,
    val name: String?,
    val displayName: String? = null,
    val status: String = "student",
    val bio: String? = null,
    val languages: String? = null,
    val specialties: String? = null,
    val timeZone: String? = null,
    val location: String? = null,
    val availability: String? = null,
    val createdAt: Long,
    val rating: Float = 0f,
    val ratingCount: Int = 0
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String?,
    val languages: String?,
    val timeZone: String?
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val hostEmail: String,
    val title: String,
    val language: String?,
    val description: String?,
    val date: String,
    val time: String,
    val duration: String
)

@Entity(
    tableName = "user_session_join",
    primaryKeys = ["email", "sessionId"]
)
data class UserSessionJoinEntity(
    val email: String,
    val sessionId: String
)

@Entity(
    tableName = "friends",
    primaryKeys = ["userEmail", "friendEmail"]
)
data class FriendEntity(
    val userEmail: String,
    val friendEmail: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val chatId: Int = 0,
    val senderId: Int = 0,
    val receiverId: Int = 0,
    val senderEmail: String,
    val receiverEmail: String,
    @androidx.room.ColumnInfo(name = "message") val content: String,
    val timestamp: Long
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val chatId: Int = 0,
    val user1Id: Int,
    val user2Id: Int
)

@Entity(tableName = "friend_requests")
data class FriendRequestEntity(
    @PrimaryKey(autoGenerate = true) val requestId: Int = 0,
    val fromEmail: String,
    val toEmail: String,
    val status: String = "pending"
)

@Entity(tableName = "friendships")
data class FriendshipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user1Email: String,
    val user2Email: String
)

@Entity(tableName = "friend_ratings")
data class FriendRatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val friendEmail: String,
    val raterEmail: String,
    val rating: Float,
    val timestamp: Long
)

@Entity(tableName = "session_invites")
data class SessionInviteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderEmail: String,
    val receiverEmail: String,
    val title: String,
    val language: String?,
    val date: String,
    val time: String,
    val duration: String,
    val description: String?,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)
