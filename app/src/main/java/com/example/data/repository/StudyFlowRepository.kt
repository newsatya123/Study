package com.example.data.repository

import com.example.data.local.FocusSessionDao
import com.example.data.local.SettingsManager
import com.example.data.local.TaskDao
import com.example.data.model.FocusSession
import com.example.data.model.Task
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class StudyFlowRepository(
    private val taskDao: TaskDao,
    private val focusSessionDao: FocusSessionDao,
    private val settingsManager: SettingsManager
) {
    val tasks: Flow<List<Task>> = taskDao.getAllTasks()
    val focusSessions: Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    val settings: StateFlow<UserSettings> = settingsManager.settings

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<Task>): List<Long> = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleTaskCompletion(task: Task) {
        val newStatus = !task.isCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        taskDao.updateTask(task.copy(isCompleted = newStatus, completedAt = completedAt))
    }

    suspend fun recordFocusSession(
        durationMinutes: Int,
        sessionType: String = "FOCUS",
        subject: String = ""
    ): Long {
        return focusSessionDao.insertSession(
            FocusSession(
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                completedAt = System.currentTimeMillis(),
                subject = subject
            )
        )
    }

    fun updateDisplayName(name: String) = settingsManager.updateDisplayName(name)

    fun updateDailyGoal(minutes: Int) = settingsManager.updateDailyGoal(minutes)

    fun updateThemeMode(mode: String) = settingsManager.updateThemeMode(mode)

    fun updateSoundEnabled(enabled: Boolean) = settingsManager.updateSoundEnabled(enabled)

    fun updateHapticEnabled(enabled: Boolean) = settingsManager.updateHapticEnabled(enabled)

    suspend fun clearAllData() {
        taskDao.clearAllTasks()
        focusSessionDao.clearAllSessions()
        settingsManager.reset()
    }
}
