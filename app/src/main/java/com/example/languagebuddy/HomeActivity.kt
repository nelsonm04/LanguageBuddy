@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.languagebuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.languagebuddy.data.Account
import com.example.languagebuddy.data.AccountRepository
import com.example.languagebuddy.data.UserPreferences
import com.example.languagebuddy.data.UserPreferencesRepository
import com.example.languagebuddy.data.accountDataStore
import com.example.languagebuddy.data.userPrefsDataStore
import com.example.languagebuddy.data.room.LanguageBuddyDatabase
import com.example.languagebuddy.data.room.RoomSessionRepository
import com.example.languagebuddy.data.room.SessionEntity
import com.example.languagebuddy.data.room.SessionWithHost
import com.example.languagebuddy.data.room.FriendRepository
import com.example.languagebuddy.data.room.ChatRepository
import com.example.languagebuddy.data.room.InviteRepository
import com.example.languagebuddy.data.room.SessionInviteEntity
import com.example.languagebuddy.data.room.NotificationEntity
import com.example.languagebuddy.social.FriendViewModel
import com.example.languagebuddy.social.RatingViewModel
import com.example.languagebuddy.profile.ProfileViewModel
import com.example.languagebuddy.buddy.BuddyViewModel
import com.example.languagebuddy.buddy.BuddyDiscoverScreen
import com.example.languagebuddy.chat.ChatDetailScreen
import com.example.languagebuddy.chat.ChatViewModel
import com.example.languagebuddy.chat.ChatListScreen
import com.example.languagebuddy.ui.theme.LanguageBuddyTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.flow.map

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageBuddyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeRoot()
                }
            }
        }
    }
}

private data class Session(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val host: String,
    val hostEmail: String? = null,
    val language: String,
    val day: String,
    val time: String,
    val duration: String,
    val rating: Double = 0.0,
    val accentColor: Color = Color(0xFF8C7AE6),
    val description: String = "",
    val originalHostEventId: String? = null
)

private data class ChatPreview(
    val chatId: Int,
    val threadId: String,
    val counterpartEmail: String,
    val counterpartId: Int,
    val name: String,
    val message: String,
    val timeAgo: String,
    val accentColor: Color,
    val lastTimestamp: Long
)

