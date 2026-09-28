package com.example.data.ai

import com.example.BuildConfig
import com.example.data.engine.ForexTechnicalEngine
import com.example.data.repository.MarketDataRepository
import com.example.model.Candle
import com.example.model.SignalCard
import com.example.model.UserSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

sealed class AnalysisStreamEvent {
    data class Status(val message: String) : AnalysisStreamEvent()
    data class Token(val text: String) : AnalysisStreamEvent()
    data class Signal(val card: SignalCard) : AnalysisStreamEvent()
    data class Done(val fullText: String, val card: SignalCard?) : AnalysisStreamEvent()
    data class Error(val message: String) : AnalysisStreamEvent()
}

class GeminiForexAnalyst(
    private val marketRepo: MarketDataRepository = MarketDataRepository(),
    private val client: OkHttpClient = OkHttpClient()
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val signalAdapter = moshi.adapter(SignalCard::class.java)

    /**
     * Analyze user query and emit streaming response with tool status events
     */
    fun analyzeStream(
        prompt: String,
        settings: UserSettings
    ): Flow<AnalysisStreamEvent> = flow {
        val isIndonesian = settings.language == "id" || prompt.contains("analisa", ignoreCase = true) || prompt.contains("sinyal", ignoreCase = true) || prompt.contains("halo", ignoreCase = true) || prompt.contains("bisa", ignoreCase = true)
        val (pair, timeframe) = marketRepo.extractPairAndTimeframe(prompt)

        // Check if query is educational or greeting vs technical analysis
        val isEducational = isGeneralEducationalQuery(prompt)

        if (isEducational) {
            emit(AnalysisStreamEvent.Status(if (isIndonesian) "Menyiapkan jawaban edukasi…" else "Preparing educational response…"))
            delay(400)
            val answer = generateEducationalResponse(prompt, isIndonesian)
            // Stream tokens
            val words = answer.split(" ")
            var streamed = ""
            for (word in words) {
                emit(AnalysisStreamEvent.Token("$word "))
                streamed += "$word "
                delay(35)
            }
            emit(AnalysisStreamEvent.Done(streamed, null))
            return@flow
        }

        // 1. Tool Status: Fetching market data & TA
        emit(AnalysisStreamEvent.Status(
            if (isIndonesian) "⚡ Mengambil data teknikal $pair $timeframe…" else "⚡ Fetching technical analysis for $pair $timeframe…"
        ))
        val candles = marketRepo.getCandles(pair, timeframe, 120)
        delay(500)

        // 2. Tool Status: Calculating Support & Resistance
        emit(AnalysisStreamEvent.Status(
            if (isIndonesian) "📐 Menghitung Support & Resistance (Pivot & Fibonacci)…" else "📐 Calculating Support & Resistance levels…"
        ))
        val analysis = ForexTechnicalEngine.analyze(pair, candles)
        val levels = ForexTechnicalEngine.calculateLevels(pair, candles)
        delay(450)

        // 3. Tool Status: Building trade plan
        emit(AnalysisStreamEvent.Status(
            if (isIndonesian) "🎯 Menyusun Trade Plan (SL berbasis ATR & R:R 1:1,5 - 1:2)…" else "🎯 Building Trade Plan with ATR-based Stop Loss & R:R…"
        ))
        val tradePlan = ForexTechnicalEngine.buildTradePlan(pair, timeframe, candles, analysis.bias)
        delay(400)

        // Try calling Gemini API if apiKey is present and valid
        val apiKey = try {
            BuildConfig.BUILD_TYPE // trigger build config check
            val keyField = BuildConfig::class.java.getField("GEMINI_API_KEY")
            keyField.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }

        var fullResponseText = ""
        var usedGeminiApi = false

        if (apiKey.isNotBlank() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("MY_GEMINI")) {
            try {
                emit(AnalysisStreamEvent.Status(if (isIndonesian) "🤖 Menghubungi Gemini AI Analyst…" else "🤖 Querying Gemini AI Analyst…"))
                val geminiResult = callGeminiApi(prompt, pair, timeframe, candles, analysis, tradePlan, apiKey, isIndonesian, settings)
                if (geminiResult.isNotBlank()) {
                    usedGeminiApi = true
                    fullResponseText = geminiResult
                }
            } catch (_: Exception) {
                usedGeminiApi = false
            }
        }

        if (!usedGeminiApi) {
            // Build rich structured expert response according to PRD section 11 & BUILD_GUIDE
            fullResponseText = buildStructuredAnalysisText(
                pair = pair,
                timeframe = timeframe,
                analysis = analysis,
                tradePlan = tradePlan,
                isIndonesian = isIndonesian,
                settings = settings
            )
        }

        // Stream the response smoothly with typewriter pacing
        val paragraphs = fullResponseText.split("\n")
        var accumulated = ""
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            for (word in words) {
                val token = if (word.isNotEmpty()) "$word " else " "
                emit(AnalysisStreamEvent.Token(token))
                accumulated += token
                delay(20)
            }
            emit(AnalysisStreamEvent.Token("\n"))
            accumulated += "\n"
            delay(40)
        }

        // Emit signal card if trade plan is valid
        if (tradePlan.isValid()) {
            emit(AnalysisStreamEvent.Signal(tradePlan))
        }

        emit(AnalysisStreamEvent.Done(accumulated, tradePlan))
    }

    private fun isGeneralEducationalQuery(prompt: String): Boolean {
        val lower = prompt.lowercase()
        val educationalKeywords = listOf(
            "apa itu", "what is", "jelaskan", "explain", "cara membaca",
            "how to read", "apa arti", "rumus", "belajar", "learn", "siapa kamu", "who are you"
        )
        val hasSymbol = prompt.contains("EURUSD", ignoreCase = true) ||
                prompt.contains("GBPUSD", ignoreCase = true) ||
                prompt.contains("XAUUSD", ignoreCase = true) ||
                prompt.contains("GOLD", ignoreCase = true) ||
                prompt.contains("USDJPY", ignoreCase = true)

        if (hasSymbol) return false
        return educationalKeywords.any { lower.contains(it) }
    }

    private fun generateEducationalResponse(prompt: String, isIndonesian: Boolean): String {
        return if (isIndonesian) {
            """
### 📚 Panduan Edukasi Trading PipChat

Pertanyaan Anda mengenai analisa pasar:
**RSI (Relative Strength Index):**
- Mengukur momentum harga pada skala 0–100.
- Di atas 70 mengindikasikan kondisi *Overbought* (jenuh beli).
- Di bawah 30 mengindikasikan kondisi *Oversold* (jenuh jual).
- Nilai 50 berfungsi sebagai garis netral penentu momentum tren.

**Manajemen Risiko & Stop Loss:**
Stop Loss adalah pertahanan utama setiap trader. Selalu gunakan rasio Risk-to-Reward minimal 1:1,5 atau 1:2,0 dengan alokasi risiko 1%–2% per posisi.

*Bukan saran keuangan. Trading berisiko kehilangan modal.*
            """.trimIndent()
        } else {
            """
### 📚 PipChat Educational Guide

Regarding your question about technical trading:
**RSI (Relative Strength Index):**
- Measures price momentum on a 0–100 scale.
- Above 70 suggests *Overbought* conditions.
- Below 30 suggests *Oversold* conditions.
- Level 50 acts as the centerline for trend bias.

**Risk Management & Stop Loss:**
Always calculate risk before reward. We enforce mandatory Stop Loss placement with a minimum 1:1.5 or 1:2.0 Risk-to-Reward ratio and risk per trade of 1%–2%.

*Not financial advice. Trading involves risk of capital loss.*
            """.trimIndent()
        }
    }

    private fun buildStructuredAnalysisText(
        pair: String,
        timeframe: String,
        analysis: com.example.data.engine.TechnicalIndicatorResult,
        tradePlan: SignalCard,
        isIndonesian: Boolean,
        settings: UserSettings
    ): String {
        val bias = tradePlan.bias
        val isBuy = bias == "BUY"
        val isSell = bias == "SELL"

        if (isIndonesian) {
            val biasTitle = when (bias) {
                "BUY" -> "BULLISH (BUY SETUP)"
                "SELL" -> "BEARISH (SELL SETUP)"
                else -> "NETRAL / KONSOLIDASI"
            }
            val reason = when (bias) {
                "BUY" -> "Struktur harga berada di atas EMA 50 dengan momentum RSI ${analysis.rsi} dan sinyal MACD ${analysis.macdStatus}. Tekanan beli mendominasi di sekitar level support."
                "SELL" -> "Tekanan jual dominan di bawah EMA 50 dengan momentum RSI ${analysis.rsi} dan sinyal MACD ${analysis.macdStatus}. Pola rejection terlihat di area resistance."
                else -> "Indikator belum menunjukkan arah tren yang tegas. RSI berada di area netral (${analysis.rsi}) dan harga bergerak sideways di antara level pivot."
            }

            val tradePlanJson = signalAdapter.toJson(tradePlan)

            return """
### Analisa $pair · Timeframe $timeframe (Asumsi)

**Bias Pasar:** **$biasTitle**
$reason

**1. Kondisi Tren & Indikator:**
- **Tren:** ${if (analysis.emaStatus == "above_ema50") "Uptrend sehat di atas EMA 50" else if (analysis.emaStatus == "below_ema50") "Downtrend terkonfirmasi di bawah EMA 50" else "Konsolidasi di dekat EMA 50"}
- **RSI (14):** ${analysis.rsi} (${if (analysis.rsi > 60) "Bullish Momentum" else if (analysis.rsi < 40) "Bearish Momentum" else "Zona Netral"})
- **MACD:** ${analysis.macdStatus.replaceFirstChar { it.uppercase() }}
- **Gaya Trading:** ${settings.tradingStyle.replaceFirstChar { it.uppercase() }} dengan batas risiko disarankan ${settings.riskPercent}% per trade.

**2. Level Kunci Support & Resistance:**
- **Support:** ${tradePlan.support.joinToString(" · ")}
- **Resistance:** ${tradePlan.resistance.joinToString(" · ")}

**3. Skenario Pembatalan (Invalidation):**
${if (isBuy) "Setup Buy ini batal jika candle H1 ditutup di bawah Stop Loss (${tradePlan.stopLoss}), mengindikasikan breakdown support." else if (isSell) "Setup Sell ini batal jika candle H1 ditutup menembus ke atas Stop Loss (${tradePlan.stopLoss}), mengindikasikan breakout resistance." else "Tunggu konfirmasi breakout resistance atau breakdown support sebelum membuka posisi baru."}

```signal
$tradePlanJson
```

*Bukan saran keuangan. Trading berisiko kehilangan modal.*
            """.trimIndent()
        } else {
            val biasTitle = when (bias) {
                "BUY" -> "BULLISH (BUY SETUP)"
                "SELL" -> "BEARISH (SELL SETUP)"
                else -> "NEUTRAL / CONSOLIDATION"
            }
            val reason = when (bias) {
                "BUY" -> "Price structure holds above the 50 EMA with RSI at ${analysis.rsi} and ${analysis.macdStatus} MACD. Buying pressure is confirmed near support."
                "SELL" -> "Selling momentum holds below the 50 EMA with RSI at ${analysis.rsi} and ${analysis.macdStatus} MACD. Rejection confirmed at resistance."
                else -> "Indicators show no definitive directional edge. RSI is near neutral (${analysis.rsi}) and price is oscillating between pivot levels."
            }

            val tradePlanJson = signalAdapter.toJson(tradePlan)

            return """
### Technical Analysis: $pair · Timeframe $timeframe (Assumed)

**Market Bias:** **$biasTitle**
$reason

**1. Trend & Indicators:**
- **Trend:** ${if (analysis.emaStatus == "above_ema50") "Healthy uptrend above 50 EMA" else if (analysis.emaStatus == "below_ema50") "Confirmed downtrend below 50 EMA" else "Testing 50 EMA"}
- **RSI (14):** ${analysis.rsi} (${if (analysis.rsi > 60) "Bullish Momentum" else if (analysis.rsi < 40) "Bearish Momentum" else "Neutral Zone"})
- **MACD:** ${analysis.macdStatus.replaceFirstChar { it.uppercase() }}
- **Trading Style:** ${settings.tradingStyle.replaceFirstChar { it.uppercase() }} with recommended risk ${settings.riskPercent}% per trade.

**2. Key Support & Resistance Levels:**
- **Support:** ${tradePlan.support.joinToString(" · ")}
- **Resistance:** ${tradePlan.resistance.joinToString(" · ")}

**3. Invalidation Scenario:**
${if (isBuy) "This Buy setup is invalidated if an H1 candle closes below Stop Loss (${tradePlan.stopLoss}), confirming support breakdown." else if (isSell) "This Sell setup is invalidated if an H1 candle closes above Stop Loss (${tradePlan.stopLoss}), confirming resistance breakout." else "Wait for clean breakout confirmation before entering any trade."}

```signal
$tradePlanJson
```

*Not financial advice. Trading involves risk of capital loss.*
            """.trimIndent()
        }
    }

    private suspend fun callGeminiApi(
        userPrompt: String,
        pair: String,
        timeframe: String,
        candles: List<Candle>,
        analysis: com.example.data.engine.TechnicalIndicatorResult,
        tradePlan: SignalCard,
        apiKey: String,
        isIndonesian: Boolean,
        settings: UserSettings
    ): String {
        val model = "gemini-2.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val systemPrompt = """
Kamu adalah PipChat, asisten AI untuk analisa teknikal market forex dan emas.
BAHASA: Balas dalam bahasa ${if (isIndonesian) "Indonesia" else "English"}.
Semua angka harga dan indikator harus konsisten dengan data berikut:
Pair: $pair, Timeframe: $timeframe, Current Price: ${analysis.currentPrice}, RSI: ${analysis.rsi}, MACD: ${analysis.macdStatus}, EMA: ${analysis.emaStatus}, Bias: ${tradePlan.bias}, Entry: ${tradePlan.entry}, SL: ${tradePlan.stopLoss}, TP1: ${tradePlan.tp1}, TP2: ${tradePlan.tp2}, Support: ${tradePlan.support}, Resistance: ${tradePlan.resistance}.
Format Jawaban:
1. Ringkasan bias 1-2 kalimat (Buy / Sell / Netral).
2. Tren, Indikator (RSI, MACD, EMA), Support & Resistance, Skenario pembatalan analisa.
3. Akhiri dengan blok JSON ```signal {"pair":"$pair","timeframe":"$timeframe","bias":"${tradePlan.bias}","entry":${tradePlan.entry},"stop_loss":${tradePlan.stopLoss},"tp1":${tradePlan.tp1},"tp2":${tradePlan.tp2},"rr":[1.5,2.0],"support":${tradePlan.support},"resistance":${tradePlan.resistance},"indicators":{"rsi":"${analysis.rsi}","macd":"${analysis.macdStatus}","ema":"${analysis.emaStatus}"},"confidence":"${tradePlan.confidence}","generated_at":"${tradePlan.generatedAt}"}```
4. Stop Loss wajib ada pada setiap sinyal.
5. Sisipkan: "${if (isIndonesian) "Bukan saran keuangan. Trading berisiko kehilangan modal." else "Not financial advice. Trading involves risk of capital loss."}"
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemPrompt\n\nUser request: $userPrompt"))
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return ""
        val respStr = response.body?.string() ?: return ""
        val respJson = JSONObject(respStr)
        val text = respJson.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
        return text
    }
}
