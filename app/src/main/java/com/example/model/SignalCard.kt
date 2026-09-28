package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignalCard(
    @Json(name = "pair") val pair: String,
    @Json(name = "timeframe") val timeframe: String = "H1",
    @Json(name = "bias") val bias: String, // BUY, SELL, NEUTRAL
    @Json(name = "entry") val entry: Double? = null,
    @Json(name = "stop_loss") val stopLoss: Double? = null,
    @Json(name = "tp1") val tp1: Double? = null,
    @Json(name = "tp2") val tp2: Double? = null,
    @Json(name = "rr") val rr: List<Double> = listOf(1.5, 2.0),
    @Json(name = "support") val support: List<Double> = emptyList(),
    @Json(name = "resistance") val resistance: List<Double> = emptyList(),
    @Json(name = "indicators") val indicators: Map<String, String> = emptyMap(),
    @Json(name = "confidence") val confidence: String = "medium", // low, medium, high
    @Json(name = "generated_at") val generatedAt: String = ""
) {
    /**
     * Strict validation per PRD Section 9.2:
     * For BUY: stop_loss < entry < tp1 < tp2
     * For SELL: stop_loss > entry > tp1 > tp2
     */
    fun isValid(): Boolean {
        val b = bias.uppercase()
        if (b == "NEUTRAL") return true
        val e = entry ?: return false
        val sl = stopLoss ?: return false
        val t1 = tp1 ?: return false
        val t2 = tp2 ?: return false

        return when (b) {
            "BUY" -> sl < e && e < t1 && t1 < t2
            "SELL" -> sl > e && e > t1 && t1 > t2
            else -> false
        }
    }
}
