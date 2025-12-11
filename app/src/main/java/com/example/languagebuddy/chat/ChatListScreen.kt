@file:OptIn(androidx.compose.material.ExperimentalMaterialApi::class)

package com.example.languagebuddy.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.languagebuddy.data.Account
import com.example.languagebuddy.data.room.MessageEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    chats: List<MessageEntity>,
    currentAccountId: Int,
    accounts: List<Account>,
    navController: NavController,
    onStartNewChat: () -> Unit,
    onDeleteChat: (Int) -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val accountLookup = remember(accounts) { accounts.associateBy { it.accountId } }

    val showConfirmMap = remember { mutableStateMapOf<Int, Boolean>() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Start or resume conversations with your language buddies.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        OutlinedButton(
            onClick = onStartNewChat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start a new chat")
        }

        chats.forEachIndexed { index, chat ->

            val otherUserId =
                if (chat.senderId == currentAccountId) chat.receiverId else chat.senderId
            if (otherUserId == currentAccountId) return@forEachIndexed

            val counterpart = accountLookup[otherUserId]

            val displayName =
                counterpart?.displayName?.takeIf { it.isNotBlank() }
                    ?: counterpart?.name?.takeIf { it.isNotBlank() }
                    ?: counterpart?.email
                    ?: if (chat.senderId == currentAccountId)
                        chat.receiverEmail else chat.senderEmail

            // Correct Material 2 dismiss state
            val dismissState = rememberDismissState { value ->
                if (value == DismissValue.DismissedToStart ||
                    value == DismissValue.DismissedToEnd
                ) {
                    showConfirmMap[chat.chatId] = true
                }
                false
            }

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.EndToStart),
                background = {
                    val bgColor =
                        if (dismissState.targetValue == DismissValue.DismissedToStart)
                            Color.Red else Color.White

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                },
                dismissContent = {
                    ConversationRow(
                        name = displayName ?: "Unknown",
                        lastMessage = chat.content,
                        timestamp = formatter.format(Date(chat.timestamp)),
                        onClick = {
                            navController.navigate("chat/${chat.chatId}/${otherUserId}")
                        }
                    )
                }
            )

            if (index < chats.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.Transparent
                )
            }

            if (showConfirmMap[chat.chatId] == true) {
                AlertDialog(
                    onDismissRequest = {
                        showConfirmMap[chat.chatId] = false
                        scope.launch { dismissState.reset() }
                    },
                    title = { Text("Delete chat?") },
                    text = { Text("Are you sure you want to delete this conversation?") },
                    confirmButton = {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    onDeleteChat(chat.chatId)
                                    showConfirmMap[chat.chatId] = false
                                    dismissState.reset()
                                }
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                showConfirmMap[chat.chatId] = false
                                scope.launch { dismissState.reset() }
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ConversationRow(
    name: String,
    lastMessage: String,
    timestamp: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color(0xFFE3E0FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().uppercaseChar().toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4AE3)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = timestamp,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