@Composable
private fun HomeRoot() {
    val context = LocalContext.current
    val userPreferencesRepository = remember { UserPreferencesRepository(context.userPrefsDataStore) }
    val database = remember { LanguageBuddyDatabase.getInstance(context) }
    val accountRepository = remember { AccountRepository(database.accountDao(), context.accountDataStore) }
    val sessionRepository = remember { RoomSessionRepository(database.sessionDao(), database.userSessionJoinDao(), database.accountDao()) }
    val chatRepositoryNew = remember { ChatRepository(database.chatDao(), database.messageDao(), database.accountDao()) }
    val inviteRepository = remember { InviteRepository(database.sessionInviteDao(), database.notificationDao()) }
    val friendRepository = remember { FriendRepository(database.accountDao(), database.friendDao()) }
    val friendViewModel: FriendViewModel = viewModel(factory = FriendViewModel.provideFactory(friendRepository))
    val ratingViewModel: RatingViewModel = viewModel(factory = RatingViewModel.provideFactory(friendRepository))
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.provideFactory(accountRepository, userPreferencesRepository, friendRepository))
    val account by accountRepository.accountFlow.collectAsState(initial = null)
    val profileRating by profileViewModel.rating.collectAsState()
    val activeEmail = account?.email ?: "default"
    val userPrefs by userPreferencesRepository.preferencesFlow(activeEmail).collectAsState(initial = UserPreferences())
    val scope = rememberCoroutineScope()
    val otherAccounts by accountRepository.observeOtherUsers(activeEmail).collectAsState(initial = emptyList())
    val buddyUsers by friendViewModel.users.collectAsState()
    val incomingRequests by friendViewModel.incomingRequests.collectAsState()
    val outgoingRequests by friendViewModel.outgoingRequests.collectAsState()
    val friendships by friendViewModel.friendships.collectAsState()
    val averageRatings by friendViewModel.averageRatings.collectAsState()
    val userRatings by friendViewModel.givenRatings.collectAsState()
    val homeRatingState = remember(activeEmail) { ratingViewModel.observeAverage(activeEmail) }
    val homeRating by homeRatingState.collectAsState()
    val todaySummary = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val currentAccountId = account?.accountId ?: activeEmail.hashCode()
    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedBuddyForInvite by remember { mutableStateOf<Account?>(null) }

    val userSessions by sessionRepository.observeUserSessions(activeEmail)
        .map { list -> list.map { it.toSession() } }
        .collectAsState(initial = emptyList())
    val discoverSessions by sessionRepository.observeDiscoverSessions(activeEmail)
        .map { list -> list.map { it.toSession() } }
        .collectAsState(initial = emptyList())
    val recentMessages by chatRepositoryNew.recentChats(currentAccountId).collectAsState(initial = emptyList())
    val chatItems = remember(recentMessages, otherAccounts, currentAccountId) {
        recentMessages.toChatPreviews(currentAccountId, otherAccounts)
    }
    val pendingInvites by inviteRepository.observeInvitesForUser(activeEmail).collectAsState(initial = emptyList())
    val notifications by inviteRepository.observeNotificationsForUser(activeEmail).collectAsState(initial = emptyList())

    LaunchedEffect(account?.email) {
        friendViewModel.setCurrentUser(account?.email)
        ratingViewModel.setRater(account?.email)
    }

    LaunchedEffect(account) {
        val email = account?.email ?: return@LaunchedEffect
        val name = userPrefs.displayName.ifBlank { account?.name ?: "" }
        database.accountDao().insertOrUpdate(
            com.example.languagebuddy.data.room.AccountEntity(
                accountId = account?.accountId ?: email.hashCode(),
                email = email,
                name = name,
                displayName = name,
                status = account?.status ?: "student",
                bio = account?.bio,
                languages = userPrefs.languages,
                specialties = account?.specialties,
                timeZone = userPrefs.timeZone,
                location = userPrefs.timeZone,
                availability = account?.availability,
                createdAt = account?.createdAt ?: System.currentTimeMillis(),
                rating = account?.rating ?: 0f,
                ratingCount = account?.ratingCount ?: 0
            )
        )
    }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedSession by remember { mutableStateOf<Session?>(null) }

    var showAddSessionDialog by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<Session?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                val (fabScale, fabInteraction) = rememberPressAnimation()
                FloatingActionButton(
                    onClick = {
                        editingSession = null
                        showAddSessionDialog = true
                    },
                    modifier = fabScale,
                    interactionSource = fabInteraction
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add session")
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val navItems = listOf(
                    Icons.Filled.Home to "Home",
                    Icons.Outlined.Group to "Buddy",
                    Icons.Filled.ChatBubble to "Chat",
                    Icons.Filled.Schedule to "Schedule",
                    Icons.Filled.Person to "Profile"
                )

                navItems.forEachIndexed { index, (icon, label) ->
                    val selected = selectedTab == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.1f else 1f,
                        animationSpec = tween(durationMillis = 150),
                        label = "navIconScale"
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = index },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                                )
                                AnimatedVisibility(visible = selected) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .height(3.dp)
                                            .width(20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(50)
                                            )
                                    )
                                }
                            }
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeScreen(
                padding = padding,
                sessions = userSessions,
                chats = chatItems,
                todaySummary = todaySummary,
                rating = homeRating,
                onRatingChanged = {},
                onSessionClick = { session -> selectedSession = session },
                notifications = notifications,
                onDismissNotification = { id ->
                    scope.launch { inviteRepository.deleteNotification(id) }
                },
                buttonLabel = "View",
                userName = account?.displayName ?: account?.name ?: activeEmail
            )
            1 -> {
                val buddyViewModel: BuddyViewModel = viewModel(factory = BuddyViewModel.provideFactory(accountRepository))
                BuddyDiscoverScreen(
                    viewModel = buddyViewModel,
                    currentEmail = activeEmail,
                    onMessage = { account ->
                        // Switch to chat tab and navigate
                        selectedTab = 2
                        scope.launch {
                            val chatId = chatRepositoryNew.startNewChat(currentAccountId, account.accountId)
                            // Chat navigation handled in ChatTabContent once tab changes
                        }
                    },
                    onSchedule = { account ->
                        selectedBuddyForInvite = account
                        showInviteDialog = true
                    }
                )
            }
            2 -> ChatTabContent(
                padding = padding,
                currentAccountId = account?.accountId ?: activeEmail.hashCode(),
                currentEmail = activeEmail,
                chatRepository = chatRepositoryNew,
                accountRepository = accountRepository
            )
            3 -> ScheduleTabContent(
                padding = padding,
                sessions = userSessions,
                pendingInvites = pendingInvites,
                onApproveInvite = { invite ->
                    scope.launch {
                        val sessionId = java.util.UUID.randomUUID().toString()
                        val sessionEntity = SessionEntity(
                            sessionId = sessionId,
                            hostEmail = activeEmail,
                            title = invite.title,
                            language = invite.language,
                            description = invite.description,
                            date = invite.date,
                            time = invite.time,
                            duration = invite.duration
                        )
                        sessionRepository.createOrUpdateSession(sessionEntity, activeEmail)
                        sessionRepository.addUserToSession(invite.senderEmail, sessionId)
                        inviteRepository.updateInviteStatus(invite.id, "approved")
                        inviteRepository.insertNotification(
                            NotificationEntity(
                                userEmail = invite.senderEmail,
                                message = "${account?.displayName ?: account?.email ?: activeEmail} accepted your event on ${invite.date}"
                            )
                        )
                    }
                },
                onDeclineInvite = { invite ->
                    scope.launch {
                        inviteRepository.updateInviteStatus(invite.id, "declined")
                        inviteRepository.insertNotification(
                            NotificationEntity(
                                userEmail = invite.senderEmail,
                                message = "${account?.displayName ?: account?.email ?: activeEmail} declined your event on ${invite.date}"
                            )
                        )
                    }
                },
                onSessionDetails = { selectedSession = it },
                onDeleteSession = { session ->
                    scope.launch { sessionRepository.leaveSession(activeEmail, session.id) }
                }
            )
            4 -> ProfileTabContent(
                padding = padding,
                accountName = account?.name,
                accountEmail = account?.email,
                account = account,
                userPreferences = userPrefs,
                rating = profileRating,
                onSaveProfile = { name, languages, specialties, statusValue, bio, availabilityValue, location ->
                    scope.launch {
                        userPreferencesRepository.updateProfile(activeEmail, name, languages, location)
                        accountRepository.updateAccountProfile(
                            email = activeEmail,
                            name = name,
                            displayName = name,
                            languages = languages,
                            specialties = specialties,
                            status = statusValue,
                            bio = bio,
                            availability = availabilityValue,
                            timeZone = location,
                            location = location
                        )
                    }
                },
                onToggleNotifications = { enabled ->
                    scope.launch { userPreferencesRepository.setNotificationsEnabled(activeEmail, enabled) }
                },
                onToggleDaily = { enabled ->
                    scope.launch { userPreferencesRepository.setDailyReminder(activeEmail, enabled) }
                },
                userSessions = userSessions,
                onCreateSession = {
                    editingSession = null
                    showAddSessionDialog = true
                },
                onEditSession = { session ->
                    editingSession = session
                    showAddSessionDialog = true
                },
                onDeleteSession = { session ->
                    scope.launch { sessionRepository.leaveSession(activeEmail, session.id) }
                },
                onLogout = {
                    scope.launch {
                        accountRepository.logout()
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    }
                }
            )
            else -> {}
        }

        selectedSession?.let { session ->
            SessionDetailDialog(
                session = session,
                onDismiss = { selectedSession = null }
            )
        }

        if (showAddSessionDialog) {
            SessionCreateDialog(
                initial = editingSession,
                onDismiss = { showAddSessionDialog = false },
                onSave = { title, language, date, time, duration, description ->
                    val existing = editingSession
                    val sessionEntity = SessionEntity(
                        sessionId = existing?.id ?: java.util.UUID.randomUUID().toString(),
                        hostEmail = activeEmail,
                        title = title,
                        language = language.ifBlank { null },
                        description = description.ifBlank { null },
                        date = date,
                        time = time,
                        duration = duration
                    )
                    scope.launch { sessionRepository.createOrUpdateSession(sessionEntity, activeEmail) }
                    editingSession = null
                    showAddSessionDialog = false
                }
            )
        }

        if (showInviteDialog && selectedBuddyForInvite != null) {
            SessionInviteDialog(
                recipient = selectedBuddyForInvite!!,
                onDismiss = {
                    showInviteDialog = false
                    selectedBuddyForInvite = null
                },
                onSave = { title, language, date, time, duration, description ->
                    scope.launch {
                        inviteRepository.insertInvite(
                            SessionInviteEntity(
                                senderEmail = activeEmail,
                                receiverEmail = selectedBuddyForInvite!!.email,
                                title = title,
                                language = language.ifBlank { null },
                                date = date,
                                time = time,
                                duration = duration,
                                description = description.ifBlank { null }
                            )
                        )
                        showInviteDialog = false
                        selectedBuddyForInvite = null
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    sessions: List<Session>,
    chats: List<ChatPreview>,
    todaySummary: String,
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    onSessionClick: (Session) -> Unit,
    notifications: List<NotificationEntity>,
    onDismissNotification: (Int) -> Unit,
    buttonLabel: String = "View",
    userName: String = ""
) {
    var sectionsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { sectionsVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedSection(visible = sectionsVisible) { HomeHeaderCard(userName = userName) }
        AnimatedSection(visible = sectionsVisible) {
            StatsRow(
                todaySummary = todaySummary,
                rating = rating,
                onRatingChanged = onRatingChanged,
                ratingEditable = false
            )
        }
        AnimatedSection(visible = sectionsVisible) {
            SectionCard(title = "Upcoming Sessions", buttonLabel = "View all") {
                sessions.forEachIndexed { index, session ->
                    AnimatedListItem(index = index, visible = sectionsVisible) {
                        SessionItem(
                            session,
                            onClick = { onSessionClick(session) }
                        )
                        if (index < sessions.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
        AnimatedSection(visible = sectionsVisible) {
            SectionCard(title = "Recent Chats", buttonLabel = "View all") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    chats.forEachIndexed { index, chat ->
                        AnimatedListItem(index = index, visible = sectionsVisible) {
                            ChatItem(chat)
                        }
                    }
                }
            }
        }
        if (notifications.isNotEmpty()) {
            AnimatedSection(visible = sectionsVisible) {
                SectionCard(title = "Notifications", buttonLabel = "View") {
                    notifications.forEachIndexed { index, notif ->
                        AnimatedListItem(index = index, visible = sectionsVisible) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(notif.message, style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { onDismissNotification(notif.id) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuddyTabContent(
    padding: PaddingValues,
    currentEmail: String,
    users: List<com.example.languagebuddy.data.room.AccountEntity>,
    incomingRequests: List<com.example.languagebuddy.data.room.FriendRequestEntity>,
    outgoingRequests: List<com.example.languagebuddy.data.room.FriendRequestEntity>,
    friendships: List<com.example.languagebuddy.data.room.FriendshipEntity>,
    averageRatings: List<com.example.languagebuddy.data.room.FriendAverage>,
    givenRatings: Map<String, Float>,
    onSendRequest: (String) -> Unit,
    onAcceptRequest: (com.example.languagebuddy.data.room.FriendRequestEntity) -> Unit,
    onDeclineRequest: (com.example.languagebuddy.data.room.FriendRequestEntity) -> Unit,
    onRateFriend: (String, Float) -> Unit
) {
    val avgMap = remember(averageRatings) { averageRatings.associate { it.email to (it.average ?: 0f) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Buddy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Discover learners, send requests, and share feedback.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        val displayUsers = remember(users) { users.filter { it.email != currentEmail } }
        if (displayUsers.isEmpty()) {
            Text(text = "No other users yet.", color = Color.Gray)
        } else {
            displayUsers.forEach { user ->
                val isFriend = friendships.any { it.user1Email == user.email || it.user2Email == user.email }
                val incoming = incomingRequests.firstOrNull { it.fromEmail == user.email && it.status == "pending" }
                val outgoing = outgoingRequests.firstOrNull { it.toEmail == user.email && it.status == "pending" }
                val avgRating = avgMap[user.email] ?: 0f
                val myRating = givenRatings[user.email] ?: 0f
                BuddyUserCard(
                    user = user,
                    isFriend = isFriend,
                    incomingRequest = incoming,
                    outgoingRequest = outgoing,
                    averageRating = avgRating,
                    myRating = myRating,
                    onSendRequest = { onSendRequest(user.email) },
                    onAccept = { incoming?.let(onAcceptRequest) },
                    onDecline = { incoming?.let(onDeclineRequest) },
                    onRate = { onRateFriend(user.email, it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BuddyUserCard(
    user: com.example.languagebuddy.data.room.AccountEntity,
    isFriend: Boolean,
    incomingRequest: com.example.languagebuddy.data.room.FriendRequestEntity?,
    outgoingRequest: com.example.languagebuddy.data.room.FriendRequestEntity?,
    averageRating: Float,
    myRating: Float,
    onSendRequest: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onRate: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName?.takeIf { it.isNotBlank() } ?: user.name ?: user.email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!user.status.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = user.status.replaceFirstChar { it.uppercase() },
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (!user.languages.isNullOrBlank()) {
                        Text(
                            text = user.languages,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    if (!user.location.isNullOrBlank()) {
                        Text(
                            text = user.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3BA58B)
                        )
                    }
                }
                Text(
                    text = String.format("%.1f ★", averageRating),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFC107)
                )
            }
            when {
                isFriend -> {
                    Text(text = "Friends", color = Color(0xFF3BA58B), fontWeight = FontWeight.SemiBold)
                    RatingBar(
                        rating = if (myRating > 0f) myRating else averageRating,
                        onRatingChanged = onRate,
                        enabled = true
                    )
                }
                incomingRequest != null -> {
                    Text(text = "Request received", color = Color(0xFFFF9800), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val (declineScale, declineInteraction) = rememberPressAnimation()
                        val (acceptScale, acceptInteraction) = rememberPressAnimation()
                        OutlinedButton(
                            onClick = onDecline,
                            modifier = declineScale,
                            shape = RoundedCornerShape(50),
                            interactionSource = declineInteraction
                        ) { Text("Decline") }
                        Button(
                            onClick = onAccept,
                            modifier = acceptScale,
                            shape = RoundedCornerShape(50),
                            interactionSource = acceptInteraction
                        ) {
                            Text("Accept")
                        }
                    }
                }
                outgoingRequest != null -> {
                    Text(text = "Request sent", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
                else -> {
                    val (sendScale, sendInteraction) = rememberPressAnimation()
                    Button(
                        onClick = onSendRequest,
                        modifier = sendScale,
                        shape = RoundedCornerShape(50),
                        interactionSource = sendInteraction
                    ) { Text("Add Friend") }
                }
            }
        }
    }
}

@Composable
private fun ChatTabContent(
    padding: PaddingValues,
    currentAccountId: Int,
    currentEmail: String,
    chatRepository: com.example.languagebuddy.data.room.ChatRepository,
    accountRepository: AccountRepository
) {
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.provideFactory(chatRepository, currentAccountId))
    val chats by chatViewModel.chats.collectAsState()
    val otherAccounts by accountRepository.observeOtherUsers(currentEmail).collectAsState(initial = emptyList())
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "chatList",
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        composable("chatList") {
            ChatListScreen(
                chats = chats,
                currentAccountId = currentAccountId,
                accounts = otherAccounts,
                navController = navController,
                onStartNewChat = { navController.navigate("chatSelect") },
                onDeleteChat = { chatId -> chatViewModel.deleteChat(chatId) }
            )
        }
        composable("chatSelect") {
            SelectUserScreen(
                accounts = otherAccounts,
                currentAccountId = currentAccountId,
                onUserSelected = { userId ->
                    coroutineScope.launch {
                        val chatId = chatViewModel.startNewChat(userId)
                        navController.navigate("chat/$chatId/$userId")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "chat/{chatId}/{otherUserId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.IntType },
                navArgument("otherUserId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId") ?: 0
            val otherUserId = backStackEntry.arguments?.getInt("otherUserId") ?: 0
            val vm: com.example.languagebuddy.chat.ChatDetailViewModel = viewModel(
                factory = com.example.languagebuddy.chat.ChatDetailViewModel.provideFactory(
                    chatId,
                    otherUserId,
                    chatRepository,
                    accountRepository
                )
            )
            ChatDetailScreen(
                chatId = chatId,
                currentAccountId = currentAccountId,
                currentEmail = currentEmail,
                otherUserId = otherUserId,
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun ScheduleTabContent(
    padding: PaddingValues,
    sessions: List<Session>,
    pendingInvites: List<SessionInviteEntity>,
    onApproveInvite: (SessionInviteEntity) -> Unit,
    onDeclineInvite: (SessionInviteEntity) -> Unit,
    onSessionDetails: (Session) -> Unit,
    onDeleteSession: (Session) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Plan upcoming sessions and track your practice calendar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        if (pendingInvites.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pending Invitations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    pendingInvites.forEach { invite ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(invite.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            val details = listOfNotNull(invite.language?.takeIf { it.isNotBlank() }?.let { "Language: $it" }, "${invite.date} ${invite.time}", "Duration: ${invite.duration}")
                            Text(details.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val (approveScale, approveInteraction) = rememberPressAnimation()
                                val (declineScale, declineInteraction) = rememberPressAnimation()
                                Button(
                                    onClick = { onApproveInvite(invite) },
                                    modifier = approveScale.weight(1f),
                                    interactionSource = approveInteraction
                                ) { Text("Approve") }
                                OutlinedButton(
                                    onClick = { onDeclineInvite(invite) },
                                    modifier = declineScale.weight(1f),
                                    interactionSource = declineInteraction
                                ) { Text("Decline") }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
        sessions.forEachIndexed { index, session ->
            ScheduleItem(
                session,
                onDetails = { onSessionDetails(session) },
                onDelete = { onDeleteSession(session) }
            )
            if (index < sessions.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProfileTabContent(
    padding: PaddingValues,
    userSessions: List<Session>,
    accountName: String?,
    accountEmail: String?,
    account: com.example.languagebuddy.data.room.AccountEntity?,
    userPreferences: UserPreferences,
    rating: Float,
    onSaveProfile: (String, String, String, String, String, String, String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleDaily: (Boolean) -> Unit,
    onCreateSession: () -> Unit,
    onEditSession: (Session) -> Unit,
    onDeleteSession: (Session) -> Unit,
    onLogout: () -> Unit = {}
) {
    var displayName by rememberSaveable { mutableStateOf(userPreferences.displayName.ifBlank { accountName ?: "" }) }
    var languages by rememberSaveable { mutableStateOf(userPreferences.languages) }
    var specialties by rememberSaveable { mutableStateOf(account?.specialties ?: "") }
    var status by rememberSaveable { mutableStateOf(account?.status ?: "student") }
    var bio by rememberSaveable { mutableStateOf(account?.bio ?: "") }
    var availability by rememberSaveable { mutableStateOf(account?.availability ?: "") }
    var location by rememberSaveable { mutableStateOf(userPreferences.timeZone) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(userPreferences.notificationsEnabled) }
    var dailyReminder by rememberSaveable { mutableStateOf(userPreferences.dailyReminder) }

    val languageOptions = listOf("English", "Spanish", "French", "Japanese", "Mandarin", "Arabic", "German")
    val selectedLanguages = remember { mutableStateListOf<String>() }
    var languagePickerExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(account, userPreferences) {
        displayName = userPreferences.displayName.ifBlank { accountName ?: userPreferences.displayName }
        val parsed = userPreferences.languages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        selectedLanguages.clear()
        selectedLanguages.addAll(parsed)
        languages = if (selectedLanguages.isEmpty()) "" else selectedLanguages.joinToString(", ")
        location = userPreferences.timeZone
        notificationsEnabled = userPreferences.notificationsEnabled
        dailyReminder = userPreferences.dailyReminder
        status = account?.status ?: status
        bio = account?.bio ?: bio
        specialties = account?.specialties ?: specialties
        availability = account?.availability ?: availability
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GradientHeaderCard(
            name = displayName.ifBlank { accountName.orEmpty() },
            email = accountEmail.orEmpty(),
            rating = rating
        )

        ProfileInfoCard(
            displayName = displayName,
            languages = languages,
            location = location,
            languageOptions = languageOptions,
            languagePickerExpanded = languagePickerExpanded,
            selectedLanguages = selectedLanguages,
            onDisplayNameChange = { displayName = it },
            onLanguagesChange = { languages = it },
            onLocationChange = { location = it },
            onToggleLanguages = { languagePickerExpanded = it },
            onSave = {
                languagePickerExpanded = false
                onSaveProfile(displayName.trim(), languages.trim(), specialties.trim(), status, bio.trim(), availability.trim(), location.trim())
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "About You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusDropdown(
                    status = status,
                    onStatusChange = { status = it }
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialties,
                    onValueChange = { specialties = it },
                    label = { Text("Specialties (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = availability,
                    onValueChange = { availability = it },
                    label = { Text("Availability") },
                    modifier = Modifier.fillMaxWidth()
                )
                val (aboutSaveScale, aboutSaveInteraction) = rememberPressAnimation()
                Button(
                    onClick = {
                        onSaveProfile(
                            displayName.trim(),
                            languages.trim(),
                            specialties.trim(),
                            status.trim(),
                            bio.trim(),
                            availability.trim(),
                            location.trim()
                        )
                    },
                    modifier = aboutSaveScale.fillMaxWidth(),
                    interactionSource = aboutSaveInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7AD7F0))
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        PreferencesCard(
            notificationsEnabled = notificationsEnabled,
            dailyReminder = dailyReminder,
            onNotificationsChange = {
                notificationsEnabled = it
                onToggleNotifications(it)
            },
            onDailyChange = {
                dailyReminder = it
                onToggleDaily(it)
            }
        )

        EventsCard(
            userSessions = userSessions,
            onCreateSession = onCreateSession,
            onEditSession = onEditSession,
            onDeleteSession = onDeleteSession
        )

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Log Out")
        }
    }
}

@Composable
private fun GradientHeaderCard(name: String, email: String, rating: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF7AD7F0), Color(0xFF6ADCC8))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = email, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format("Rating %.1f ★", rating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Streak • Keep going!",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    displayName: String,
    languages: String,
    location: String,
    languageOptions: List<String>,
    languagePickerExpanded: Boolean,
    selectedLanguages: MutableList<String>,
    onDisplayNameChange: (String) -> Unit,
    onLanguagesChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onToggleLanguages: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Profile Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            val arrowRotation = if (languagePickerExpanded) 180f else 0f
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = languages,
                    onValueChange = { onLanguagesChange(it) },
                    label = { Text("Languages") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onToggleLanguages(!languagePickerExpanded) }
                )
            }
            if (languagePickerExpanded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        languageOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedLanguages.contains(option)) selectedLanguages.remove(option) else selectedLanguages.add(option)
                                        onLanguagesChange(selectedLanguages.joinToString(", "))
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(option)
                                Checkbox(
                                    checked = selectedLanguages.contains(option),
                                    onCheckedChange = {
                                        if (it) selectedLanguages.add(option) else selectedLanguages.remove(option)
                                        onLanguagesChange(selectedLanguages.joinToString(", "))
                                    }
                                )
                            }
                        }
                    }
                }
            }
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            val (saveScale, saveInteraction) = rememberPressAnimation()
            Button(
                onClick = onSave,
                modifier = saveScale.fillMaxWidth(),
                interactionSource = saveInteraction,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7AD7F0))
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PreferencesCard(
    notificationsEnabled: Boolean,
    dailyReminder: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    onDailyChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ProfileToggleRow(
                title = "Session reminders",
                subtitle = "Get notified 15 minutes before a session",
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChange
            )
            ProfileToggleRow(
                title = "Daily practice nudges",
                subtitle = "Light reminders to keep your streak",
                checked = dailyReminder,
                onCheckedChange = onDailyChange
            )
        }
    }
}

@Composable
private fun EventsCard(
    userSessions: List<Session>,
    onCreateSession: () -> Unit,
    onEditSession: (Session) -> Unit,
    onDeleteSession: (Session) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "My Events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onCreateSession) { Text("Create") }
            }
            if (userSessions.isEmpty()) {
                Text(text = "No events yet. Create your first session!", color = Color.Gray)
            } else {
                userSessions.forEach { session ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = session.title, fontWeight = FontWeight.SemiBold)
                            Text(text = "${session.day} ${session.time}", color = Color.Gray)
                            if (session.language.isNotBlank()) {
                                Text(
                                    text = "Language: ${session.language}",
                                    color = Color(0xFF3BA58B),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onEditSession(session) }) { Text("Edit") }
                            TextButton(onClick = { onDeleteSession(session) }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    if (session != userSessions.last()) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderCard(userName: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning,"
        hour < 18 -> "Good afternoon,"
        else -> "Good evening,"
    }
    val firstName = userName.split(" ").firstOrNull().orEmpty().ifBlank { "there" }
    val userInitial = firstName.firstOrNull()?.uppercase() ?: "?"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF6A5AE0), Color(0xFF8EC5FC))
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(userInitial, fontWeight = FontWeight.Bold, color = Color(0xFF6A5AE0))
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    greeting,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    firstName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ready to practice with your buddies?",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StatsRow(
    todaySummary: String,
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    ratingEditable: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        TodayCard(dateText = todaySummary)
        RatingStatCard(rating = rating, onRatingChanged = onRatingChanged, enabled = ratingEditable)
    }
}

@Composable
private fun RowScope.TodayCard(dateText: String) {
    val parsedDate = remember(dateText) {
        runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateText)
        }.getOrNull()
    }
    val displayDate = parsedDate?.let {
        SimpleDateFormat("MMMM dd", Locale.getDefault()).format(it)
    } ?: dateText

    Card(
        modifier = Modifier
            .weight(1f)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Today", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(displayDate, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Keep your streak going", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RowScope.RatingStatCard(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Rating", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(String.format("%.1f", rating), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { index ->
                    val tintColor = Color(0xFFFFC107)
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier
                            .size(20.dp)
                            .let { base ->
                                if (enabled) base.clickable { onRatingChanged(index + 1f) } else base
                            }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Based on user feedback", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    enabled: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { index ->
            val filled = rating >= index
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Set rating to $index",
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .size(26.dp)
                    .let { base ->
                        if (enabled) base.clickable { onRatingChanged(index.toFloat()) } else base
                    }
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    buttonLabel: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = buttonLabel, color = Color(0xFF3BA58B), fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SessionItem(session: Session, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(64.dp)
                    .background(session.accentColor, shape = RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            val hostInitial = session.host.firstOrNull()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(session.accentColor.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hostInitial,
                    color = session.accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Host • ${session.host}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (session.language.isNotBlank()) {
                    Text(
                        text = "Language: ${session.language}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${session.day} ${session.time}  •  ${session.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = session.rating.toString(), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AnimatedSection(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 250)) + slideInVertically(
            animationSpec = tween(durationMillis = 250),
            initialOffsetY = { it / 6 }
        )
    ) {
        content()
    }
}

@Composable
private fun AnimatedListItem(
    index: Int,
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = index * 40))
    ) {
        content()
    }
}

@Composable
private fun ChatItem(chat: ChatPreview, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(chat.accentColor.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.name.first().toString(),
                    color = chat.accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chat.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun rememberPressAnimation(): Pair<Modifier, MutableInteractionSource> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale"
    )
    return Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    } to interactionSource
}

@Composable
private fun SelectUserScreen(
    accounts: List<Account>,
    currentAccountId: Int,
    onUserSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Start a new chat", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
        }
        accounts.filter { it.accountId != currentAccountId }.forEach { account ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserSelected(account.accountId) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = account.displayName?.takeIf { it.isNotBlank() } ?: account.email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!account.languages.isNullOrBlank()) {
                        Text(
                            text = account.languages,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleItem(session: Session, onDetails: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val hostInitial = session.host.firstOrNull()?.toString() ?: "?"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(session.accentColor.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hostInitial,
                        color = session.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "With ${session.host}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    if (session.language.isNotBlank()) {
                        Text(
                            text = "Language: ${session.language}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3BA58B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${session.day} ${session.time}  •  ${session.duration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val (detailsScale, detailsInteraction) = rememberPressAnimation()
                val (removeScale, removeInteraction) = rememberPressAnimation()
                OutlinedButton(
                    onClick = onDetails,
                    modifier = detailsScale,
                    interactionSource = detailsInteraction
                ) {
                    Text("Details")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = removeScale,
                    interactionSource = removeInteraction
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ProfileToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewChatDialog(
    onDismiss: () -> Unit,
    accounts: List<Account>,
    onSave: (String, String) -> Unit
) {
    var message by rememberSaveable { mutableStateOf("") }
    var selectedReceiverEmail by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    val selectedLabel = accounts
        .firstOrNull { it.email == selectedReceiverEmail }
        ?.let { formatAccountDisplay(it) }
        .orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Start a chat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it }
                ) {
                    TextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select user") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(formatAccountDisplay(account)) },
                                onClick = {
                                    selectedReceiverEmail = account.email
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val canSave = selectedReceiverEmail.isNotBlank() && message.isNotBlank()
            TextButton(
                onClick = {
                    if (canSave) {
                        onSave(selectedReceiverEmail, message.trim())
                    }
                },
                enabled = canSave
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SessionDetailDialog(
    session: Session,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = session.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "With ${session.host}", fontWeight = FontWeight.SemiBold)
                if (session.language.isNotBlank()) {
                    Text(text = "Language: ${session.language}")
                }
                Text(text = "${session.day} at ${session.time}")
                Text(text = "Duration: ${session.duration}")
                if (session.description.isNotBlank()) {
                    Text(
                        text = session.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = session.rating.toString(), fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = "Prepare a few phrases and join on time. You can reschedule up to 2 hours before start.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Start
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private fun SessionWithHost.toSession(): Session = Session(
    id = session.sessionId,
    title = session.title,
    host = hostName,
    hostEmail = session.hostEmail,
    language = session.language.orEmpty(),
    day = session.date,
    time = session.time,
    duration = session.duration,
    rating = 0.0,
    accentColor = deriveAccentColor(session.sessionId),
    description = session.description.orEmpty(),
    originalHostEventId = null
)

private fun deriveAccentColor(seed: String): Color {
    val colors = listOf(Color(0xFF8C7AE6), Color(0xFF6D9EDE), Color(0xFF3BA58B), Color(0xFFF2A14A))
    val index = kotlin.math.abs(seed.hashCode()) % colors.size
    return colors[index]
}

private fun formatAccountDisplay(account: Account): String {
    val displayName = account.displayName?.takeIf { it.isNotBlank() }
        ?: account.name?.takeIf { it.isNotBlank() }
        ?: account.email
    return "$displayName (${account.email})"
}

private fun com.example.languagebuddy.data.room.MessageEntity.threadId(): String {
    val delimiterIndex = messageId.indexOf('|')
    if (delimiterIndex > 0) return messageId.substring(0, delimiterIndex)
    return listOf(senderEmail, receiverEmail).sorted().joinToString("|")
}

private fun chatIdFor(a: String, b: String): Int =
    listOf(a, b).sorted().joinToString("|").hashCode()

private fun List<com.example.languagebuddy.data.room.MessageEntity>.toChatPreviews(
    currentUserId: Int,
    accounts: List<Account>
): List<ChatPreview> {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val accountById = accounts.associateBy { it.accountId }

    return groupBy { it.chatId }
        .mapNotNull { (_, messages) ->
            val latest = messages.maxByOrNull { it.timestamp } ?: return@mapNotNull null
            val counterpartId = if (latest.senderId == currentUserId) latest.receiverId else latest.senderId
            if (counterpartId == currentUserId) return@mapNotNull null
            val account = accountById[counterpartId]
            val displayName = account?.displayName?.takeIf { it.isNotBlank() }
                ?: account?.name?.takeIf { it.isNotBlank() }
                ?: account?.email
                ?: if (latest.senderId == currentUserId) latest.receiverEmail else latest.senderEmail
            val counterpartEmail = account?.email ?: if (latest.senderId == currentUserId) latest.receiverEmail else latest.senderEmail
            ChatPreview(
                chatId = latest.chatId,
                threadId = latest.chatId.toString(),
                counterpartEmail = counterpartEmail,
                counterpartId = counterpartId,
                name = displayName,
                message = latest.content,
                timeAgo = formatter.format(Date(latest.timestamp)),
                accentColor = deriveAccentColor(counterpartEmail),
                lastTimestamp = latest.timestamp
            )
        }
        .sortedByDescending { it.lastTimestamp }
}

@Composable
private fun SessionCreateDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit,
    initial: Session? = null
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var language by rememberSaveable { mutableStateOf(initial?.language ?: "") }
    var date by rememberSaveable { mutableStateOf(initial?.day ?: "") }
    var time by rememberSaveable { mutableStateOf(initial?.time ?: "") }
    var duration by rememberSaveable { mutableStateOf(initial?.duration ?: "") }
    var description by rememberSaveable { mutableStateOf(initial?.description ?: "") }

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = "${month + 1}/$dayOfMonth/$year"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val h = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                val ampm = if (hourOfDay >= 12) "PM" else "AM"
                time = "%d:%02d %s".format(h, minute, ampm)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Spanish") }
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { /* readonly */ },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Select date") }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker() }
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { /* readonly */ },
                        label = { Text("Time") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Select time") }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showTimePicker() }
                    )
                }
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g. 30 min)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && language.isNotBlank() && date.isNotBlank() && time.isNotBlank() && duration.isNotBlank()) {
                        onSave(
                            title.trim(),
                            language.trim(),
                            date.trim(),
                            time.trim(),
                            duration.trim(),
                            description.trim()
                        )
                        onDismiss()
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    status: String,
    onStatusChange: (String) -> Unit
    ) {
    val options = listOf("teacher", "student", "volunteer")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = status,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onStatusChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionInviteDialog(
    recipient: Account,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = "${month + 1}/$dayOfMonth/$year"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val h = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                val ampm = if (hourOfDay >= 12) "PM" else "AM"
                time = "%d:%02d %s".format(h, minute, ampm)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite ${recipient.displayName ?: recipient.email}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Select date") }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker() }
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Time") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Select time") }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showTimePicker() }
                    )
                }
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && duration.isNotBlank()) {
                    onSave(
                        title.trim(),
                        language.trim(),
                        date.trim(),
                        time.trim(),
                        duration.trim(),
                        description.trim()
                    )
                }
            }) { Text("Send Invite") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
