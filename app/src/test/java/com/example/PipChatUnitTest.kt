package com.example

import com.example.data.engine.ForexTechnicalEngine
import com.example.data.repository.MarketDataRepository
import com.example.model.Candle
import com.example.model.SignalCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PipChatUnitTest {

    @Test
    fun testSignalCardValidationBuy() {
        val validBuy = SignalCard(
            pair = "EURUSD",
            timeframe = "H1",
            bias = "BUY",
            entry = 1.0842,
            stopLoss = 1.0815,
            tp1 = 1.0882,
            tp2 = 1.0896
        )
        assertTrue(validBuy.isValid())

        val invalidBuy = SignalCard(
            pair = "EURUSD",
            timeframe = "H1",
            bias = "BUY",
            entry = 1.0842,
            stopLoss = 1.0850, // SL above entry is invalid for BUY
            tp1 = 1.0882,
            tp2 = 1.0896
        )
        assertFalse(invalidBuy.isValid())
    }

    @Test
    fun testSignalCardValidationSell() {
        val validSell = SignalCard(
            pair = "GBPUSD",
            timeframe = "H1",
            bias = "SELL",
            entry = 1.2980,
            stopLoss = 1.3010,
            tp1 = 1.2935,
            tp2 = 1.2920
        )
        assertTrue(validSell.isValid())

        val invalidSell = SignalCard(
            pair = "GBPUSD",
            timeframe = "H1",
            bias = "SELL",
            entry = 1.2980,
            stopLoss = 1.2950, // SL below entry is invalid for SELL
            tp1 = 1.2935,
            tp2 = 1.2920
        )
        assertFalse(invalidSell.isValid())
    }

    @Test
    fun testPairAndTimeframeExtraction() {
        val repo = MarketDataRepository()
        val (pair1, tf1) = repo.extractPairAndTimeframe("Analisa EURUSD H1 dong min")
        assertEquals("EURUSD", pair1)
        assertEquals("H1", tf1)

        val (pair2, tf2) = repo.extractPairAndTimeframe("Cek chart gold M15")
        assertEquals("XAUUSD", pair2)
        assertEquals("M15", tf2)

        val (pair3, tf3) = repo.extractPairAndTimeframe("USD/JPY")
        assertEquals("USDJPY", pair3)
        assertEquals("H1", tf3) // Default H1
    }

    @Test
    fun testTradePlanCalculation() {
        val candles = mutableListOf<Candle>()
        var price = 1.0800
        for (i in 0 until 50) {
            price += 0.0002
            candles.add(
                Candle(
                    time = 1000L + (i * 3600),
                    open = price - 0.0001,
                    high = price + 0.0005,
                    low = price - 0.0005,
                    close = price
                )
            )
        }

        val planBuy = ForexTechnicalEngine.buildTradePlan("EURUSD", "H1", candles, forcedBias = "BUY")
        assertTrue(planBuy.isValid())
        assertTrue(planBuy.stopLoss!! < planBuy.entry!!)
        assertTrue(planBuy.entry!! < planBuy.tp1!!)
        assertTrue(planBuy.tp1!! < planBuy.tp2!!)

        val planSell = ForexTechnicalEngine.buildTradePlan("EURUSD", "H1", candles, forcedBias = "SELL")
        assertTrue(planSell.isValid())
        assertTrue(planSell.stopLoss!! > planSell.entry!!)
        assertTrue(planSell.entry!! > planSell.tp1!!)
        assertTrue(planSell.tp1!! > planSell.tp2!!)
    }

    @Test
    fun testAppleUserFormatAndEmailMask() {
        val appleUser = com.example.model.UserProfile(
            uid = "apple_user_12345",
            displayName = "Apple Trader",
            email = "trader.apple@privaterelay.appleid.com",
            provider = com.example.model.AuthProvider.APPLE
        )
        assertEquals(com.example.model.AuthProvider.APPLE, appleUser.provider)
        assertTrue(appleUser.email.contains("appleid.com"))
        assertEquals("Apple Trader", appleUser.displayName)
    }

    @Test
    fun testGoogleUserProfile() {
        val googleUser = com.example.model.UserProfile(
            uid = "google_user_9988",
            displayName = "Trader Google",
            email = "trader@gmail.com",
            provider = com.example.model.AuthProvider.GOOGLE
        )
        assertEquals(com.example.model.AuthProvider.GOOGLE, googleUser.provider)
        assertEquals("trader@gmail.com", googleUser.email)
    }
}
