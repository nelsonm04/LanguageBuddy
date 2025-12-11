package com.example.languagebuddy.data.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSessionRepository(
    private val sessionDao: SessionDao,
    private val userSessionJoinDao: UserSessionJoinDao,
    private val accountDao: AccountDao
) {
    fun observeUserSessions(email: String): Flow<List<SessionWithHost>> =
        userSessionJoinDao.getSessionsForUser(email).map { sessions ->
            sessions.map { it.withHostName() }
        }

    fun observeDiscoverSessions(email: String): Flow<List<SessionWithHost>> =
        sessionDao.getAllSessions().map { sessions ->
            sessions
                .filter { it.hostEmail != email }
                .map { it.withHostName() }
        }

    suspend fun createOrUpdateSession(session: SessionEntity, ownerEmail: String) {
        sessionDao.insert(session)
        userSessionJoinDao.insert(UserSessionJoinEntity(ownerEmail, session.sessionId))
    }

    suspend fun joinSession(email: String, sessionId: String) {
        userSessionJoinDao.insert(UserSessionJoinEntity(email, sessionId))
    }

    suspend fun leaveSession(email: String, sessionId: String) {
        userSessionJoinDao.delete(email, sessionId)
    }

    suspend fun addUserToSession(email: String, sessionId: String) {
        userSessionJoinDao.insert(UserSessionJoinEntity(email, sessionId))
    }

    private suspend fun SessionEntity.withHostName(): SessionWithHost {
        val hostAccount = accountDao.getAccount(hostEmail)
        val hostName = hostAccount?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: hostAccount?.name
            ?: hostEmail
        return SessionWithHost(this, hostName)
    }
}

class RoomChatRepository(
    private val messageDao: MessageDao
) {
    fun recentChats(currentUserId: Int): Flow<List<MessageEntity>> = messageDao.getRecentChats(currentUserId)

    fun messagesForChat(chatId: Int): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)

    suspend fun sendMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun lastMessage(chatId: Int): MessageEntity? = messageDao.getLastMessageForChat(chatId)

    suspend fun participants(chatId: Int): List<Int> = messageDao.getChatParticipants(chatId)

    fun messagesBetween(sender: String, receiver: String): Flow<List<MessageEntity>> =
        messageDao.getMessages(sender, receiver)

}

class FriendRepository(
    private val accountDao: AccountDao,
    private val friendDao: FriendDao
) {
    fun getAllUsers(currentEmail: String): Flow<List<AccountEntity>> =
        accountDao.observeOtherAccounts(currentEmail)

    fun getIncomingRequests(email: String): Flow<List<FriendRequestEntity>> =
        friendDao.getIncomingRequests(email)

    fun getOutgoingRequests(email: String): Flow<List<FriendRequestEntity>> =
        friendDao.getOutgoingRequests(email)

    suspend fun sendRequest(fromEmail: String, toEmail: String) {
        friendDao.insertFriendRequest(
            FriendRequestEntity(
                fromEmail = fromEmail,
                toEmail = toEmail,
                status = "pending"
            )
        )
    }

    suspend fun updateRequestStatus(requestId: Int, status: String) {
        friendDao.updateRequestStatus(requestId, status)
    }

    suspend fun acceptFriendship(request: FriendRequestEntity) {
        val (u1, u2) = orderEmails(request.fromEmail, request.toEmail)
        friendDao.insertFriendship(FriendshipEntity(user1Email = u1, user2Email = u2))
        friendDao.updateRequestStatus(request.requestId, "accepted")
    }

    suspend fun declineFriendship(requestId: Int) {
        friendDao.updateRequestStatus(requestId, "declined")
    }

    fun friends(email: String): Flow<List<FriendshipEntity>> =
        friendDao.getFriendships(email)

    suspend fun rateFriend(friendEmail: String, raterEmail: String, rating: Float) {
        friendDao.insertRating(
            FriendRatingEntity(
                friendEmail = friendEmail,
                raterEmail = raterEmail,
                rating = rating,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun averageRating(friendEmail: String): Flow<Float?> = friendDao.getAverageRating(friendEmail)

    fun averageRatings(): Flow<List<FriendAverage>> = friendDao.getAverageRatings()

    fun userRatingForFriend(friendEmail: String, raterEmail: String): Flow<Float?> =
        friendDao.getUserRatingForFriend(friendEmail, raterEmail)

    fun ratingsByRater(raterEmail: String): Flow<List<FriendRatingEntity>> =
        friendDao.getRatingsByRater(raterEmail)

    private fun orderEmails(a: String, b: String): Pair<String, String> =
        if (a <= b) a to b else b to a
}

data class SessionWithHost(
    val session: SessionEntity,
    val hostName: String
)
