package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.model.MessageStatus
import com.example.model.SignalCard
import com.example.model.UserSettings
import com.example.ui.components.PipChatLogoIcon
import com.example.ui.components.PipChatWordmark
import com.example.ui.components.SignalCardView
import com.example.ui.theme.LocalPipCustomColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    isStreaming: Boolean,
    currentToolStatus: String?,
    sessionTitle: String,
    settings: UserSettings,
    onSendMessage: (String) -> Unit,
    onStopStreaming: () -> Unit,
    onNewChat: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onViewChart: (SignalCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customColors = LocalPipCustomColors.current
    val isIndonesian = settings.language == "id"
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when messages update
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < messages.size - 3
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PipChatLogoIcon(size = 28.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sessionTitle.ifBlank { if (isIndonesian) "Percakapan" else "Conversation" },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Drawer"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = customColors.gold
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (showScrollToBottom) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    },
                    containerColor = customColors.surface2,
                    contentColor = customColors.gold,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Chat Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (messages.isEmpty()) {
                    EmptyChatGreeting(
                        isIndonesian = isIndonesian,
                        onSuggestionClicked = { suggestion ->
                            inputText = suggestion
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            if (message.role == MessageRole.USER) {
                                UserMessageBubble(message = message)
                            } else {
                                AssistantMessageView(
                                    message = message,
                                    isIndonesian = isIndonesian,
                                    onViewChart = onViewChart,
                                    onRegenerate = { onSendMessage(messages.findLast { it.role == MessageRole.USER }?.text ?: "") }
                                )
                            }
                        }

                        // Active Tool Status Chip
                        if (isStreaming && !currentToolStatus.isNullOrBlank()) {
                            item {
                                ToolStatusChip(status = currentToolStatus)
                            }
                        }
                    }
                }
            }

            // Input Bar at bottom
            ChatInputBar(
                inputText = inputText,
                isStreaming = isStreaming,
                isIndonesian = isIndonesian,
                onTextChanged = { inputText = it },
                onSend = {
                    val msg = inputText
                    inputText = ""
                    onSendMessage(msg)
                },
                onStop = onStopStreaming
            )
        }
    }
}

@Composable
private fun EmptyChatGreeting(
    isIndonesian: Boolean,
    onSuggestionClicked: (String) -> Unit
) {
    val customColors = LocalPipCustomColors.current
    val suggestions = if (isIndonesian) {
        listOf(
            "Analisa EURUSD H1 dan beri level Entry/TP/SL",
            "Cari level support dan resistance XAUUSD",
            "Cek keselarasan tren GBPUSD multi-timeframe",
            "Jelaskan cara membaca indikator RSI dan MACD"
        )
    } else {
        listOf(
            "Analyze EURUSD H1 and provide Entry/TP/SL setup",
            "Find key support and resistance for XAUUSD (Gold)",
            "Check multi-timeframe trend alignment for GBPUSD",
            "Explain how to read RSI and MACD indicators"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PipChatLogoIcon(
            size = 76.dp,
            animated = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isIndonesian) "Mau analisa apa hari ini?" else "What shall we analyze today?",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isIndonesian) "Tanyakan analisa teknikal pair forex atau emas (XAUUSD)" else "Ask for any Forex pair or Gold technical analysis",
            style = MaterialTheme.typography.bodyLarge,
            color = customColors.textMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 2x2 Grid of Suggestions (PRD Section 6.4)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SuggestionCard(
                    text = suggestions[0],
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClicked(suggestions[0]) }
                )
                SuggestionCard(
                    text = suggestions[1],
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClicked(suggestions[1]) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SuggestionCard(
                    text = suggestions[2],
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClicked(suggestions[2]) }
                )
                SuggestionCard(
                    text = suggestions[3],
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClicked(suggestions[3]) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val customColors = LocalPipCustomColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(customColors.surface2)
            .border(1.dp, customColors.line, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    val customColors = LocalPipCustomColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 4.dp // PRD Section 6.2 user bubble radius
                    )
                )
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, customColors.line, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AssistantMessageView(
    message: ChatMessage,
    isIndonesian: Boolean,
    onViewChart: (SignalCard) -> Unit,
    onRegenerate: () -> Unit
) {
    val context = LocalContext.current
    val customColors = LocalPipCustomColors.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Text portion
        // Remove raw JSON ```signal block from readable text if signal card is rendered
        val cleanText = if (message.text.contains("```signal")) {
            val idx = message.text.indexOf("```signal")
            val endIdx = message.text.indexOf("```", idx + 9)
            if (endIdx != -1) {
                message.text.removeRange(idx, endIdx + 3).trim()
            } else {
                message.text.substring(0, idx).trim()
            }
        } else {
            message.text
        }

        if (cleanText.isNotBlank()) {
            Text(
                text = cleanText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Render Signal Card if available
        message.signal?.let { card ->
            Spacer(modifier = Modifier.height(14.dp))
            SignalCardView(
                signal = card,
                isIndonesian = isIndonesian,
                onViewChart = onViewChart
            )
        }

        // Assistant Footer: Copy & Regenerate actions
        if (message.status == MessageStatus.COMPLETE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipMgr.setPrimaryClip(ClipData.newPlainText("Analysis", cleanText))
                        Toast.makeText(context, if (isIndonesian) "Analisa disalin" else "Analysis copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy text",
                        tint = customColors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint = customColors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolStatusChip(status: String) {
    val customColors = LocalPipCustomColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(customColors.surface2)
            .border(1.dp, customColors.gold.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(12.dp),
            color = customColors.gold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = customColors.gold,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    isStreaming: Boolean,
    isIndonesian: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    val customColors = LocalPipCustomColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, customColors.line, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isIndonesian) "Ketik pair (mis. Analisa EURUSD H1)…" else "Ask PipChat (e.g. Analyze EURUSD H1)…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = customColors.textMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank() && !isStreaming) onSend()
                })
            )

            // Send or Stop Button
            if (isStreaming) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(customColors.sell)
                        .testTag("stop_streaming_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) onSend()
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) customColors.gold else customColors.surface2)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.Black else customColors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Mandatory Disclaimer under input per PRD Section 6.4 & 14
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isIndonesian) "Bukan saran keuangan. Trading berisiko kehilangan modal." else "Not financial advice. Trading involves risk of capital loss.",
            style = MaterialTheme.typography.labelSmall,
            color = customColors.textMuted,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
