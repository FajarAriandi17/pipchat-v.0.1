package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val preview: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String, // "USER", "ASSISTANT"
    val text: String,
    val timestamp: Long,
    val signalJson: String?, // serialized SignalCard JSON
    val status: String // "COMPLETE", "ERROR", "STOPPED"
)
