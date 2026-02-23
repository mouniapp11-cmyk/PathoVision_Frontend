package com.simats.pathovision.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simats.pathovision.models.Message
import com.simats.pathovision.utils.Resource
import com.simats.pathovision.utils.UrlUtils
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val PrimaryBlue = Color(0xFF4A6FE3)
private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val CardWhite = Color.White
private val SentBubble = Color(0xFFE8F0FF)
private val ReceivedBubble = Color(0xFFF0F0F0)

@Composable
fun ChatDetailScreen(
    caseId: String,
    receiverId: String,
    receiverName: String,
    onNavigateBack: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messagesState by viewModel.messagesState.collectAsState()
    val sendMessageState by viewModel.sendMessageState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(caseId) {
        viewModel.loadMessages(caseId)
    }

    LaunchedEffect(messagesState) {
        if (messagesState is Resource.Success) {
            scope.launch {
                val list = (messagesState as Resource.Success).data
                if (list.isNotEmpty()) {
                    listState.animateScrollToItem(list.size - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextDark)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receiverName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Case #$caseId",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        }

        // Messages List
        when (val state = messagesState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message ?: "Failed to load messages", color = TextGray)
                }
            }

            is Resource.Success -> {
                val messages = state.data ?: emptyList()
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message = message, receiverId = receiverId)
                    }
                }
            }

            null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No messages yet. Start the conversation!", color = TextGray)
                }
            }
        }

        // Input Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...", color = TextGray, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(caseId, receiverId, messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    receiverId: String
) {
    val isSent = message.receiver_id == receiverId
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        if (!isSent) {
            if (!message.Sender?.profile_picture.isNullOrBlank()) {
                AsyncImage(
                    model = UrlUtils.resolveMediaUrl(message.Sender?.profile_picture ?: ""),
                    contentDescription = message.Sender?.name,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape),
                    color = Color(0xFFE9F0FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (message.Sender?.name?.firstOrNull() ?: "?").toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(
                    if (isSent) SentBubble else ReceivedBubble,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isSent) 16.dp else 0.dp,
                        bottomEnd = if (isSent) 0.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.message_text,
                fontSize = 14.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatChatTime(message.created_at),
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}

private fun formatChatTime(isoDateTime: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("HH:mm a")
        dateTime.format(formatter)
    } catch (e: Exception) {
        "now"
    }
}
