package com.example.languagebuddy.buddy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.languagebuddy.data.Account
import com.example.languagebuddy.ui.theme.LanguageBuddyTheme

@Composable
fun BuddyDiscoverScreen(
    viewModel: BuddyViewModel,
    currentEmail: String?,
    onMessage: (Account) -> Unit,
    onSchedule: (Account) -> Unit
) {
    val accounts by viewModel.results.collectAsState()
    val selectedTab by viewModel.selected.collectAsState()
    val scrollState = rememberScrollState()
    LaunchedEffect(currentEmail) { viewModel.setCurrentEmail(currentEmail) }
    LanguageBuddyTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GradientHeader()
            SearchBar(
                onSearch = { query -> viewModel.setQuery(query) }
            )
            FilterTabs(
                selected = selectedTab,
                onSelected = { tab -> viewModel.setTab(tab) }
            )
            accounts.forEach { account ->
                BuddyCard(account = account, onMessage = { onMessage(account) }, onSchedule = { onSchedule(account) })
            }
        }
    }
}

@Composable
private fun GradientHeader() {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF7F7FD5), Color(0xFF86A8E7), Color(0xFF91EAE4)))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape = RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Find Language Buddies", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Search mentors, peers, and volunteers to practice with.", color = Color.White)
            }
        }
    }
}

@Composable
private fun SearchBar(onSearch: (String) -> Unit) {
    val text = remember { androidx.compose.runtime.mutableStateOf("") }
    OutlinedTextField(
        value = text.value,
        onValueChange = {
            text.value = it
            onSearch(it)
        },
        placeholder = { Text("Search by name, specialty, language...") },
        modifier = Modifier
            .fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White
        )
    )
}

@Composable
private fun FilterTabs(selected: String?, onSelected: (String) -> Unit) {
    val tabs = listOf("All", "Teachers", "Students", "Volunteers")
    val normalizedSelected = selected?.lowercase()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val normalizedTab = when (tab.lowercase()) {
                "teachers" -> "teacher"
                "students" -> "student"
                "volunteers" -> "volunteer"
                else -> "all"
            }
            val isSelected = normalizedSelected == normalizedTab || normalizedSelected == tab.lowercase()
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelected(tab) },
                color = if (isSelected) Color(0xFF3BA58B) else Color(0x143BA58B)
            ) {
                Text(
                    text = tab,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color(0xFF3BA58B),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BuddyCard(
    account: Account,
    onMessage: () -> Unit,
    onSchedule: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.displayName?.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold)
                }
            Column(modifier = Modifier.weight(1f)) {
                Text(account.displayName ?: account.email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${account.rating}", color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold)
                    Text("(${account.ratingCount})", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    if (!account.location.isNullOrBlank()) {
                        Text("• ${account.location}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            StatusPill(account.status)
        }
            if (!account.bio.isNullOrBlank()) {
                Text(account.bio ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.DarkGray)
            }
            if (!account.languages.isNullOrBlank()) {
                ChipRow(label = "Languages", values = account.languages.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
            if (!account.specialties.isNullOrBlank()) {
                ChipRow(label = "Specialties", values = account.specialties.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
            if (!account.availability.isNullOrBlank()) {
                Text("Availability: ${account.availability}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onMessage, modifier = Modifier.weight(1f)) {
                    Text("Message")
                }
                Button(onClick = onSchedule, modifier = Modifier.weight(1f)) {
                    Text("Schedule")
                }
            }
        }
    }
}

@Composable
private fun ChipRow(label: String, values: List<String>) {
    if (values.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            values.forEach { value ->
                Surface(
                    color = Color(0x143BA58B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = value,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color(0xFF3BA58B),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val color = when (status.lowercase()) {
        "teacher" -> Color(0xFF4C6FFF)
        "volunteer" -> Color(0xFF00BFA5)
        else -> Color(0xFF9C27B0)
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
