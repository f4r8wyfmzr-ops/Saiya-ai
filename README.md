# SIYA AI — Android Voice Assistant

A native Kotlin + Jetpack Compose Android AI voice assistant featuring a futuristic purple-and-black interface, animated holographic avatar, glowing microphone button, Hindi / Hinglish / English voice & text chat, and privacy-first device actions.

## Features

- **Personalized Persona**: Addresses the owner as **Vishal Boss** (customizable and stored locally via Jetpack DataStore).
- **Multilingual Voice & Text Chat**: Supports **Hinglish**, **Hindi**, and **English** with quick language switching.
- **Speech-to-Text (`SpeechRecognizer`)**: On-demand voice recognition with live partial transcript and a clear **Microphone Active** HUD indicator.
- **Text-to-Speech (`TextToSpeech`)**: Automatically selects a natural female voice where available, with adjustable speech rate, pitch, and voice picker in Settings.
- **On-Device System Actions (Confirmation-Gated)**:
  - Tell current **date, time, and live battery level** (`BatteryManager`).
  - **Open installed apps** via Android launcher intents (`PackageManager` + `<queries>`).
  - **Create alarms & timers** (`AlarmClock.ACTION_SET_ALARM`, `AlarmClock.ACTION_SET_TIMER`).
  - **Create reminders** saved locally in Room and opened in Calendar (`CalendarContract.Events`).
- **Privacy-First Design**:
  - Microphone permission (`RECORD_AUDIO`) is requested **only when the user taps the mic** (never at startup).
  - Explicit **Confirm / Cancel** dialog before opening external apps, alarms, timers, or reminders.
  - Zero hidden background recording; zero silent calls, messages, file deletions, or system changes.
- **AI Engine Architecture**:
  - Conversations route through `AiRepository` (`com.example.data.ai.AiRepository`).
  - Currently backed by `SampleAiRepository` (since Firebase AI Logic setup was not enabled), which handles all local commands live and returns clearly labeled `[Sample Response]` replies for open-ended queries.
  - Can be upgraded to live Gemini responses by enabling Firebase and implementing a `FirebaseAiRepository` behind `AiRepository`.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3 + Navigation Compose
- **Fonts**: Local bundled `Space Grotesk` & `Plus Jakarta Sans` (`res/font/`)
- **Local Persistence**: Jetpack DataStore Preferences + Room Database (KSP)
- **Speech**: Android `SpeechRecognizer` & `TextToSpeech`
- **Testing**: JUnit 4 + Robolectric + Compose UI Test (`@Config(sdk = [36])`)

## Build & Installation Instructions

### Prerequisites
- Android Studio (Meerkat / Narwhal or newer) or Android SDK with **SDK 36** installed (`minSdk = 24`, `targetSdk = 36`)
- JDK 11 or JDK 17

### Build Debug APK
```bash
gradle :app:assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Unit & Robolectric Screen Tests
```bash
gradle :app:testDebugUnitTest
```

### Install on an Android Device / Emulator
1. Enable **USB Debugging** on your Android phone (or start an Android Emulator).
2. Install the debug APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Launch **SIYA AI** from your app drawer.

### Optional: Connecting Live Gemini via Firebase AI Logic
If you want live cloud Gemini responses instead of Sample AI Mode:
1. Enable Firebase AI Logic for the project (`google-services.json` + Firebase BoM + `firebase-ai` + App Check).
2. Add a `FirebaseAiRepository` implementing `AiRepository` using `Firebase.ai.generativeModel("gemini-3.8-flash")` with `configureAppCheck(this, intent)` in `MainActivity.onCreate()`.
