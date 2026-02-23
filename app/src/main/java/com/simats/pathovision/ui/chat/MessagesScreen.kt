package com.simats.pathovision.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simats.pathovision.models.Conversation
import com.simats.pathovision.utils.Resource
import com.simats.pathovision.utils.UrlUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val PrimaryBlue = Color(0xFF4A6FE3)
private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val CardWhite = Color.White
private val BadgeRed = Color(0xFFEF4444)

@Composable
fun MessagesScreen(
    onNavigateBack: () -> Unit = {},
    onConversationClick: (Conversation) -> Unit = { },
    viewModel: ChatViewModel = hiltViewModel()
) {
    val inboxState by viewModel.inboxState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextDark, modifier = Modifier.size(24.dp))
                }
                Text(
                    text = "Messages",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
            }
            IconButton(onClick = { }) {
                Icon(Icons.Rounded.Sort, contentDescription = "Sort", tint = TextGray, modifier = Modifier.size(22.dp))
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by patient or case ID...", color = TextGray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextGray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = CardWhite,
                unfocusedContainerColor = CardWhite
            )
        )

        // Messages List
        when (val state = inboxState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Error",
                            tint = TextGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.message ?: "Failed to load messages", color = TextGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadInbox() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is Resource.Success -> {
                val conversations = state.data ?: emptyList()
                val filtered = if (searchQuery.isBlank()) {
                    conversations
                } else {
                    conversations.filter {
                        it.case_title.contains(searchQuery, ignoreCase = true) ||
                        it.other_user.name.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "No messages",
                                tint = TextGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No messages yet", color = TextGray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { conversation ->
                            ConversationListItem(
                                conversation = conversation,
                                onClick = {
                                    onConversationClick(conversation)
                                }
                            )
                        }
                    }
                }
            }

            null -> {}
        }
    }
}

@Composable
private fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .height(92.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar + Badge
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                if (!conversation.other_user.profile_picture.isNullOrBlank()) {
                    AsyncImage(
                        model = UrlUtils.resolveMediaUrl(conversation.other_user.profile_picture),
                        contentDescription = conversation.other_user.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        color = Color(0xFFE9F0FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = conversation.other_user.name.first().toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                // Unread Badge
                if (conversation.unread_count > 0) {
                    Surface(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                        color = BadgeRed
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = conversation.unread_count.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Message Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.other_user.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        text = formatMessageTime(conversation.last_message_time),
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }

                Text(
                    text = "Re: ${conversation.case_title}",
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = conversation.last_message.message_text,
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatMessageTime(isoDateTime: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()
        val diff = java.time.temporal.ChronoUnit.HOURS.between(dateTime, now)

        when {
            diff < 1 -> {
                val minutes = java.time.temporal.ChronoUnit.MINUTES.between(dateTime, now)
                if (minutes < 1) "now" else "${minutes}m ago"
            }
            diff < 24 -> {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                dateTime.format(formatter)
            }
            diff < 168 -> "Yesterday"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MM/dd")
                dateTime.format(formatter)
            }
        }
    } catch (e: Exception) {
        "recently"
    }
}
