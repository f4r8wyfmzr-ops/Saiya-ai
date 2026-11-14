package com.example.data.local

enum class SiyaLanguage(
    val displayName: String,
    val speechLocaleTag: String,
    val shortLabel: String
) {
    HINGLISH(
        displayName = "Hinglish (Hindi + English)",
        speechLocaleTag = "en-IN",
        shortLabel = "Hinglish"
    ),
    HINDI(
        displayName = "हिन्दी (Hindi)",
        speechLocaleTag = "hi-IN",
        shortLabel = "हिन्दी"
    ),
    ENGLISH(
        displayName = "English",
        speechLocaleTag = "en-IN",
        shortLabel = "English"
    )
}

data class UserPreferences(
    val ownerName: String = "Vishal Boss",
    val preferredLanguage: SiyaLanguage = SiyaLanguage.HINGLISH,
    val autoSpeakEnabled: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.06f,
    val selectedVoiceName: String = "",
    val saveChatHistory: Boolean = true
)
