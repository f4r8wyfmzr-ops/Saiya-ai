package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isFromUser: Boolean,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isSampleAiResponse: Boolean = false,
    val actionLabel: String? = null
)
