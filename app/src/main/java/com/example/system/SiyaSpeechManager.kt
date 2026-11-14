package com.example.system

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.local.SiyaLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

data class VoiceOption(
    val name: String,
    val displayLabel: String,
    val localeTag: String,
    val isLikelyFemale: Boolean
)

class SiyaSpeechManager(context: Context) {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceOption>>(emptyList())
    val availableVoices: StateFlow<List<VoiceOption>> = _availableVoices.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                _isReady.value = true
                refreshVoices()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            } else {
                _isReady.value = false
            }
        }
    }

    private fun refreshVoices() {
        val voices = runCatching { tts?.voices?.toList().orEmpty() }.getOrDefault(emptyList())
        val mapped = voices
            .filter { voice ->
                val lang = voice.locale.language.lowercase()
                lang == "hi" || lang == "en"
            }
            .map { voice ->
                val likelyFemale = isFemaleVoice(voice)
                val tag = voice.locale.toLanguageTag()
                val label = buildString {
                    append(voice.locale.displayLanguage)
                    if (voice.locale.country.isNotBlank()) {
                        append(" (${voice.locale.country})")
                    }
                    if (likelyFemale) append(" • Female")
                    append(" • ${voice.name.takeLast(14)}")
                }
                VoiceOption(
                    name = voice.name,
                    displayLabel = label,
                    localeTag = tag,
                    isLikelyFemale = likelyFemale
                )
            }
            .sortedWith(
                compareByDescending<VoiceOption> { it.isLikelyFemale }
                    .thenByDescending { it.localeTag.contains("IN", ignoreCase = true) }
                    .thenBy { it.displayLabel }
            )
        _availableVoices.value = mapped
    }

    fun speak(
        text: String,
        language: SiyaLanguage,
        rate: Float,
        pitch: Float,
        preferredVoiceName: String
    ) {
        val engine = tts ?: return
        if (!_isReady.value || text.isBlank()) return

        val cleanedText = text
            .replace("[Sample AI]", "")
            .replace("[Sample Response]", "")
            .trim()

        val targetLocale = when (language) {
            SiyaLanguage.HINDI -> Locale.forLanguageTag("hi-IN")
            SiyaLanguage.HINGLISH -> Locale.forLanguageTag("en-IN")
            SiyaLanguage.ENGLISH -> Locale.forLanguageTag("en-IN")
        }

        runCatching {
            val allVoices = engine.voices?.toList().orEmpty()
            val chosenVoice = if (preferredVoiceName.isNotBlank()) {
                allVoices.firstOrNull { it.name == preferredVoiceName }
            } else {
                pickBestFemaleVoice(allVoices, targetLocale)
            }

            if (chosenVoice != null) {
                engine.voice = chosenVoice
            } else {
                val res = engine.setLanguage(targetLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.language = Locale.US
                }
            }

            engine.setSpeechRate(rate.coerceIn(0.6f, 1.6f))
            engine.setPitch(pitch.coerceIn(0.7f, 1.5f))

            val utteranceId = UUID.randomUUID().toString()
            engine.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        runCatching { tts?.stop() }
        _isSpeaking.value = false
    }

    fun shutdown() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
        _isSpeaking.value = false
        _isReady.value = false
    }

    private fun pickBestFemaleVoice(voices: List<Voice>, targetLocale: Locale): Voice? {
        if (voices.isEmpty()) return null
        val sameLanguage = voices.filter {
            it.locale.language.equals(targetLocale.language, ignoreCase = true)
        }
        val pool = sameLanguage.ifEmpty { voices }
        return pool
            .sortedWith(
                compareByDescending<Voice> { isFemaleVoice(it) }
                    .thenByDescending { it.locale.country.equals("IN", ignoreCase = true) }
                    .thenByDescending { !it.isNetworkConnectionRequired }
                    .thenByDescending { it.quality }
            )
            .firstOrNull()
    }

    private fun isFemaleVoice(voice: Voice): Boolean {
        val lowerName = voice.name.lowercase()
        val features = voice.features?.joinToString(" ")?.lowercase().orEmpty()
        // Common Google TTS / Android female voice identifiers
        return lowerName.contains("female") ||
            lowerName.contains("-f-") ||
            lowerName.contains("hia") ||
            lowerName.contains("hid") ||
            lowerName.contains("ena") ||
            lowerName.contains("iob") ||
            lowerName.contains("sfg") ||
            lowerName.contains("tpf") ||
            features.contains("female")
    }
}
