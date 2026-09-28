package com.example.data.engine

import com.example.model.Candle
import com.example.model.SignalCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

data class TechnicalIndicatorResult(
    val rsi: Double,
    val macdStatus: String, // "bullish", "bearish", "neutral"
    val emaStatus: String, // "above_ema50", "below_ema50", "testing_ema50"
    val atr: Double,
    val currentPrice: Double,
    val bias: String, // "BUY", "SELL", "NEUTRAL"
    val confidence: String // "low", "medium", "high"
)

data class PriceLevels(
    val pivot: Double,
    val support: List<Double>,
    val resistance: List<Double>
)

object ForexTechnicalEngine {

    /**
     * Compute RSI (Relative Strength Index) for period N (default 14)
     */
    fun calculateRSI(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size <= period) return 50.0
        val closes = candles.map { it.close }
        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = closes[i] - closes[i - 1]
            if (change >= 0) gains += change else losses += abs(change)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in (period + 1) until closes.size) {
            val change = closes[i] - closes[i - 1]
            if (change >= 0) {
                avgGain = (avgGain * (period - 1) + change) / period
                avgLoss = (avgLoss * (period - 1)) / period
            } else {
                avgGain = (avgGain * (period - 1)) / period
                avgLoss = (avgLoss * (period - 1) + abs(change)) / period
            }
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return (100.0 - (100.0 / (1.0 + rs))).coerceIn(0.0, 100.0)
    }

    /**
     * Compute Exponential Moving Average (EMA)
     */
    fun calculateEMA(candles: List<Candle>, period: Int): Double {
        if (candles.isEmpty()) return 0.0
        if (candles.size < period) return candles.last().close
        val multiplier = 2.0 / (period + 1.0)
        var ema = candles.take(period).map { it.close }.average()
        for (i in period until candles.size) {
            ema = (candles[i].close - ema) * multiplier + ema
        }
        return ema
    }

    /**
     * Compute Average True Range (ATR)
     */
    fun calculateATR(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < 2) return 0.0015
        val trs = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val current = candles[i]
            val prevClose = candles[i - 1].close
            val tr = max(
                current.high - current.low,
                max(abs(current.high - prevClose), abs(current.low - prevClose))
            )
            trs.add(tr)
        }
        if (trs.isEmpty()) return 0.0015
        val recentTrs = trs.takeLast(period)
        return recentTrs.average()
    }

    /**
     * Compute Support and Resistance Levels (Pivot Points + Fibonacci + Swings)
     */
    fun calculateLevels(pair: String, candles: List<Candle>): PriceLevels {
        if (candles.isEmpty()) {
            val base = if (pair.contains("JPY")) 150.00 else if (pair.contains("XAU")) 2650.00 else 1.0850
            return PriceLevels(
                pivot = base,
                support = listOf(base * 0.995, base * 0.990),
                resistance = listOf(base * 1.005, base * 1.010)
            )
        }

        val lastCandles = candles.takeLast(30)
        val high = lastCandles.maxOf { it.high }
        val low = lastCandles.minOf { it.low }
        val close = lastCandles.last().close

        val pivot = (high + low + close) / 3.0
        val r1 = 2 * pivot - low
        val s1 = 2 * pivot - high
        val r2 = pivot + (high - low)
        val s2 = pivot - (high - low)

        // Fibonacci levels
        val diff = high - low
        val fib382 = high - diff * 0.382
        val fib618 = high - diff * 0.618

        val decimals = if (pair.contains("JPY")) 3 else if (pair.contains("XAU")) 2 else 5
        val supports = listOf(roundPrice(s1, decimals), roundPrice(s2, decimals), roundPrice(fib618, decimals))
            .distinct()
            .filter { it < close }
            .sortedDescending()
            .take(2)
            .ifEmpty { listOf(roundPrice(close * 0.997, decimals), roundPrice(close * 0.993, decimals)) }

        val resistances = listOf(roundPrice(r1, decimals), roundPrice(r2, decimals), roundPrice(fib382, decimals))
            .distinct()
            .filter { it > close }
            .sorted()
            .take(2)
            .ifEmpty { listOf(roundPrice(close * 1.003, decimals), roundPrice(close * 1.007, decimals)) }

        return PriceLevels(
            pivot = roundPrice(pivot, decimals),
            support = supports,
            resistance = resistances
        )
    }

    /**
     * Synthesize full technical analysis
     */
    fun analyze(pair: String, candles: List<Candle>): TechnicalIndicatorResult {
        if (candles.isEmpty()) {
            return TechnicalIndicatorResult(
                rsi = 52.0,
                macdStatus = "neutral",
                emaStatus = "above_ema50",
                atr = 0.0015,
                currentPrice = 1.0850,
                bias = "NEUTRAL",
                confidence = "medium"
            )
        }

        val currentPrice = candles.last().close
        val rsi = calculateRSI(candles)
        val ema20 = calculateEMA(candles, 20)
        val ema50 = calculateEMA(candles, 50)
        val ema200 = calculateEMA(candles, min(200, candles.size))
        val atr = calculateATR(candles)

        val emaStatus = when {
            currentPrice > ema50 && ema20 > ema50 -> "above_ema50"
            currentPrice < ema50 && ema20 < ema50 -> "below_ema50"
            else -> "testing_ema50"
        }

        val macdDiff = ema20 - ema50
        val macdStatus = when {
            macdDiff > (atr * 0.1) -> "bullish"
            macdDiff < -(atr * 0.1) -> "bearish"
            else -> "neutral"
        }

        // Bias determination
        var bullPoints = 0
        var bearPoints = 0

        if (rsi in 45.0..68.0 && emaStatus == "above_ema50") bullPoints += 2
        if (rsi > 55.0) bullPoints += 1
        if (macdStatus == "bullish") bullPoints += 2
        if (currentPrice > ema200) bullPoints += 1

        if (rsi in 32.0..55.0 && emaStatus == "below_ema50") bearPoints += 2
        if (rsi < 45.0) bearPoints += 1
        if (macdStatus == "bearish") bearPoints += 2
        if (currentPrice < ema200) bearPoints += 1

        val bias = when {
            bullPoints >= 4 && bullPoints > bearPoints + 1 -> "BUY"
            bearPoints >= 4 && bearPoints > bullPoints + 1 -> "SELL"
            else -> "NEUTRAL"
        }

        val confidence = when {
            max(bullPoints, bearPoints) >= 5 -> "high"
            max(bullPoints, bearPoints) >= 3 -> "medium"
            else -> "low"
        }

        return TechnicalIndicatorResult(
            rsi = round(rsi * 10) / 10.0,
            macdStatus = macdStatus,
            emaStatus = emaStatus,
            atr = atr,
            currentPrice = currentPrice,
            bias = bias,
            confidence = confidence
        )
    }

    /**
     * Build Trade Plan with strict R:R and Stop Loss enforcement
     * Validates BUY: stop_loss < entry < tp1 < tp2
     * Validates SELL: stop_loss > entry > tp1 > tp2
     */
    fun buildTradePlan(
        pair: String,
        timeframe: String,
        candles: List<Candle>,
        forcedBias: String? = null
    ): SignalCard {
        val analysis = analyze(pair, candles)
        val levels = calculateLevels(pair, candles)
        val bias = (forcedBias ?: analysis.bias).uppercase()
        val decimals = if (pair.contains("JPY")) 3 else if (pair.contains("XAU")) 2 else 5
        val atr = max(analysis.atr, if (pair.contains("XAU")) 8.5 else if (pair.contains("JPY")) 0.35 else 0.0018)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val generatedAt = dateFormat.format(Date())

        if (bias == "NEUTRAL") {
            return SignalCard(
                pair = pair.uppercase(),
                timeframe = timeframe.uppercase(),
                bias = "NEUTRAL",
                entry = null,
                stopLoss = null,
                tp1 = null,
                tp2 = null,
                rr = listOf(1.5, 2.0),
                support = levels.support,
                resistance = levels.resistance,
                indicators = mapOf(
                    "rsi" to "${analysis.rsi}",
                    "macd" to analysis.macdStatus,
                    "ema" to analysis.emaStatus
                ),
                confidence = "low",
                generatedAt = generatedAt
            )
        }

        val currentPrice = analysis.currentPrice
        val slDistance = atr * 1.5 // ATR(14) x 1.5 per PRD Section 11

        val (entry, stopLoss, tp1, tp2) = if (bias == "BUY") {
            val rawEntry = currentPrice
            val rawSL = rawEntry - slDistance
            val risk = rawEntry - rawSL
            val rawTP1 = rawEntry + (risk * 1.5)
            val rawTP2 = rawEntry + (risk * 2.0)
            listOf(
                roundPrice(rawEntry, decimals),
                roundPrice(rawSL, decimals),
                roundPrice(rawTP1, decimals),
                roundPrice(rawTP2, decimals)
            )
        } else {
            val rawEntry = currentPrice
            val rawSL = rawEntry + slDistance
            val risk = rawSL - rawEntry
            val rawTP1 = rawEntry - (risk * 1.5)
            val rawTP2 = rawEntry - (risk * 2.0)
            listOf(
                roundPrice(rawEntry, decimals),
                roundPrice(rawSL, decimals),
                roundPrice(rawTP1, decimals),
                roundPrice(rawTP2, decimals)
            )
        }

        val card = SignalCard(
            pair = pair.uppercase(),
            timeframe = timeframe.uppercase(),
            bias = bias,
            entry = entry,
            stopLoss = stopLoss,
            tp1 = tp1,
            tp2 = tp2,
            rr = listOf(1.5, 2.0),
            support = levels.support,
            resistance = levels.resistance,
            indicators = mapOf(
                "rsi" to "${analysis.rsi}",
                "macd" to analysis.macdStatus,
                "ema" to analysis.emaStatus
            ),
            confidence = analysis.confidence,
            generatedAt = generatedAt
        )

        return card
    }

    private fun roundPrice(value: Double, decimals: Int): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return round(value * factor) / factor
    }
}
