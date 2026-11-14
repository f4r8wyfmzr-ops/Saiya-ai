package com.example.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.ai.AiRepository
import com.example.data.ai.PendingAssistantAction
import com.example.data.ai.SampleAiRepository
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatRepository
import com.example.data.local.PreferencesRepository
import com.example.data.local.ReminderNoteEntity
import com.example.data.local.SiyaDatabase
import com.example.data.local.SiyaLanguage
import com.example.data.local.UserPreferences
import com.example.system.AppLauncherHelper
import com.example.system.BatteryTelemetry
import com.example.system.DeviceInfoHelper
import com.example.system.SiyaSpeechManager
import com.example.system.SystemActionHelper
import com.example.system.VoiceRecognitionState
import com.example.system.VoiceRecognizerManager
import com.example.system.VoiceOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantMood {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class SiyaUiState(
    val preferences: UserPreferences = UserPreferences(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val reminders: List<ReminderNoteEntity> = emptyList(),
    val isThinking: Boolean = false,
    val voiceState: VoiceRecognitionState = VoiceRecognitionState.Idle,
    val isSpeaking: Boolean = false,
    val isTtsReady: Boolean = false,
    val availableVoices: List<VoiceOption> = emptyList(),
    val pendingAction: PendingAssistantAction? = null,
    val battery: BatteryTelemetry = BatteryTelemetry(100, false, "Ready"),
    val currentTimeText: String = "",
    val hasMicPermission: Boolean = false,
    val showFirebaseNotice: Boolean = true,
    val statusSnackbar: String? = null
) {
    val mood: AssistantMood
        get() = when {
            voiceState is VoiceRecognitionState.Listening -> AssistantMood.LISTENING
            isThinking -> AssistantMood.THINKING
            isSpeaking -> AssistantMood.SPEAKING
            else -> AssistantMood.IDLE
        }

    val greetingMessage: String
        get() = "Hello ${preferences.ownerName}! Siya is ready. Bataiye aaj kya karna hai?"
}

class SiyaViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    private val database = SiyaDatabase.getInstance(application)
    private val chatRepository = ChatRepository(database.chatMessageDao(), database.reminderNoteDao())
    private val deviceInfoHelper = DeviceInfoHelper(application)
    private val appLauncherHelper = AppLauncherHelper(application)
    private val systemActionHelper = SystemActionHelper(application)
    private val aiRepository: AiRepository = SampleAiRepository(deviceInfoHelper, appLauncherHelper)

    private val speechManager = SiyaSpeechManager(application)
    private val voiceRecognizer = VoiceRecognizerManager(application) { transcript ->
        sendUserMessage(transcript)
    }

    private val _isThinking = MutableStateFlow(false)
    private val _pendingAction = MutableStateFlow<PendingAssistantAction?>(null)
    private val _battery = MutableStateFlow(deviceInfoHelper.getBatteryTelemetry())
    private val _currentTime = MutableStateFlow(deviceInfoHelper.getFormattedTime())
    private val _hasMicPermission = MutableStateFlow(checkMicPermission())
    private val _showFirebaseNotice = MutableStateFlow(true)
    private val _statusSnackbar = MutableStateFlow<String?>(null)

    private val internalState = combine(
        _isThinking,
        _pendingAction,
        _battery,
        _currentTime,
        _hasMicPermission
    ) { thinking, action, battery, time, hasMic ->
        InternalSignals(thinking, action, battery, time, hasMic)
    }

    private val mediaState = combine(
        voiceRecognizer.state,
        speechManager.isSpeaking,
        speechManager.isReady,
        speechManager.availableVoices
    ) { voice, speaking, ttsReady, voices ->
        MediaSignals(voice, speaking, ttsReady, voices)
    }

    val uiState: StateFlow<SiyaUiState> = combine(
        preferencesRepository.preferencesFlow,
        chatRepository.messages,
        chatRepository.reminders,
        internalState,
        mediaState
    ) { prefs, messages, reminders, internal, media ->
        SiyaUiState(
            preferences = prefs,
            messages = messages,
            reminders = reminders,
            isThinking = internal.isThinking,
            voiceState = media.voiceState,
            isSpeaking = media.isSpeaking,
            isTtsReady = media.isTtsReady,
            availableVoices = media.availableVoices,
            pendingAction = internal.pendingAction,
            battery = internal.battery,
            currentTimeText = internal.time,
            hasMicPermission = internal.hasMicPermission,
            showFirebaseNotice = _showFirebaseNotice.value && !aiRepository.isLiveGeminiEnabled,
            statusSnackbar = _statusSnackbar.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SiyaUiState(
            battery = deviceInfoHelper.getBatteryTelemetry(),
            currentTimeText = deviceInfoHelper.getFormattedTime(),
            hasMicPermission = checkMicPermission()
        )
    )

    fun refreshTelemetryAndPermissions() {
        _battery.value = deviceInfoHelper.getBatteryTelemetry()
        _currentTime.value = deviceInfoHelper.getFormattedTime()
        _hasMicPermission.value = checkMicPermission()
    }

    fun sendUserMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank()) return

        refreshTelemetryAndPermissions()
        speechManager.stop()
        voiceRecognizer.clearError()

        viewModelScope.launch {
            val prefs = uiState.value.preferences
            if (prefs.saveChatHistory) {
                chatRepository.addMessage(
                    ChatMessageEntity(
                        text = text,
                        isFromUser = true
                    )
                )
            }

            _isThinking.value = true
            try {
                val reply = aiRepository.processPrompt(
                    prompt = text,
                    ownerName = prefs.ownerName,
                    language = prefs.preferredLanguage
                )

                if (prefs.saveChatHistory) {
                    chatRepository.addMessage(
                        ChatMessageEntity(
                            text = reply.text,
                            isFromUser = false,
                            isSampleAiResponse = reply.isSampleFallback,
                            actionLabel = reply.actionLabel
                        )
                    )
                }

                _pendingAction.value = reply.pendingAction

                if (prefs.autoSpeakEnabled) {
                    speechManager.speak(
                        text = reply.text,
                        language = prefs.preferredLanguage,
                        rate = prefs.speechRate,
                        pitch = prefs.speechPitch,
                        preferredVoiceName = prefs.selectedVoiceName
                    )
                }
            } catch (e: Exception) {
                _statusSnackbar.value = e.localizedMessage ?: "Unable to process request right now."
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun startVoiceListening() {
        refreshTelemetryAndPermissions()
        if (!_hasMicPermission.value) {
            _statusSnackbar.value = getApplication<Application>().getString(R.string.mic_permission_denied_notice)
            return
        }
        speechManager.stop()
        val localeTag = uiState.value.preferences.preferredLanguage.speechLocaleTag
        voiceRecognizer.startListening(localeTag)
    }

    fun stopVoiceListening() {
        voiceRecognizer.stopListening()
    }

    fun clearVoiceError() {
        voiceRecognizer.clearError()
    }

    fun speakMessage(text: String) {
        val prefs = uiState.value.preferences
        speechManager.speak(
            text = text,
            language = prefs.preferredLanguage,
            rate = prefs.speechRate,
            pitch = prefs.speechPitch,
            preferredVoiceName = prefs.selectedVoiceName
        )
    }

    fun speakGreeting() {
        speakMessage(uiState.value.greetingMessage)
    }

    fun stopSpeaking() {
        speechManager.stop()
    }

    fun confirmPendingAction() {
        val action = _pendingAction.value ?: return
        _pendingAction.value = null

        viewModelScope.launch {
            val prefs = uiState.value.preferences
            val success = when (action) {
                is PendingAssistantAction.OpenApp -> appLauncherHelper.launchApp(action.packageName)
                is PendingAssistantAction.SetAlarm -> systemActionHelper.openAlarmIntent(
                    action.hour24,
                    action.minute,
                    action.label
                )
                is PendingAssistantAction.SetTimer -> systemActionHelper.openTimerIntent(
                    action.durationSeconds,
                    action.label
                )
                is PendingAssistantAction.CreateReminder -> {
                    chatRepository.addReminder(
                        ReminderNoteEntity(
                            title = action.title,
                            triggerTimeMillis = action.triggerTimeMillis
                        )
                    )
                    systemActionHelper.openReminderCalendarIntent(action.title, action.triggerTimeMillis)
                }
            }

            if (!success) {
                _statusSnackbar.value = when (action) {
                    is PendingAssistantAction.CreateReminder ->
                        "Saved reminder locally in SIYA AI (no Calendar app found to open)."
                    else ->
                        "Couldn't find a supported system app for this action on your device."
                }
            } else {
                val confirmationNote = when (action) {
                    is PendingAssistantAction.OpenApp -> "Opened ${action.appName} for you, ${prefs.ownerName}."
                    is PendingAssistantAction.SetAlarm -> "Clock opened to confirm your alarm, ${prefs.ownerName}."
                    is PendingAssistantAction.SetTimer -> "Clock opened to start your timer, ${prefs.ownerName}."
                    is PendingAssistantAction.CreateReminder -> "Reminder saved locally & Calendar opened, ${prefs.ownerName}."
                }
                _statusSnackbar.value = confirmationNote
            }
        }
    }

    fun dismissPendingAction() {
        _pendingAction.value = null
        _statusSnackbar.value = "Action cancelled."
    }

    fun updateOwnerName(name: String) {
        viewModelScope.launch { preferencesRepository.updateOwnerName(name) }
    }

    fun updateLanguage(language: SiyaLanguage) {
        viewModelScope.launch { preferencesRepository.updatePreferredLanguage(language) }
    }

    fun cycleLanguage() {
        val all = SiyaLanguage.entries
        val currentIdx = all.indexOf(uiState.value.preferences.preferredLanguage)
        val next = all[(currentIdx + 1) % all.size]
        updateLanguage(next)
    }

    fun updateAutoSpeak(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateAutoSpeak(enabled) }
    }

    fun updateSpeechRate(rate: Float) {
        viewModelScope.launch { preferencesRepository.updateSpeechRate(rate) }
    }

    fun updateSpeechPitch(pitch: Float) {
        viewModelScope.launch { preferencesRepository.updateSpeechPitch(pitch) }
    }

    fun updateSelectedVoice(voiceName: String) {
        viewModelScope.launch { preferencesRepository.updateSelectedVoiceName(voiceName) }
    }

    fun updateSaveChatHistory(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateSaveChatHistory(enabled) }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearMessages()
            _statusSnackbar.value = "Conversation history cleared."
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch { chatRepository.deleteReminder(id) }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            preferencesRepository.resetToDefaults()
            _statusSnackbar.value = "Preferences reset to defaults."
        }
    }

    fun openAppSettings() {
        systemActionHelper.openAppDetailsSettings()
    }

    fun dismissFirebaseNotice() {
        _showFirebaseNotice.value = false
    }

    fun clearSnackbar() {
        _statusSnackbar.value = null
    }

    fun notifyMessage(msg: String) {
        _statusSnackbar.value = msg
    }

    private fun checkMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognizer.destroy()
        speechManager.shutdown()
    }

    private data class InternalSignals(
        val isThinking: Boolean,
        val pendingAction: PendingAssistantAction?,
        val battery: BatteryTelemetry,
        val time: String,
        val hasMicPermission: Boolean
    )

    private data class MediaSignals(
        val voiceState: VoiceRecognitionState,
        val isSpeaking: Boolean,
        val isTtsReady: Boolean,
        val availableVoices: List<VoiceOption>
    )
}
