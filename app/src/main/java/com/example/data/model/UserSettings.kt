package com.example.data.model

data class UserSettings(
    val displayName: String = "Student",
    val dailyGoalMinutes: Int = 120,
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)
