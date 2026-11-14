package com.example.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

data class InstalledAppInfo(
    val appName: String,
    val packageName: String
)

class AppLauncherHelper(private val context: Context) {

    private val commonAliases: Map<String, List<String>> = mapOf(
        "youtube" to listOf("youtube", "yt", "com.google.android.youtube"),
        "whatsapp" to listOf("whatsapp", "whats app", "com.whatsapp"),
        "chrome" to listOf("chrome", "browser", "com.android.chrome"),
        "camera" to listOf("camera", "cam"),
        "settings" to listOf("settings", "setting", "com.android.settings"),
        "maps" to listOf("maps", "google maps", "com.google.android.apps.maps"),
        "clock" to listOf("clock", "alarm", "deskclock"),
        "calendar" to listOf("calendar", "calender"),
        "calculator" to listOf("calculator", "calc"),
        "gmail" to listOf("gmail", "mail", "email", "com.google.android.gm"),
        "spotify" to listOf("spotify", "music", "com.spotify.music"),
        "instagram" to listOf("instagram", "insta", "com.instagram.android"),
        "phone" to listOf("phone", "dialer", "call"),
        "messages" to listOf("messages", "sms", "messaging"),
        "play store" to listOf("play store", "playstore", "com.android.vending")
    )

    fun getLaunchableApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }
        return resolveInfos.mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == context.packageName) return@mapNotNull null
            val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
            if (label.isBlank()) return@mapNotNull null
            InstalledAppInfo(appName = label, packageName = pkg)
        }.distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun findMatchingApp(queryName: String): InstalledAppInfo? {
        val normalized = queryName.trim().lowercase()
        if (normalized.isBlank()) return null

        val apps = getLaunchableApps()
        // 1. Exact label match
        apps.firstOrNull { it.appName.equals(normalized, ignoreCase = true) }?.let { return it }

        // 2. Check alias expansions
        val aliasCandidates = commonAliases.entries
            .firstOrNull { (key, aliases) ->
                normalized == key || aliases.any { normalized.contains(it) }
            }?.value.orEmpty()

        for (candidate in aliasCandidates) {
            apps.firstOrNull {
                it.packageName.equals(candidate, ignoreCase = true) ||
                    it.appName.contains(candidate, ignoreCase = true)
            }?.let { return it }
        }

        // 3. Partial label containment
        return apps.firstOrNull {
            it.appName.lowercase().contains(normalized) ||
                normalized.contains(it.appName.lowercase())
        }
    }

    fun launchApp(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }
}
