package com.example.languagebuddy.data.room

import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val accountDao: AccountDao
) {

    fun chatsWithLastMessage(currentUserId: Int): Flow<List<MessageEntity>> =
        messageDao.getChatsWithLastMessage(currentUserId)

    fun recentChats(currentUserId: Int): Flow<List<MessageEntity>> =
        messageDao.getRecentChats(currentUserId)

    suspend fun startNewChat(userId: Int, otherUserId: Int): Int {
        val existing = chatDao.getExistingChat(userId, otherUserId)
        if (existing != null) return existing.chatId
        return chatDao.insert(ChatEntity(user1Id = userId, user2Id = otherUserId)).toInt()
    }

    fun messagesForChat(chatId: Int): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)

    suspend fun getOrCreateChat(userA: Int, userB: Int): Int {
        val existing = chatDao.getExistingChat(userA, userB)
        if (existing != null) return existing.chatId
        val id = chatDao.insert(ChatEntity(user1Id = userA, user2Id = userB))
        return id.toInt()
    }

    suspend fun sendMessage(
        currentUserId: Int,
        otherUserId: Int,
        senderEmail: String,
        receiverEmail: String,
        content: String
    ): Int {
        val chatId = getOrCreateChat(currentUserId, otherUserId)
        val message = MessageEntity(
            messageId = "$chatId|${System.currentTimeMillis()}",
            chatId = chatId,
            senderId = currentUserId,
            receiverId = otherUserId,
            senderEmail = senderEmail,
            receiverEmail = receiverEmail,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(message)
        return chatId
    }

    suspend fun otherUserAccount(otherUserId: Int): AccountEntity? = accountDao.getAccountById(otherUserId)

    suspend fun deleteChatCompletely(chatId: Int) {
        messageDao.deleteMessagesForChat(chatId)
        chatDao.deleteChat(chatId)
    }
}
