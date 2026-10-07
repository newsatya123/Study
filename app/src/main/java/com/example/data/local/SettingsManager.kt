package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("studyflow_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            displayName = prefs.getString("display_name", "Student") ?: "Student",
            dailyGoalMinutes = prefs.getInt("daily_goal_minutes", 120),
            themeMode = prefs.getString("theme_mode", "DARK") ?: "DARK",
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            hapticEnabled = prefs.getBoolean("haptic_enabled", true)
        )
    }

    fun updateDisplayName(name: String) {
        val clean = name.trim().ifEmpty { "Student" }
        prefs.edit().putString("display_name", clean).apply()
        _settings.value = _settings.value.copy(displayName = clean)
    }

    fun updateDailyGoal(minutes: Int) {
        val valid = minutes.coerceIn(15, 720)
        prefs.edit().putInt("daily_goal_minutes", valid).apply()
        _settings.value = _settings.value.copy(dailyGoalMinutes = valid)
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun updateSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun updateHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
        _settings.value = _settings.value.copy(hapticEnabled = enabled)
    }

    fun reset() {
        prefs.edit().clear().apply()
        _settings.value = loadSettings()
    }
}
