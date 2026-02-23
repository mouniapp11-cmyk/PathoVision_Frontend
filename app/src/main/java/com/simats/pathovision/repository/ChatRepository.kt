package com.simats.pathovision.repository

import com.simats.pathovision.models.Conversation
import com.simats.pathovision.models.Message
import com.simats.pathovision.models.MessageRequest
import com.simats.pathovision.network.ApiService
import com.simats.pathovision.utils.Resource
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getInbox(): Resource<List<Conversation>> {
        return try {
            val response = apiService.getInbox()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to load inbox")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMessages(caseId: String): Resource<List<Message>> {
        return try {
            val response = apiService.getMessages(caseId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to load messages")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun sendMessage(
        caseId: String,
        receiverId: String,
        messageText: String
    ): Resource<Message> {
        return try {
            val request = MessageRequest(
                case_id = caseId,
                receiver_id = receiverId,
                message_text = messageText
            )
            val response = apiService.sendMessage(request)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: error("No response body"))
            } else {
                Resource.Error(response.message() ?: "Failed to send message")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
