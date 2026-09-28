package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.model.SignalCard
import com.example.ui.components.CandleChartCanvas
import com.example.ui.theme.LocalPipCustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    signal: SignalCard,
    candles: List<Candle>,
    isLoading: Boolean,
    currentTimeframe: String,
    isIndonesian: Boolean,
    onTimeframeChanged: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalPipCustomColors.current
    val timeframes = listOf("M15", "H1", "H4", "D1")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${signal.pair} · $currentTimeframe",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (signal.bias.uppercase()) {
                                        "BUY" -> customColors.buy.copy(alpha = 0.2f)
                                        "SELL" -> customColors.sell.copy(alpha = 0.2f)
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = signal.bias.uppercase(),
                                color = when (signal.bias.uppercase()) {
                                    "BUY" -> customColors.buy
                                    "SELL" -> customColors.sell
                                    else -> Color.Gray
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chart_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Timeframe Selector Chips (PRD Section 6.6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeframes.forEach { tf ->
                    val isSelected = tf.equals(currentTimeframe, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) customColors.gold else customColors.surface2)
                            .clickable { onTimeframeChanged(tf) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("timeframe_chip_$tf"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf,
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Legend Row for Level Lines
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(customColors.surface2.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                signal.entry?.let { LegendItem("Entry", it, customColors.gold) }
                signal.stopLoss?.let { LegendItem("SL", it, customColors.sell) }
                signal.tp1?.let { LegendItem("TP1", it, customColors.buy) }
                signal.tp2?.let { LegendItem("TP2", it, customColors.buy) }
            }

            // Interactive Candlestick Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = customColors.gold,
                        modifier = Modifier.size(36.dp)
                    )
                } else if (candles.isEmpty()) {
                    Text(
                        text = if (isIndonesian) "Data chart tidak tersedia" else "Chart data unavailable",
                        color = customColors.textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    CandleChartCanvas(
                        candles = candles,
                        signal = signal,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Bottom disclaimer banner
            Text(
                text = if (isIndonesian) "Bukan saran keuangan. Trading berisiko kehilangan modal." else "Not financial advice. Trading involves risk of capital loss.",
                style = MaterialTheme.typography.labelSmall,
                color = customColors.textMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: $value",
            style = TextStyle(
                color = color,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
