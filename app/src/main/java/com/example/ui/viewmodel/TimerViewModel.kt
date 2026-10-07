package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsManager
import com.example.data.local.StudyFlowDatabase
import com.example.data.repository.StudyFlowRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PomodoroMode(val title: String, val defaultMinutes: Int) {
    FOCUS("Focus Session", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StudyFlowDatabase.getInstance(application)
    private val settingsManager = SettingsManager(application)
    private val repository = StudyFlowRepository(
        taskDao = database.taskDao(),
        focusSessionDao = database.focusSessionDao(),
        settingsManager = settingsManager
    )

    private val _currentMode = MutableStateFlow(PomodoroMode.FOCUS)
    val currentMode: StateFlow<PomodoroMode> = _currentMode.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(PomodoroMode.FOCUS.defaultMinutes * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(PomodoroMode.FOCUS.defaultMinutes * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentSubject = MutableStateFlow("Mathematics")
    val currentSubject: StateFlow<String> = _currentSubject.asStateFlow()

    private val _sessionCompletedEvent = MutableSharedFlow<String>()
    val sessionCompletedEvent: SharedFlow<String> = _sessionCompletedEvent.asSharedFlow()

    private var targetEndTimeMillis: Long = 0L
    private var tickerJob: Job? = null

    fun selectMode(mode: PomodoroMode) {
        if (_isRunning.value && !_isPaused.value) {
            // Keep running or ask user, but let's reset to selected mode cleanly
            stopTimer()
        }
        _currentMode.value = mode
        val seconds = mode.defaultMinutes * 60
        _totalDurationSeconds.value = seconds
        _remainingSeconds.value = seconds
        _isPaused.value = false
    }

    fun setSubject(subject: String) {
        _currentSubject.value = subject.trim().ifEmpty { "General Study" }
    }

    fun startTimer() {
        if (_isRunning.value && !_isPaused.value) return

        targetEndTimeMillis = SystemClock.elapsedRealtime() + (_remainingSeconds.value * 1000L)
        _isRunning.value = true
        _isPaused.value = false

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (_isRunning.value) {
                delay(200)
                val timeLeftMillis = targetEndTimeMillis - SystemClock.elapsedRealtime()
                val secondsLeft = ((timeLeftMillis + 999) / 1000).toInt().coerceAtLeast(0)
                _remainingSeconds.value = secondsLeft

                if (secondsLeft <= 0) {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        if (!_isRunning.value || _isPaused.value) return
        tickerJob?.cancel()
        _isPaused.value = true
        val timeLeftMillis = targetEndTimeMillis - SystemClock.elapsedRealtime()
        _remainingSeconds.value = ((timeLeftMillis + 999) / 1000).toInt().coerceAtLeast(0)
    }

    fun resumeTimer() {
        if (_isPaused.value) {
            startTimer()
        }
    }

    fun resetTimer() {
        tickerJob?.cancel()
        _isRunning.value = false
        _isPaused.value = false
        val seconds = _currentMode.value.defaultMinutes * 60
        _totalDurationSeconds.value = seconds
        _remainingSeconds.value = seconds
    }

    private fun stopTimer() {
        tickerJob?.cancel()
        _isRunning.value = false
        _isPaused.value = false
    }

    private fun onTimerFinished() {
        _isRunning.value = false
        _isPaused.value = false
        _remainingSeconds.value = 0

        val mode = _currentMode.value
        val minutesCompleted = mode.defaultMinutes
        val subject = _currentSubject.value

        viewModelScope.launch {
            if (mode == PomodoroMode.FOCUS) {
                repository.recordFocusSession(
                    durationMinutes = minutesCompleted,
                    sessionType = "FOCUS",
                    subject = subject
                )
                triggerVibration()
                _sessionCompletedEvent.emit("Fantastic job! Completed ${minutesCompleted}m focus on $subject 🎉")
                // Auto switch to short break
                selectMode(PomodoroMode.SHORT_BREAK)
            } else {
                triggerVibration()
                _sessionCompletedEvent.emit("Break finished! Refreshed and ready for your next focus session? ⚡")
                selectMode(PomodoroMode.FOCUS)
            }
        }
    }

    private fun triggerVibration() {
        if (!settingsManager.settings.value.hapticEnabled) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 200, 100, 300), -1)
            }
        } catch (_: Exception) {
            // Ignore if vibration not permitted or unavailable
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
