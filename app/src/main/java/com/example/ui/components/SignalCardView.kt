package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalCard
import com.example.ui.theme.LocalPipCustomColors
import com.example.ui.theme.PipNeutral

@Composable
fun SignalCardView(
    signal: SignalCard,
    isIndonesian: Boolean,
    onViewChart: (SignalCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customColors = LocalPipCustomColors.current
    val isBuy = signal.bias.equals("BUY", ignoreCase = true)
    val isSell = signal.bias.equals("SELL", ignoreCase = true)
    val isNeutral = !isBuy && !isSell

    val badgeColor = when {
        isBuy -> customColors.buy
        isSell -> customColors.sell
        else -> PipNeutral
    }

    val confidenceText = when (signal.confidence.lowercase()) {
        "high", "tinggi" -> if (isIndonesian) "Keyakinan: tinggi" else "Confidence: high"
        "low", "rendah" -> if (isIndonesian) "Keyakinan: rendah" else "Confidence: low"
        else -> if (isIndonesian) "Keyakinan: sedang" else "Confidence: medium"
    }

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun copyToClipboard(text: String, label: String) {
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(
            context,
            if (isIndonesian) "Disalin: $text" else "Copied: $text",
            Toast.LENGTH_SHORT
        ).show()
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.98f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, customColors.line, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("signal_card")
        ) {
            // Header Row: Pair · Timeframe and Bias Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${signal.pair} · ${signal.timeframe}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.18f))
                        .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = signal.bias.uppercase(),
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Text(
                text = confidenceText,
                style = MaterialTheme.typography.labelSmall,
                color = customColors.textMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Setup Details Grid
            if (isNeutral || signal.entry == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(customColors.surface2)
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isIndonesian) "Belum ada setup yang jelas (Pasar Sideways)" else "No clear setup yet (Consolidation)",
                        color = customColors.textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // 2x2 Grid: Entry, Stop Loss, TP1, TP2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SignalCell(
                        label = if (isIndonesian) "Entry" else "Entry",
                        value = "${signal.entry}",
                        valueColor = customColors.gold,
                        modifier = Modifier.weight(1f)
                    ) {
                        copyToClipboard("${signal.entry}", "Entry")
                    }

                    SignalCell(
                        label = if (isIndonesian) "Stop Loss" else "Stop Loss",
                        value = "${signal.stopLoss}",
                        valueColor = customColors.sell,
                        modifier = Modifier.weight(1f)
                    ) {
                        copyToClipboard("${signal.stopLoss}", "Stop Loss")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SignalCell(
                        label = if (isIndonesian) "TP1 · 1:1,5" else "TP1 · 1:1.5",
                        value = "${signal.tp1}",
                        valueColor = customColors.buy,
                        modifier = Modifier.weight(1f)
                    ) {
                        copyToClipboard("${signal.tp1}", "TP1")
                    }

                    SignalCell(
                        label = if (isIndonesian) "TP2 · 1:2,0" else "TP2 · 1:2.0",
                        value = "${signal.tp2}",
                        valueColor = customColors.buy,
                        modifier = Modifier.weight(1f)
                    ) {
                        copyToClipboard("${signal.tp2}", "TP2")
                    }
                }
            }

            // S&R Levels
            if (signal.support.isNotEmpty() || signal.resistance.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(customColors.surface2.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (signal.support.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isIndonesian) "Support" else "Support",
                                style = MaterialTheme.typography.labelSmall,
                                color = customColors.textMuted
                            )
                            Text(
                                text = signal.support.joinToString("  ·  "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (signal.resistance.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isIndonesian) "Resistance" else "Resistance",
                                style = MaterialTheme.typography.labelSmall,
                                color = customColors.textMuted
                            )
                            Text(
                                text = signal.resistance.joinToString("  ·  "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Action Buttons Row: View Chart & Copy Full Plan
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onViewChart(signal) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("view_chart_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = customColors.gold
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(customColors.gold.copy(alpha = 0.6f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = "View Chart",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isIndonesian) "Lihat Chart" else "View Chart",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedButton(
                    onClick = {
                        val fullPlan = """
${signal.pair} · ${signal.timeframe} [${signal.bias}]
Entry: ${signal.entry ?: "N/A"}
Stop Loss: ${signal.stopLoss ?: "N/A"}
TP1 (1:1.5): ${signal.tp1 ?: "N/A"}
TP2 (1:2.0): ${signal.tp2 ?: "N/A"}
Support: ${signal.support.joinToString(", ")}
Resistance: ${signal.resistance.joinToString(", ")}
                        """.trimIndent()
                        copyToClipboard(fullPlan, "Signal Plan")
                    },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("copy_signal_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(customColors.line)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Signal",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isIndonesian) "Salin" else "Copy",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Disclaimer Caption
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = customColors.textMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isIndonesian) "Bukan saran keuangan. Trading berisiko kehilangan modal." else "Not financial advice. Trading involves risk of capital loss.",
                    style = MaterialTheme.typography.labelSmall,
                    color = customColors.textMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SignalCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val customColors = LocalPipCustomColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(customColors.surface2)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = customColors.textMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = valueColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
