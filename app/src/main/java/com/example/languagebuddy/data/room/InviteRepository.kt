package com.example.languagebuddy.data.room

import kotlinx.coroutines.flow.Flow

class InviteRepository(
    private val inviteDao: SessionInviteDao,
    private val notificationDao: NotificationDao
) {
    suspend fun insertInvite(invite: SessionInviteEntity) = inviteDao.insertInvite(invite)

    fun observeInvitesForUser(email: String): Flow<List<SessionInviteEntity>> =
        inviteDao.observeInvitesForUser(email)

    fun observeInvitesSentByUser(email: String): Flow<List<SessionInviteEntity>> =
        inviteDao.observeInvitesSentByUser(email)

    suspend fun updateInviteStatus(id: Int, status: String) = inviteDao.updateInviteStatus(id, status)

    suspend fun deleteInvite(id: Int) = inviteDao.deleteInvite(id)

    suspend fun insertNotification(notification: NotificationEntity) =
        notificationDao.insertNotification(notification)

    fun observeNotificationsForUser(email: String): Flow<List<NotificationEntity>> =
        notificationDao.observeNotificationsForUser(email)

    suspend fun deleteNotification(id: Int) = notificationDao.deleteNotification(id)
}
