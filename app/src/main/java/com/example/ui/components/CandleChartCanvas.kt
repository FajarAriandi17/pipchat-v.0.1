package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.model.SignalCard
import com.example.ui.theme.LocalPipCustomColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CandleChartCanvas(
    candles: List<Candle>,
    signal: SignalCard?,
    modifier: Modifier = Modifier
) {
    val customColors = LocalPipCustomColors.current
    val textMeasurer = rememberTextMeasurer()

    var touchOffset by remember { mutableStateOf<Offset?>(null) }
    var hoveredCandle by remember { mutableStateOf<Candle?>(null) }

    val buyColor = customColors.buy
    val sellColor = customColors.sell
    val goldColor = customColors.gold
    val lineColor = customColors.line
    val textMuted = customColors.textMuted
    val surface2 = customColors.surface2

    val dateFormat = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxSize()) {
        // Crosshair HUD Information when touched
        if (hoveredCandle != null) {
            val c = hoveredCandle!!
            val isBullish = c.close >= c.open
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surface2)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(c.time)),
                    style = TextStyle(fontSize = 11.sp, color = textMuted)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HudPriceItem("O:", c.open, textMuted)
                    HudPriceItem("H:", c.high, buyColor)
                    HudPriceItem("L:", c.low, sellColor)
                    HudPriceItem("C:", c.close, if (isBullish) buyColor else sellColor)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .pointerInput(candles) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchOffset = offset
                            val candleIndex = calculateCandleIndex(offset.x, size.width.toFloat(), candles.size)
                            hoveredCandle = candles.getOrNull(candleIndex)
                        }
                    )
                }
                .pointerInput(candles) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchOffset = offset
                            val candleIndex = calculateCandleIndex(offset.x, size.width.toFloat(), candles.size)
                            hoveredCandle = candles.getOrNull(candleIndex)
                        },
                        onDragEnd = {
                            touchOffset = null
                            hoveredCandle = null
                        },
                        onDragCancel = {
                            touchOffset = null
                            hoveredCandle = null
                        },
                        onDrag = { change, _ ->
                            touchOffset = change.position
                            val candleIndex = calculateCandleIndex(change.position.x, size.width.toFloat(), candles.size)
                            hoveredCandle = candles.getOrNull(candleIndex)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (candles.isEmpty()) return@Canvas

                val width = size.width
                val height = size.height
                val rightScaleWidth = 64.dp.toPx()
                val bottomScaleHeight = 24.dp.toPx()
                val chartWidth = width - rightScaleWidth
                val chartHeight = height - bottomScaleHeight

                // Find global min and max with buffer
                var minPrice = candles.minOf { it.low }
                var maxPrice = candles.maxOf { it.high }

                // Include signal levels in scale bounds
                signal?.let { s ->
                    s.entry?.let { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) }
                    s.stopLoss?.let { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) }
                    s.tp1?.let { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) }
                    s.tp2?.let { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) }
                    s.support.forEach { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) }
                    s.resistance.forEach { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) }
                }

                val pricePadding = (maxPrice - minPrice) * 0.08
                minPrice -= pricePadding
                maxPrice += pricePadding
                val priceRange = max(maxPrice - minPrice, 0.0001)

                fun priceToY(price: Double): Float {
                    val normalized = (maxPrice - price) / priceRange
                    return (normalized * chartHeight).toFloat()
                }

                // Draw background grid lines (horizontal)
                val gridSteps = 5
                val stepPrice = priceRange / gridSteps
                for (i in 0..gridSteps) {
                    val p = minPrice + (i * stepPrice)
                    val y = priceToY(p)
                    drawLine(
                        color = lineColor.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )

                    // Draw right-axis price text
                    val formatted = String.format(Locale.US, if (maxPrice > 100) "%.2f" else "%.4f", p)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatted,
                        topLeft = Offset(chartWidth + 6.dp.toPx(), y - 7.dp.toPx()),
                        style = TextStyle(
                            color = textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Draw S&R dashed lines
                signal?.let { s ->
                    // Support lines (gray dashed)
                    s.support.forEach { sup ->
                        val y = priceToY(sup)
                        drawLevelLine(
                            y = y,
                            chartWidth = chartWidth,
                            color = Color(0xFF9E9E9E),
                            label = "S: $sup",
                            isDashed = true,
                            textMeasurer = textMeasurer
                        )
                    }

                    // Resistance lines (faint red dashed)
                    s.resistance.forEach { res ->
                        val y = priceToY(res)
                        drawLevelLine(
                            y = y,
                            chartWidth = chartWidth,
                            color = Color(0xFFEF5350).copy(alpha = 0.8f),
                            label = "R: $res",
                            isDashed = true,
                            textMeasurer = textMeasurer
                        )
                    }

                    // Entry line (Gold solid)
                    s.entry?.let { entry ->
                        val y = priceToY(entry)
                        drawLevelLine(
                            y = y,
                            chartWidth = chartWidth,
                            color = goldColor,
                            label = "Entry: $entry",
                            isDashed = false,
                            textMeasurer = textMeasurer
                        )
                    }

                    // Stop Loss line (Red solid)
                    s.stopLoss?.let { sl ->
                        val y = priceToY(sl)
                        drawLevelLine(
                            y = y,
                            chartWidth = chartWidth,
                            color = sellColor,
                            label = "SL: $sl",
                            isDashed = false,
                            textMeasurer = textMeasurer
                        )
                    }

                    // TP1 & TP2 (Green solid)
                    s.tp1?.let { tp1 ->
                        val y = priceToY(tp1)
                        drawLevelLine(
                            y = y,
                            chartWidth = chartWidth,
                            color = buyColor,
                            label = "TP1: $tp1",
                            isDashed = false,
                            textMeasurer = textMeasurer
                        )
                    }

                    s.tp2?.let { tp2 ->
                        val y = priceToY(tp2)
                        drawLevelLine(
                            y = y,
                            chartWidth = chartWidth,
                            color = buyColor,
                            label = "TP2: $tp2",
                            isDashed = false,
                            textMeasurer = textMeasurer
                        )
                    }
                }

                // Draw Candlesticks
                val candleCount = candles.size
                val candleSlot = chartWidth / candleCount
                val candleWidth = max(candleSlot * 0.7f, 2f)

                candles.forEachIndexed { index, candle ->
                    val centerX = (index * candleSlot) + (candleSlot / 2f)
                    val isBullish = candle.close >= candle.open
                    val candleColor = if (isBullish) buyColor else sellColor

                    val openY = priceToY(candle.open)
                    val closeY = priceToY(candle.close)
                    val highY = priceToY(candle.high)
                    val lowY = priceToY(candle.low)

                    // Draw Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 1.5f
                    )

                    // Draw Body
                    val bodyTop = min(openY, closeY)
                    val bodyHeight = max(kotlin.math.abs(openY - closeY), 2f)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(centerX - (candleWidth / 2f), bodyTop),
                        size = Size(candleWidth, bodyHeight)
                    )
                }

                // Draw Crosshair if active
                touchOffset?.let { touch ->
                    val touchX = touch.x.coerceIn(0f, chartWidth)
                    val touchY = touch.y.coerceIn(0f, chartHeight)

                    // Vertical line
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(touchX, 0f),
                        end = Offset(touchX, chartHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    // Horizontal line
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(0f, touchY),
                        end = Offset(chartWidth, touchY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    // Right price badge on crosshair
                    val currentPriceAtY = maxPrice - ((touchY / chartHeight) * priceRange)
                    val priceStr = String.format(Locale.US, if (maxPrice > 100) "%.2f" else "%.4f", currentPriceAtY)
                    drawRect(
                        color = surface2,
                        topLeft = Offset(chartWidth + 2.dp.toPx(), touchY - 9.dp.toPx()),
                        size = Size(rightScaleWidth - 4.dp.toPx(), 18.dp.toPx())
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = priceStr,
                        topLeft = Offset(chartWidth + 6.dp.toPx(), touchY - 7.dp.toPx()),
                        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawLevelLine(
    y: Float,
    chartWidth: Float,
    color: Color,
    label: String,
    isDashed: Boolean,
    textMeasurer: TextMeasurer
) {
    if (y < 0 || y > size.height) return

    val pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 8f)) else null

    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(chartWidth, y),
        strokeWidth = if (isDashed) 1.2f else 2f,
        pathEffect = pathEffect
    )

    // Pill badge for the label on the right
    val labelMeasure = textMeasurer.measure(
        text = label,
        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold)
    )
    val badgeWidth = labelMeasure.size.width + 12f
    val badgeHeight = 16.dp.toPx()
    val badgeX = chartWidth - badgeWidth - 4f
    val badgeY = y - (badgeHeight / 2f)

    drawRect(
        color = color.copy(alpha = 0.85f),
        topLeft = Offset(badgeX, badgeY),
        size = Size(badgeWidth, badgeHeight)
    )

    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(badgeX + 6f, badgeY + 2f),
        style = TextStyle(
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )
}

private fun calculateCandleIndex(touchX: Float, totalWidth: Float, count: Int): Int {
    if (count == 0) return 0
    val slot = totalWidth / count
    val idx = (touchX / slot).roundToInt()
    return idx.coerceIn(0, count - 1)
}

@Composable
private fun HudPriceItem(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = TextStyle(fontSize = 11.sp, color = Color.Gray))
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = String.format(Locale.US, if (value > 100) "%.2f" else "%.4f", value),
            style = TextStyle(fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        )
    }
}
