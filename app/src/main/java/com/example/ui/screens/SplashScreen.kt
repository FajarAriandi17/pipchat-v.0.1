package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PipChatLogoIcon
import com.example.ui.components.PipChatWordmark
import com.example.ui.theme.LocalPipCustomColors
import com.example.ui.theme.PipDarkBg
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalPipCustomColors.current
    val scale = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        // Animation per PRD Section 7 (600ms easeOutCubic scale & fade)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
        )
        delay(1000) // Brief pause to display branding & initialize sessions
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PipDarkBg)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            PipChatLogoIcon(
                size = 96.dp,
                animated = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            PipChatWordmark(
                iconSize = 0.dp, // Only show the wordmark text below the big icon
                fontSize = 32.sp,
                isDark = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI Forex & Gold Analysis",
                style = MaterialTheme.typography.labelSmall,
                color = customColors.textMuted,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        }

        // Bottom disclaimer badge
        Text(
            text = "PipChat · Technical Analyst",
            style = MaterialTheme.typography.labelSmall,
            color = customColors.textMuted.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
