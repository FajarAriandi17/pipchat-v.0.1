package com.example.model

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val preview: String = ""
)

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long = 0L
)

data class UserSettings(
    val language: String = "id", // "id" or "en"
    val themeMode: String = "dark", // "dark", "light", "system"
    val tradingStyle: String = "day", // "scalping", "day", "swing"
    val riskPercent: Double = 1.5 // 1.0, 1.5, 2.0, etc.
)
