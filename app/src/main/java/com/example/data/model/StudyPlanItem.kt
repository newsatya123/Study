package com.example.data.model

import java.util.UUID

data class StudyPlanItem(
    val id: String = UUID.randomUUID().toString(),
    val timeRange: String,
    val subject: String,
    val topic: String,
    val activity: String,
    val durationMinutes: Int,
    val isBreak: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val isSavedAsTask: Boolean = false
)

data class GeneratedPlanResult(
    val title: String,
    val isAiGenerated: Boolean,
    val summary: String,
    val items: List<StudyPlanItem>
)
