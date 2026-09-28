package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalPipCustomColors
import kotlinx.coroutines.delay

/**
 * High-definition PipChat Logo Icon matching PRD Section 6.8:
 * Golden rounded speech bubble with 3 rising candlesticks (3rd candle is Buy Green #2BB673).
 */
@Composable
fun PipChatLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    animated: Boolean = false
) {
    val customColors = LocalPipCustomColors.current
    val goldColor = customColors.gold
    val navyColor = Color(0xFF182233)
    val greenColor = customColors.buy

    // Animation progress for candles
    val candle1Progress = remember { Animatable(if (animated) 0f else 1f) }
    val candle2Progress = remember { Animatable(if (animated) 0f else 1f) }
    val candle3Progress = remember { Animatable(if (animated) 0f else 1f) }

    if (animated) {
        LaunchedEffect(Unit) {
            candle1Progress.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
            delay(80)
            candle2Progress.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
            delay(80)
            candle3Progress.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        }
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Draw Golden Speech Bubble
        val bubblePath = Path().apply {
            val left = w * 0.12f
            val top = h * 0.15f
            val right = w * 0.88f
            val bottom = h * 0.70f
            val radius = w * 0.18f

            addRoundRect(
                RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    cornerRadius = CornerRadius(radius, radius)
                )
            )

            // Pointer tail at bottom-left
            moveTo(w * 0.40f, bottom)
            lineTo(w * 0.20f, h * 0.88f)
            lineTo(w * 0.26f, bottom)
            close()
        }

        drawPath(path = bubblePath, color = goldColor, style = Fill)

        // Candlesticks parameters
        val wickWidth = (w * 0.035f).coerceAtLeast(1.5f)
        val bodyWidth = w * 0.11f
        val cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)

        // Candle 1 (Left, Navy)
        val c1CenterX = w * 0.35f
        val c1TopWickY = h * 0.33f
        val c1BottomWickY = h * 0.58f
        val c1BodyTop = h * 0.39f
        val c1BodyHeight = (h * 0.14f) * candle1Progress.value

        drawLine(
            color = navyColor,
            start = Offset(c1CenterX, c1TopWickY),
            end = Offset(c1CenterX, c1BottomWickY),
            strokeWidth = wickWidth
        )
        if (c1BodyHeight > 0f) {
            drawRoundRect(
                color = navyColor,
                topLeft = Offset(c1CenterX - bodyWidth / 2f, c1BodyTop),
                size = Size(bodyWidth, c1BodyHeight),
                cornerRadius = cornerRadius
            )
        }

        // Candle 2 (Middle, Navy)
        val c2CenterX = w * 0.50f
        val c2TopWickY = h * 0.29f
        val c2BottomWickY = h * 0.55f
        val c2BodyTop = h * 0.34f
        val c2BodyHeight = (h * 0.15f) * candle2Progress.value

        drawLine(
            color = navyColor,
            start = Offset(c2CenterX, c2TopWickY),
            end = Offset(c2CenterX, c2BottomWickY),
            strokeWidth = wickWidth
        )
        if (c2BodyHeight > 0f) {
            drawRoundRect(
                color = navyColor,
                topLeft = Offset(c2CenterX - bodyWidth / 2f, c2BodyTop),
                size = Size(bodyWidth, c2BodyHeight),
                cornerRadius = cornerRadius
            )
        }

        // Candle 3 (Right, Buy Green)
        val c3CenterX = w * 0.65f
        val c3TopWickY = h * 0.25f
        val c3BottomWickY = h * 0.51f
        val c3BodyTop = h * 0.29f
        val c3BodyHeight = (h * 0.16f) * candle3Progress.value

        drawLine(
            color = navyColor,
            start = Offset(c3CenterX, c3TopWickY),
            end = Offset(c3CenterX, c3BottomWickY),
            strokeWidth = wickWidth
        )
        if (c3BodyHeight > 0f) {
            drawRoundRect(
                color = greenColor,
                topLeft = Offset(c3CenterX - bodyWidth / 2f, c3BodyTop),
                size = Size(bodyWidth, c3BodyHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}

/**
 * PipChat Wordmark matching PRD Section 6.8:
 * [Icon] PipChat ("Pip" in text color, "Chat" in gold accent).
 */
@Composable
fun PipChatWordmark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    fontSize: TextUnit = 22.sp,
    isDark: Boolean = true
) {
    val customColors = LocalPipCustomColors.current
    val pipColor = if (isDark) MaterialTheme.colorScheme.onBackground else Color(0xFF121A2A)
    val chatColor = customColors.gold

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PipChatLogoIcon(size = iconSize)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = pipColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                ) {
                    append("Pip")
                }
                withStyle(
                    SpanStyle(
                        color = chatColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    )
                ) {
                    append("Chat")
                }
            },
            fontSize = fontSize,
            letterSpacing = (-0.5).sp
        )
    }
}
