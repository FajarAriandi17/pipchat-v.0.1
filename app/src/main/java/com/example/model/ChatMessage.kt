package com.example.model

enum class MessageRole {
    USER, ASSISTANT
}

enum class MessageStatus {
    COMPLETE, ERROR, STOPPED, STREAMING
}

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val signal: SignalCard? = null,
    val toolStatus: String? = null,
    val status: MessageStatus = MessageStatus.COMPLETE
)
