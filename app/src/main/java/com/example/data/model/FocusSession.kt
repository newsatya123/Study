package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,
    val sessionType: String = "FOCUS",
    val completedAt: Long = System.currentTimeMillis(),
    val subject: String = ""
)
