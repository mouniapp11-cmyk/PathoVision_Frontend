package com.simats.pathovision.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.models.Conversation
import com.simats.pathovision.models.Message
import com.simats.pathovision.repository.ChatRepository
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _inboxState = MutableStateFlow<Resource<List<Conversation>>?>(null)
    val inboxState = _inboxState.asStateFlow()

    private val _messagesState = MutableStateFlow<Resource<List<Message>>?>(null)
    val messagesState = _messagesState.asStateFlow()

    private val _sendMessageState = MutableStateFlow<Resource<Message>?>(null)
    val sendMessageState = _sendMessageState.asStateFlow()

    init {
        loadInbox()
    }

    fun loadInbox() {
        viewModelScope.launch {
            _inboxState.value = Resource.Loading()
            _inboxState.value = chatRepository.getInbox()
        }
    }

    fun loadMessages(caseId: String) {
        viewModelScope.launch {
            _messagesState.value = Resource.Loading()
            _messagesState.value = chatRepository.getMessages(caseId)
        }
    }

    fun sendMessage(caseId: String, receiverId: String, messageText: String) {
        viewModelScope.launch {
            _sendMessageState.value = Resource.Loading()
            _sendMessageState.value = chatRepository.sendMessage(caseId, receiverId, messageText)
            
            // Reload messages after sending
            if (_sendMessageState.value is Resource.Success) {
                loadMessages(caseId)
            }
        }
    }

    fun clearSendState() {
        _sendMessageState.value = null
    }
}
