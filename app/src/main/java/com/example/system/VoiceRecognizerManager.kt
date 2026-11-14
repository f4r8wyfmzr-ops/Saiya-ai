package com.example.system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface VoiceRecognitionState {
    data object Idle : VoiceRecognitionState
    data class Listening(
        val partialText: String = "",
        val rmsLevel: Float = 0f
    ) : VoiceRecognitionState
    data class Error(val message: String) : VoiceRecognitionState
}

class VoiceRecognizerManager(
    private val context: Context,
    private val onFinalTranscript: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(languageTag: String) {
        if (!isAvailable()) {
            _state.value = VoiceRecognitionState.Error(
                "Speech recognition service is not available on this device. You can use text chat anytime."
            )
            return
        }

        stopListeningInternal()

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = VoiceRecognitionState.Listening(partialText = "", rmsLevel = 0.1f)
            }

            override fun onBeginningOfSpeech() {
                val currentPartial = (_state.value as? VoiceRecognitionState.Listening)?.partialText.orEmpty()
                _state.value = VoiceRecognitionState.Listening(partialText = currentPartial, rmsLevel = 0.35f)
            }

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                val currentPartial = (_state.value as? VoiceRecognitionState.Listening)?.partialText.orEmpty()
                _state.value = VoiceRecognitionState.Listening(
                    partialText = currentPartial,
                    rmsLevel = normalized
                )
            }

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                val currentPartial = (_state.value as? VoiceRecognitionState.Listening)?.partialText.orEmpty()
                _state.value = VoiceRecognitionState.Listening(partialText = currentPartial, rmsLevel = 0.15f)
            }

            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check your microphone."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice input."
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition. Please try again or type your message."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Sorry, I couldn't catch that. Please tap the mic and speak again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Please try again in a moment."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap the mic whenever you're ready."
                    else -> "Voice recognition couldn't complete (code $error). You can also type below."
                }
                _state.value = VoiceRecognitionState.Error(msg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.firstOrNull()?.trim().orEmpty()
                _state.value = VoiceRecognitionState.Idle
                if (transcript.isNotBlank()) {
                    onFinalTranscript(transcript)
                } else {
                    _state.value = VoiceRecognitionState.Error("Couldn't hear any words. Please try again.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()
                    .orEmpty()
                if (partial.isNotBlank()) {
                    val rms = (_state.value as? VoiceRecognitionState.Listening)?.rmsLevel ?: 0.3f
                    _state.value = VoiceRecognitionState.Listening(partialText = partial, rmsLevel = rms)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        _state.value = VoiceRecognitionState.Listening()
        runCatching {
            recognizer.startListening(intent)
        }.onFailure { e ->
            _state.value = VoiceRecognitionState.Error(
                e.localizedMessage ?: "Unable to start speech recognition."
            )
        }
    }

    fun stopListening() {
        runCatching { speechRecognizer?.stopListening() }
        _state.value = VoiceRecognitionState.Idle
    }

    fun clearError() {
        if (_state.value is VoiceRecognitionState.Error) {
            _state.value = VoiceRecognitionState.Idle
        }
    }

    private fun stopListeningInternal() {
        runCatching {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        }
        speechRecognizer = null
    }

    fun destroy() {
        stopListeningInternal()
        _state.value = VoiceRecognitionState.Idle
    }
}
