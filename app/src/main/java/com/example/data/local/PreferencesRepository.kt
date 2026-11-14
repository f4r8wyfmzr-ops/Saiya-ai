package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.siyaDataStore: DataStore<Preferences> by preferencesDataStore(name = "siya_preferences")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val AUTO_SPEAK_ENABLED = booleanPreferencesKey("auto_speak_enabled")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val SELECTED_VOICE_NAME = stringPreferencesKey("selected_voice_name")
        val SAVE_CHAT_HISTORY = booleanPreferencesKey("save_chat_history")
    }

    val preferencesFlow: Flow<UserPreferences> = context.siyaDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val languageName = prefs[Keys.PREFERRED_LANGUAGE] ?: SiyaLanguage.HINGLISH.name
            val language = runCatching { SiyaLanguage.valueOf(languageName) }
                .getOrDefault(SiyaLanguage.HINGLISH)

            UserPreferences(
                ownerName = prefs[Keys.OWNER_NAME]?.takeIf { it.isNotBlank() } ?: "Vishal Boss",
                preferredLanguage = language,
                autoSpeakEnabled = prefs[Keys.AUTO_SPEAK_ENABLED] ?: true,
                speechRate = (prefs[Keys.SPEECH_RATE] ?: 1.0f).coerceIn(0.6f, 1.6f),
                speechPitch = (prefs[Keys.SPEECH_PITCH] ?: 1.06f).coerceIn(0.7f, 1.5f),
                selectedVoiceName = prefs[Keys.SELECTED_VOICE_NAME] ?: "",
                saveChatHistory = prefs[Keys.SAVE_CHAT_HISTORY] ?: true
            )
        }

    suspend fun updateOwnerName(name: String) {
        val cleaned = name.trim().ifBlank { "Vishal Boss" }
        context.siyaDataStore.edit { prefs ->
            prefs[Keys.OWNER_NAME] = cleaned
        }
    }

    suspend fun updatePreferredLanguage(language: SiyaLanguage) {
        context.siyaDataStore.edit { prefs ->
            prefs[Keys.PREFERRED_LANGUAGE] = language.name
        }
    }

    suspend fun updateAutoSpeak(enabled: Boolean) {
        context.siyaDataStore.edit { prefs ->
            prefs[Keys.AUTO_SPEAK_ENABLED] = enabled
        }
    }

    suspend fun updateSpeechRate(rate: Float) {
        context.siyaDataStore.edit { prefs ->
            prefs[Keys.SPEECH_RATE] = rate.coerceIn(0.6f, 1.6f)
        }
    }

    suspend fun updateSpeechPitch(pitch: Float) {
        context.siyaDataStore.edit { prefs ->
            prefs[Keys.SPEECH_PITCH] = pitch.coerceIn(0.7f, 1.5f)
        }
    }

    suspend fun updateSelectedVoiceName(voiceName: String) {
        context.siyaDataStore.edit { prefs ->
            prefs[Keys.SELECTED_VOICE_NAME] = voiceName
        }
    }

    suspend fun updateSaveChatHistory(enabled: Boolean) {
        context.siyaDataStore.edit { prefs ->
            prefs[Keys.SAVE_CHAT_HISTORY] = enabled
        }
    }

    suspend fun resetToDefaults() {
        context.siyaDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
