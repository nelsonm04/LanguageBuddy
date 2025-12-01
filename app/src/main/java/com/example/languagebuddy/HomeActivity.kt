package com.example.languagebuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import com.example.languagebuddy.data.AccountRepository
import com.example.languagebuddy.data.StoredSession
import com.example.languagebuddy.data.SessionRepository
import com.example.languagebuddy.data.UserPreferences
import com.example.languagebuddy.data.UserPreferencesRepository
import com.example.languagebuddy.data.accountDataStore
import com.example.languagebuddy.data.sessionDataStore
import com.example.languagebuddy.data.userPrefsDataStore
import com.example.languagebuddy.ui.theme.LanguageBuddyTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.mutableStateListOf

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageBuddyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen()
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
    val day: String,
    val time: String,
    val duration: String,
    val rating: Double,
    val accentColor: Color,
    val description: String = ""
)

private data class ChatPreview(
    val name: String,
    val message: String,
    val timeAgo: String,
    val accentColor: Color
)

private data class Buddy(
    val name: String,
    val languages: String,
    val availability: String,
    val accentColor: Color
)

@Composable
private fun HomeScreen() {
    val context = LocalContext.current
    val userPreferencesRepository = remember { UserPreferencesRepository(context.userPrefsDataStore) }
    val accountRepository = remember { AccountRepository(context.accountDataStore) }
    val sessionRepository = remember { SessionRepository(context.sessionDataStore) }
    val account by accountRepository.accountFlow.collectAsState(initial = null)
    val activeEmail = account?.email ?: "default"
    val userPrefs by userPreferencesRepository.preferencesFlow(activeEmail).collectAsState(initial = UserPreferences())
    val displayNameForSessions = userPrefs.displayName.ifBlank { account?.name ?: "You" }
    val scope = rememberCoroutineScope()

    val sessions = listOf(
        Session(
            title = "Job Interview Practice",
            host = "Maria Rodriguez",
            day = "Today",
            time = "3:00 PM",
            duration = "30 min",
            rating = 4.9,
            accentColor = Color(0xFF8C7AE6)
        ),
        Session(
            title = "Medical Appointment",
            host = "James Wilson",
            day = "Tomorrow",
            time = "11:00 AM",
            duration = "20 min",
            rating = 4.8,
            accentColor = Color(0xFF6D9EDE)
        )
    )

    val userSessions by sessionRepository.sessionsFlow(activeEmail)
        .map { stored -> stored.map { it.toSession() } }
        .collectAsState(initial = emptyList())

    val chats = listOf(
        ChatPreview(
            name = "Sarah Chen",
            message = "Great practice session! Your pronunciation is improving.",
            timeAgo = "2h ago",
            accentColor = Color(0xFF42C9C7)
        ),
        ChatPreview(
            name = "Ahmed Hassan",
            message = "Looking forward to our restaurant conversation practice!",
            timeAgo = "1d ago",
            accentColor = Color(0xFFF2A14A)
        )
    )

    val buddies = listOf(
        Buddy(
            name = "Julia Nakamura",
            languages = "Japanese · English",
            availability = "Weeknights, 7-9 PM",
            accentColor = Color(0xFF8C7AE6)
        ),
        Buddy(
            name = "Carlos Mendoza",
            languages = "Spanish · English",
            availability = "Lunch hours, 12-2 PM",
            accentColor = Color(0xFF3BA58B)
        ),
        Buddy(
            name = "Amira El-Sayed",
            languages = "Arabic · French",
            availability = "Weekends, 10 AM-1 PM",
            accentColor = Color(0xFF6D9EDE)
        )
    )

    val chatItems = remember { mutableStateListOf<ChatPreview>().apply { addAll(chats) } }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedSession by remember { mutableStateOf<Session?>(null) }

    var showAddSessionDialog by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<Session?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = {
                    editingSession = null
                    showAddSessionDialog = true
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add session")
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navItems = listOf(
                    Icons.Filled.Home to "Home",
                    Icons.Outlined.Group to "Buddy",
                    Icons.Filled.ChatBubble to "Chat",
                    Icons.Filled.Schedule to "Schedule",
                    Icons.Filled.Person to "Profile"
                )

                navItems.forEachIndexed { index, (icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF3BA58B),
                            selectedTextColor = Color(0xFF3BA58B)
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTabContent(
                padding = padding,
                sessions = sessions + userSessions,
                chats = chatItems,
                onSessionClick = { selectedSession = it }
            )
            1 -> BuddyTabContent(
                padding = padding,
                buddies = buddies,
                onScheduleBuddy = { buddy ->
                    Toast.makeText(context, "Scheduled time with ${buddy.name}", Toast.LENGTH_SHORT).show()
                }
            )
            2 -> ChatTabContent(padding = padding, chats = chatItems)
            3 -> ScheduleTabContent(
                padding = padding,
                sessions = sessions + userSessions,
                onSessionDetails = { selectedSession = it }
            )
            4 -> ProfileTabContent(
                padding = padding,
                accountName = account?.name,
                accountEmail = account?.email,
                userPreferences = userPrefs,
                onSaveProfile = { name, languages, timeZone ->
                    scope.launch {
                        userPreferencesRepository.updateProfile(activeEmail, name, languages, timeZone)
                        val newHost = name.ifBlank { account?.name ?: "You" }
                        userSessions.forEach { session ->
                            sessionRepository.upsert(activeEmail, session.copy(host = newHost).toStored())
                        }
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
                    scope.launch { sessionRepository.delete(activeEmail, session.id) }
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
            else -> HomeTabContent(
                padding = padding,
                sessions = sessions + userSessions,
                chats = chatItems,
                onSessionClick = { selectedSession = it }
            )
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
                onSave = { title, date, time, duration, description ->
                    val colorOptions = listOf(Color(0xFF8C7AE6), Color(0xFF6D9EDE), Color(0xFF3BA58B), Color(0xFFF2A14A))
                    val existing = editingSession
                    val accent = existing?.accentColor ?: colorOptions.random()
                    val rating = existing?.rating ?: 0.0
                    val hostName = displayNameForSessions
                    val session = Session(
                        id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title,
                        host = hostName,
                        hostEmail = activeEmail,
                        day = date,
                        time = time,
                        duration = duration,
                        rating = rating,
                        accentColor = accent,
                        description = description
                    )
                    scope.launch { sessionRepository.upsert(activeEmail, session.toStored()) }
                    editingSession = null
                    showAddSessionDialog = false
                }
            )
        }
    }
}

@Composable
private fun HomeTabContent(
    padding: PaddingValues,
    sessions: List<Session>,
    chats: List<ChatPreview>,
    onSessionClick: (Session) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeCard()
        StatsRow()
        SectionCard(title = "Upcoming Sessions", buttonLabel = "View all") {
            sessions.forEachIndexed { index, session ->
                SessionItem(
                    session,
                    onClick = { onSessionClick(session) }
                )
                if (index < sessions.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        SectionCard(title = "Recent Chats", buttonLabel = "View all") {
            chats.forEachIndexed { index, chat ->
                ChatItem(chat)
                if (index < chats.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun BuddyTabContent(
    padding: PaddingValues,
    buddies: List<Buddy>,
    onScheduleBuddy: (Buddy) -> Unit
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
            text = "Your Buddies",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Find the right practice partner for your next session.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        buddies.forEach { buddy ->
            BuddyItem(buddy, onSchedule = { onScheduleBuddy(buddy) })
        }
    }
}

@Composable
private fun ChatTabContent(
    padding: PaddingValues,
    chats: SnapshotStateList<ChatPreview>
) {
    var showNewChatDialog by remember { mutableStateOf(false) }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onSave = { name, message ->
                chats.add(0, ChatPreview(name = name, message = message, timeAgo = "Just now", accentColor = Color(0xFF3BA58B)))
                showNewChatDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Start or resume conversations with your language buddies.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        OutlinedButton(
            onClick = { showNewChatDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start a new chat")
        }
        chats.forEachIndexed { index, chat ->
            ChatItem(chat)
            if (index < chats.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun ScheduleTabContent(
    padding: PaddingValues,
    sessions: List<Session>,
    onSessionDetails: (Session) -> Unit
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
        sessions.forEachIndexed { index, session ->
            ScheduleItem(session, onDetails = { onSessionDetails(session) })
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
    userPreferences: UserPreferences,
    onSaveProfile: (String, String, String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleDaily: (Boolean) -> Unit,
    onCreateSession: () -> Unit,
    onEditSession: (Session) -> Unit,
    onDeleteSession: (Session) -> Unit,
    onLogout: () -> Unit = {}
) {
    var displayName by rememberSaveable { mutableStateOf(userPreferences.displayName.ifBlank { accountName ?: "" }) }
    var languages by rememberSaveable { mutableStateOf(userPreferences.languages) }
    var location by rememberSaveable { mutableStateOf(userPreferences.timeZone) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(userPreferences.notificationsEnabled) }
    var dailyReminder by rememberSaveable { mutableStateOf(userPreferences.dailyReminder) }

    val languageOptions = listOf("English", "Spanish", "French", "Japanese", "Mandarin", "Arabic", "German")
    val selectedLanguages = remember { mutableStateListOf<String>() }
    var languagePickerExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userPreferences) {
        displayName = userPreferences.displayName.ifBlank { accountName ?: userPreferences.displayName }
        val parsed = userPreferences.languages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        selectedLanguages.clear()
        selectedLanguages.addAll(parsed)
        languages = if (selectedLanguages.isEmpty()) "" else selectedLanguages.joinToString(", ")
        location = userPreferences.timeZone
        notificationsEnabled = userPreferences.notificationsEnabled
        dailyReminder = userPreferences.dailyReminder
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Update your goals, location, and availability.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = accountName.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = accountEmail.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your name") }
                )
                val arrowRotation = if (languagePickerExpanded) 180f else 0f
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = languages,
                        onValueChange = { /* read-only */ },
                        label = { Text("Languages") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Select languages") },
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
                            .clickable { languagePickerExpanded = !languagePickerExpanded }
                    )
                }
                if (languagePickerExpanded) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            languageOptions.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (selectedLanguages.contains(option)) selectedLanguages.remove(option) else selectedLanguages.add(option)
                                            languages = selectedLanguages.joinToString(", ")
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(option)
                                    Checkbox(
                                        checked = selectedLanguages.contains(option),
                                        onCheckedChange = {
                                            if (it) selectedLanguages.add(option) else selectedLanguages.remove(option)
                                            languages = selectedLanguages.joinToString(", ")
                                        }
                                    )
                                }
                                if (option != languageOptions.last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("City, Country") }
                )
                Button(
                    onClick = {
                        languagePickerExpanded = false
                        onSaveProfile(displayName.trim(), languages.trim(), location.trim())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save profile")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ProfileToggleRow(
                    title = "Session reminders",
                    subtitle = "Get notified 15 minutes before a session",
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        onToggleNotifications(it)
                    }
                )
                ProfileToggleRow(
                    title = "Daily practice nudges",
                    subtitle = "Light reminders to keep your streak",
                    checked = dailyReminder,
                    onCheckedChange = {
                        dailyReminder = it
                        onToggleDaily(it)
                    }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "My Events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onCreateSession) { Text("Create") }
                }
                if (userSessions.isEmpty()) {
                    Text(text = "No events yet.", color = Color.Gray)
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
private fun WelcomeCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4DE0FF), Color(0xFF59F3C8))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready to practice with your language buddies?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubble,
                    contentDescription = "Chats",
                    tint = Color(0xFF55B5EA)
                )
            }
        }
    }
}

@Composable
private fun StatsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(title = "This Week", value = "4")
        StatCard(title = "Minutes", value = "145")
        StatCard(title = "Rating", value = "4.7")
    }
}

@Composable
private fun RowScope.StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(session.accentColor.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = session.host.first().toString(),
                color = session.accentColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.host,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = session.title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${session.day} ${session.time}  •  ${session.duration}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
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

@Composable
private fun ChatItem(chat: ChatPreview) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = chat.timeAgo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun ScheduleItem(session: Session, onDetails: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
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
                        text = session.host.first().toString(),
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
                    Text(
                        text = "${session.day} ${session.time}  •  ${session.duration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            OutlinedButton(onClick = onDetails) {
                Text("Details")
            }
        }
    }
}

@Composable
private fun BuddyItem(buddy: Buddy, onSchedule: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(buddy.accentColor.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buddy.name.first().toString(),
                    color = buddy.accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buddy.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = buddy.languages,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = buddy.availability,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            OutlinedButton(onClick = onSchedule) {
                Text(text = "Schedule", color = Color(0xFF3BA58B), fontWeight = FontWeight.SemiBold)
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

@Composable
private fun NewChatDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Start a chat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && message.isNotBlank()) {
                        onSave(name.trim(), message.trim())
                    }
                }
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

private fun StoredSession.toSession(): Session = Session(
    id = id,
    title = title,
    host = host,
    hostEmail = hostEmail,
    day = day,
    time = time,
    duration = duration,
    rating = rating,
    accentColor = Color(accentColor.toULong()),
    description = description
)

private fun Session.toStored(): StoredSession = StoredSession(
    id = id,
    title = title,
    host = host,
    hostEmail = hostEmail,
    day = day,
    time = time,
    duration = duration,
    rating = rating,
    accentColor = accentColor.toArgb().toLong() and 0xFFFFFFFFL,
    description = description
)

@Composable
private fun SessionCreateDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
    initial: Session? = null
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
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
                    if (title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && duration.isNotBlank()) {
                        onSave(title.trim(), date.trim(), time.trim(), duration.trim(), description.trim())
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
