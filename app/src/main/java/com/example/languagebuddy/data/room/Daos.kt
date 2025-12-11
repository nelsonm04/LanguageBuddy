package com.example.languagebuddy.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun getAccount(email: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE accountId = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: Int): AccountEntity?

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    fun observeAccount(email: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts")
    fun getAllUsers(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE email != :currentEmail ORDER BY displayName")
    fun observeOtherAccounts(currentEmail: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE status = :status")
    fun getAccountsByStatus(status: String): Flow<List<AccountEntity>>

    @Query("""
        SELECT * FROM accounts
        WHERE displayName LIKE '%' || :query || '%'
           OR bio LIKE '%' || :query || '%'
           OR languages LIKE '%' || :query || '%'
           OR specialties LIKE '%' || :query || '%'
    """)
    fun searchAccounts(query: String): Flow<List<AccountEntity>>

    @Query("UPDATE accounts SET rating = :rating WHERE email = :email")
    suspend fun updateRating(email: String, rating: Float)

    @Query("SELECT rating FROM accounts WHERE email = :email LIMIT 1")
    fun getRatingFlow(email: String): Flow<Float>
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUser(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun delete(sessionId: String)

    @Query("SELECT * FROM sessions WHERE hostEmail = :email")
    fun getSessionsByHost(email: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions")
    fun getAllSessions(): Flow<List<SessionEntity>>
}

@Dao
interface UserSessionJoinDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(join: UserSessionJoinEntity)

    @Query("DELETE FROM user_session_join WHERE email = :email AND sessionId = :sessionId")
    suspend fun delete(email: String, sessionId: String)

    @Query("""
        SELECT s.* FROM sessions s
        INNER JOIN user_session_join us ON s.sessionId = us.sessionId
        WHERE us.email = :email
    """)
    fun getSessionsForUser(email: String): Flow<List<SessionEntity>>

    @Query("SELECT sessionId FROM user_session_join WHERE email = :email")
    fun getSessionIdsForUser(email: String): Flow<List<String>>
}

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendRequest(request: FriendRequestEntity)

    @Query("SELECT * FROM friend_requests WHERE toEmail = :email")
    fun getIncomingRequests(email: String): Flow<List<FriendRequestEntity>>

    @Query("SELECT * FROM friend_requests WHERE fromEmail = :email")
    fun getOutgoingRequests(email: String): Flow<List<FriendRequestEntity>>

    @Query("UPDATE friend_requests SET status = :status WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: Int, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: FriendshipEntity)

    @Query("SELECT * FROM friendships WHERE user1Email = :email OR user2Email = :email")
    fun getFriendships(email: String): Flow<List<FriendshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: FriendRatingEntity)

    @Query("SELECT rating FROM friend_ratings WHERE friendEmail = :friendEmail AND raterEmail = :raterEmail LIMIT 1")
    fun getUserRatingForFriend(friendEmail: String, raterEmail: String): Flow<Float?>

    @Query("SELECT * FROM friend_ratings WHERE raterEmail = :raterEmail")
    fun getRatingsByRater(raterEmail: String): Flow<List<FriendRatingEntity>>

    @Query("SELECT AVG(rating) FROM friend_ratings WHERE friendEmail = :friendEmail")
    fun getAverageRating(friendEmail: String): Flow<Float?>

    @Query("SELECT friendEmail as email, AVG(rating) as average FROM friend_ratings GROUP BY friendEmail")
    fun getAverageRatings(): Flow<List<FriendAverage>>
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Int)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    fun getMessagesForChat(chatId: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForChat(chatId: Int): MessageEntity?

    @Query("SELECT DISTINCT senderId FROM messages WHERE chatId = :chatId UNION SELECT DISTINCT receiverId FROM messages WHERE chatId = :chatId")
    suspend fun getChatParticipants(chatId: Int): List<Int>

    @Query("""
        SELECT M.* FROM messages M
        JOIN (
            SELECT chatId, MAX(timestamp) AS latest
            FROM messages
            GROUP BY chatId
        ) grouped
        ON M.chatId = grouped.chatId AND M.timestamp = grouped.latest
        WHERE (M.senderId = :currentUserId OR M.receiverId = :currentUserId)
          AND NOT (M.senderId = :currentUserId AND M.receiverId = :currentUserId)
        ORDER BY M.timestamp DESC
    """)
    fun getChatsWithLastMessage(currentUserId: Int): Flow<List<MessageEntity>>

    @Query("""
        SELECT M.* FROM messages M
        JOIN (
            SELECT chatId, MAX(timestamp) AS latest
            FROM messages
            GROUP BY chatId
        ) grouped
        ON M.chatId = grouped.chatId AND M.timestamp = grouped.latest
        WHERE (M.senderId = :currentUserId OR M.receiverId = :currentUserId)
          AND NOT (M.senderId = :currentUserId AND M.receiverId = :currentUserId)
        ORDER BY M.timestamp DESC
        LIMIT 3
    """)
    fun getRecentChats(currentUserId: Int): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages
        WHERE (senderEmail = :sender AND receiverEmail = :receiver)
           OR (senderEmail = :receiver AND receiverEmail = :sender)
        ORDER BY timestamp ASC
    """)
    fun getMessages(sender: String, receiver: String): Flow<List<MessageEntity>>
}

data class FriendAverage(
    val email: String,
    val average: Float?
)

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity): Long

    @Query("""
        SELECT * FROM chats
        WHERE (user1Id = :userA AND user2Id = :userB)
           OR (user1Id = :userB AND user2Id = :userA)
        LIMIT 1
    """)
    suspend fun getExistingChat(userA: Int, userB: Int): ChatEntity?

    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    suspend fun getChat(chatId: Int): ChatEntity?

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: Int)
}

@Dao
interface SessionInviteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvite(invite: SessionInviteEntity)

    @Query("SELECT * FROM session_invites WHERE receiverEmail = :email AND status = 'pending' ORDER BY createdAt DESC")
    fun observeInvitesForUser(email: String): Flow<List<SessionInviteEntity>>

    @Query("SELECT * FROM session_invites WHERE senderEmail = :email ORDER BY createdAt DESC")
    fun observeInvitesSentByUser(email: String): Flow<List<SessionInviteEntity>>

    @Query("UPDATE session_invites SET status = :status WHERE id = :id")
    suspend fun updateInviteStatus(id: Int, status: String)

    @Query("DELETE FROM session_invites WHERE id = :id")
    suspend fun deleteInvite(id: Int)
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userEmail = :email ORDER BY createdAt DESC")
    fun observeNotificationsForUser(email: String): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)
}
