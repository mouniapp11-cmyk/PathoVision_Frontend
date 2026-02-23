package com.simats.pathovision.models

import com.google.gson.annotations.SerializedName

data class Message(
    val id: String,
    val message_text: String,
    val is_read: Boolean,
    val created_at: String,
    val sender_id: String,
    val receiver_id: String,
    val case_id: String,
    val Sender: UserData? = null,
    val Receiver: UserData? = null
)

data class MessageRequest(
    val case_id: String,
    val receiver_id: String,
    val message_text: String
)

data class Conversation(
    val case_id: String,
    val case_title: String,
    val case_image: String,
    val case_prediction: String? = null,
    val other_user: UserData,
    val last_message: Message,
    val last_message_time: String,
    val unread_count: Int
)

data class ConversationGroup(
    val case_id: String,
    val case_title: String,
    val case_image: String,
    val other_user: UserData,
    val last_message_preview: String,
    val last_message_time: String,
    val unread_count: Int,
    val sender_name: String
)
