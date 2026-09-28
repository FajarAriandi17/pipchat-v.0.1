package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AnalysisStreamEvent
import com.example.data.ai.GeminiForexAnalyst
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.PipChatDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import com.example.data.repository.MarketDataRepository
import com.example.model.Candle
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.model.MessageRole
import com.example.model.MessageStatus
import com.example.model.SignalCard
import com.example.model.UserProfile
import com.example.model.UserSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class UiState(
    val currentSessionId: String = "",
    val sessions: List<ChatSession> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val currentToolStatus: String? = null,
    val selectedSignalForChart: SignalCard? = null,
    val chartCandles: List<Candle> = emptyList(),
    val isChartLoading: Boolean = false,
    val currentChartTimeframe: String = "H1",
    val userSettings: UserSettings = UserSettings(),
    val authState: AuthState = AuthState.Unauthenticated,
    val currentUser: UserProfile? = null,
    val errorMessage: String? = null
)

class PipChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PipChatDatabase.getInstance(application)
    private val dao = db.chatDao()
    private val analyst = GeminiForexAnalyst()
    private val marketRepo = MarketDataRepository()
    private val authRepo = AuthRepository(application)
    private val prefs = application.getSharedPreferences("pipchat_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val signalAdapter = moshi.adapter(SignalCard::class.java)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null

    init {
        loadSettings()
        initSessions()
        initAuth()
    }

    private fun initAuth() {
        viewModelScope.launch {
            authRepo.authState.collect { state ->
                val user = authRepo.getCurrentUser()
                _uiState.update { it.copy(authState = state, currentUser = user) }
            }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            authRepo.signInWithGoogle()
        }
    }

    fun loginWithApple() {
        viewModelScope.launch {
            authRepo.signInWithApple()
        }
    }

    fun requestOtp(email: String) {
        viewModelScope.launch {
            authRepo.requestOtp(email)
        }
    }

    fun verifyOtp(email: String, code: String) {
        viewModelScope.launch {
            authRepo.verifyOtp(email, code)
        }
    }

    fun loginWithPassword(email: String, pass: String) {
        viewModelScope.launch {
            authRepo.signInWithEmailPassword(email, pass)
        }
    }

    fun continueAsGuest() {
        authRepo.continueAsGuest()
    }

    fun logout() {
        authRepo.logout()
    }

    private fun loadSettings() {
        val lang = prefs.getString("lang", "id") ?: "id"
        val theme = prefs.getString("theme", "dark") ?: "dark"
        val style = prefs.getString("style", "day") ?: "day"
        val risk = prefs.getFloat("risk", 1.5f).toDouble()
        _uiState.update {
            it.copy(userSettings = UserSettings(lang, theme, style, risk))
        }
    }

    fun updateSettings(newSettings: UserSettings) {
        prefs.edit()
            .putString("lang", newSettings.language)
            .putString("theme", newSettings.themeMode)
            .putString("style", newSettings.tradingStyle)
            .putFloat("risk", newSettings.riskPercent.toFloat())
            .apply()
        _uiState.update { it.copy(userSettings = newSettings) }
    }

    private fun initSessions() {
        viewModelScope.launch {
            dao.getAllSessions().collect { entities ->
                val sessions = entities.map {
                    ChatSession(
                        id = it.id,
                        title = it.title,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        preview = it.preview
                    )
                }

                _uiState.update { current ->
                    if (sessions.isEmpty()) {
                        // Create initial default session
                        val newId = UUID.randomUUID().toString()
                        val newSession = ChatSession(
                            id = newId,
                            title = if (current.userSettings.language == "id") "Percakapan Baru" else "New Conversation",
                            preview = ""
                        )
                        viewModelScope.launch {
                            dao.insertSession(
                                ChatSessionEntity(
                                    id = newId,
                                    title = newSession.title,
                                    createdAt = newSession.createdAt,
                                    updatedAt = newSession.updatedAt,
                                    preview = ""
                                )
                            )
                        }
                        current.copy(sessions = listOf(newSession), currentSessionId = newId)
                    } else {
                        val activeId = if (current.currentSessionId.isNotEmpty() && sessions.any { it.id == current.currentSessionId }) {
                            current.currentSessionId
                        } else {
                            sessions.first().id
                        }
                        current.copy(sessions = sessions, currentSessionId = activeId)
                    }
                }

                loadMessagesForSession(_uiState.value.currentSessionId)
            }
        }
    }

    fun switchSession(sessionId: String) {
        _uiState.update { it.copy(currentSessionId = sessionId) }
        loadMessagesForSession(sessionId)
    }

    fun createNewChat() {
        val newId = UUID.randomUUID().toString()
        val title = if (_uiState.value.userSettings.language == "id") "Analisa Baru" else "New Analysis"
        val session = ChatSessionEntity(
            id = newId,
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            preview = ""
        )
        viewModelScope.launch {
            dao.insertSession(session)
            _uiState.update { it.copy(currentSessionId = newId, messages = emptyList()) }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            dao.deleteSession(sessionId)
            dao.deleteMessagesForSession(sessionId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearAllSessions()
            dao.clearAllMessages()
            createNewChat()
        }
    }

    private fun loadMessagesForSession(sessionId: String) {
        if (sessionId.isEmpty()) return
        viewModelScope.launch {
            dao.getMessagesForSession(sessionId).collect { entities ->
                val list = entities.map { entity ->
                    val signalCard = entity.signalJson?.let { json ->
                        try { signalAdapter.fromJson(json) } catch (_: Exception) { null }
                    }
                    val role = if (entity.role == "USER") MessageRole.USER else MessageRole.ASSISTANT
                    val status = when (entity.status) {
                        "ERROR" -> MessageStatus.ERROR
                        "STOPPED" -> MessageStatus.STOPPED
                        else -> MessageStatus.COMPLETE
                    }
                    ChatMessage(
                        id = entity.id,
                        role = role,
                        text = entity.text,
                        timestamp = entity.timestamp,
                        signal = signalCard,
                        status = status
                    )
                }
                _uiState.update { it.copy(messages = list) }
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isStreaming) return

        val sessionId = _uiState.value.currentSessionId
        val userMsgId = UUID.randomUUID().toString()
        val userMsg = ChatMessage(
            id = userMsgId,
            role = MessageRole.USER,
            text = trimmed,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.COMPLETE
        )

        // Generate dynamic title if it's the first message
        val currentMsgs = _uiState.value.messages
        if (currentMsgs.isEmpty()) {
            val title = trimmed.take(28)
            viewModelScope.launch {
                dao.renameSession(sessionId, title, System.currentTimeMillis())
            }
        }

        // Persist User Message
        viewModelScope.launch {
            dao.insertMessage(
                ChatMessageEntity(
                    id = userMsgId,
                    sessionId = sessionId,
                    role = "USER",
                    text = trimmed,
                    timestamp = userMsg.timestamp,
                    signalJson = null,
                    status = "COMPLETE"
                )
            )
        }

        // Prepare Assistant placeholder
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantPlaceholder = ChatMessage(
            id = assistantMsgId,
            role = MessageRole.ASSISTANT,
            text = "",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.STREAMING
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg + assistantPlaceholder,
                isStreaming = true,
                currentToolStatus = if (it.userSettings.language == "id") "Memulai analisa…" else "Initializing analysis…"
            )
        }

        // Start streaming analysis
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            var accumulatedText = ""
            var receivedSignal: SignalCard? = null

            try {
                analyst.analyzeStream(trimmed, _uiState.value.userSettings).collect { event ->
                    when (event) {
                        is AnalysisStreamEvent.Status -> {
                            _uiState.update { it.copy(currentToolStatus = event.message) }
                        }
                        is AnalysisStreamEvent.Token -> {
                            accumulatedText += event.text
                            updateStreamingAssistantMessage(assistantMsgId, accumulatedText, receivedSignal, MessageStatus.STREAMING)
                        }
                        is AnalysisStreamEvent.Signal -> {
                            receivedSignal = event.card
                            updateStreamingAssistantMessage(assistantMsgId, accumulatedText, receivedSignal, MessageStatus.STREAMING)
                        }
                        is AnalysisStreamEvent.Done -> {
                            accumulatedText = event.fullText
                            val finalCard = event.card ?: receivedSignal
                            updateStreamingAssistantMessage(assistantMsgId, accumulatedText, finalCard, MessageStatus.COMPLETE)

                            // Save to Room
                            val signalJson = finalCard?.let { signalAdapter.toJson(it) }
                            dao.insertMessage(
                                ChatMessageEntity(
                                    id = assistantMsgId,
                                    sessionId = sessionId,
                                    role = "ASSISTANT",
                                    text = accumulatedText,
                                    timestamp = System.currentTimeMillis(),
                                    signalJson = signalJson,
                                    status = "COMPLETE"
                                )
                            )

                            _uiState.update {
                                it.copy(
                                    isStreaming = false,
                                    currentToolStatus = null
                                )
                            }
                        }
                        is AnalysisStreamEvent.Error -> {
                            _uiState.update {
                                it.copy(
                                    isStreaming = false,
                                    currentToolStatus = null,
                                    errorMessage = event.message
                                )
                            }
                            updateStreamingAssistantMessage(assistantMsgId, "Error: ${event.message}", null, MessageStatus.ERROR)
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isStreaming = false, currentToolStatus = null) }
            }
        }
    }

    private fun updateStreamingAssistantMessage(
        msgId: String,
        text: String,
        signal: SignalCard?,
        status: MessageStatus
    ) {
        _uiState.update { current ->
            val updated = current.messages.map { msg ->
                if (msg.id == msgId) {
                    msg.copy(text = text, signal = signal, status = status)
                } else msg
            }
            current.copy(messages = updated)
        }
    }

    fun stopStreaming() {
        streamJob?.cancel()
        _uiState.update { current ->
            val updated = current.messages.map { msg ->
                if (msg.status == MessageStatus.STREAMING) {
                    msg.copy(status = MessageStatus.STOPPED)
                } else msg
            }
            current.copy(isStreaming = false, currentToolStatus = null, messages = updated)
        }
    }

    fun openChartForSignal(signal: SignalCard) {
        _uiState.update {
            it.copy(
                selectedSignalForChart = signal,
                currentChartTimeframe = signal.timeframe,
                isChartLoading = true
            )
        }
        loadChartData(signal.pair, signal.timeframe)
    }

    fun changeChartTimeframe(tf: String) {
        val signal = _uiState.value.selectedSignalForChart ?: return
        _uiState.update {
            it.copy(
                currentChartTimeframe = tf,
                selectedSignalForChart = signal.copy(timeframe = tf),
                isChartLoading = true
            )
        }
        loadChartData(signal.pair, tf)
    }

    private fun loadChartData(pair: String, tf: String) {
        viewModelScope.launch {
            val candles = marketRepo.getCandles(pair, tf, 80)
            _uiState.update {
                it.copy(chartCandles = candles, isChartLoading = false)
            }
        }
    }

    fun clearChart() {
        _uiState.update { it.copy(selectedSignalForChart = null, chartCandles = emptyList()) }
    }
}
