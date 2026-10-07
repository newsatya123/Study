package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsManager
import com.example.data.local.StudyFlowDatabase
import com.example.data.model.FocusSession
import com.example.data.model.GeneratedPlanResult
import com.example.data.model.Priority
import com.example.data.model.Task
import com.example.data.model.UserSettings
import com.example.data.remote.StudyPlanGenerator
import com.example.data.remote.StudyPlanInput
import com.example.data.repository.StudyFlowRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DailyActivity(
    val dayLabel: String, // e.g. "Mon"
    val dateString: String,
    val focusMinutes: Int,
    val tasksCompleted: Int,
    val isToday: Boolean
)

data class DashboardStats(
    val greeting: String,
    val formattedDate: String,
    val todayCompletedTasks: Int,
    val todayTotalTasks: Int,
    val todayCompletionRate: Float,
    val overallCompletionRate: Float,
    val todayFocusMinutes: Int,
    val todayFocusSessions: Int,
    val totalFocusMinutes: Int,
    val totalFocusSessions: Int,
    val currentStreakDays: Int,
    val motivationalQuote: String,
    val weeklyActivity: List<DailyActivity>
)

class StudyFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StudyFlowDatabase.getInstance(application)
    private val settingsManager = SettingsManager(application)
    private val repository = StudyFlowRepository(
        taskDao = database.taskDao(),
        focusSessionDao = database.focusSessionDao(),
        settingsManager = settingsManager
    )
    private val planGenerator = StudyPlanGenerator()

    val allTasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSession>> = repository.focusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettings> = repository.settings

    // Snackbar / Feedback events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // AI Study Plan Generator UI State
    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _generatedPlan = MutableStateFlow<GeneratedPlanResult?>(null)
    val generatedPlan: StateFlow<GeneratedPlanResult?> = _generatedPlan.asStateFlow()

    private val _planGenerationError = MutableStateFlow<String?>(null)
    val planGenerationError: StateFlow<String?> = _planGenerationError.asStateFlow()

    // Computed Dashboard Statistics Flow
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allTasks,
        focusSessions,
        userSettings
    ) { tasks, sessions, settings ->
        calculateStats(tasks, sessions, settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultStats())

    private fun calculateStats(
        tasks: List<Task>,
        sessions: List<FocusSession>,
        settings: UserSettings
    ): DashboardStats {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val greeting = when (currentHour) {
            in 5..11 -> "Good morning, ${settings.displayName}"
            in 12..16 -> "Good afternoon, ${settings.displayName}"
            in 17..21 -> "Good evening, ${settings.displayName}"
            else -> "Late night focus, ${settings.displayName}"
        }

        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(now.time)

        // Today's bounds
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfToday = startOfToday + TimeUnit.DAYS.toMillis(1)

        val todayTasks = tasks.filter {
            it.dueDateMillis in startOfToday until endOfToday
        }
        val todayCompleted = todayTasks.count { it.isCompleted }
        val todayTotal = todayTasks.size
        val todayRate = if (todayTotal > 0) todayCompleted.toFloat() / todayTotal else 0f

        val totalTasks = tasks.size
        val totalCompleted = tasks.count { it.isCompleted }
        val overallRate = if (totalTasks > 0) totalCompleted.toFloat() / totalTasks else 0f

        // Focus sessions
        val todaySessions = sessions.filter { it.completedAt in startOfToday until endOfToday }
        val todayFocusMinutes = todaySessions.sumOf { it.durationMinutes }
        val todayFocusCount = todaySessions.size
        val totalFocusMinutes = sessions.sumOf { it.durationMinutes }
        val totalFocusCount = sessions.size

        // Streak Calculation
        val streak = calculateStreak(tasks, sessions)

        // Weekly Activity (Last 7 days)
        val weeklyActivity = calculateWeeklyActivity(tasks, sessions)

        // Motivational Quote
        val quote = getMotivationalQuote(todayCompleted, todayTotal, todayFocusMinutes, settings.dailyGoalMinutes)

        return DashboardStats(
            greeting = greeting,
            formattedDate = formattedDate,
            todayCompletedTasks = todayCompleted,
            todayTotalTasks = todayTotal,
            todayCompletionRate = todayRate,
            overallCompletionRate = overallRate,
            todayFocusMinutes = todayFocusMinutes,
            todayFocusSessions = todayFocusCount,
            totalFocusMinutes = totalFocusMinutes,
            totalFocusSessions = totalFocusCount,
            currentStreakDays = streak,
            motivationalQuote = quote,
            weeklyActivity = weeklyActivity
        )
    }

    private fun calculateStreak(tasks: List<Task>, sessions: List<FocusSession>): Int {
        // Collect all distinct active epoch days (days where a task was completed or focus session finished)
        val activeDays = mutableSetOf<Long>()
        val dayMillis = TimeUnit.DAYS.toMillis(1)

        fun toEpochDay(time: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis / dayMillis
        }

        tasks.forEach { task ->
            if (task.isCompleted && task.completedAt != null) {
                activeDays.add(toEpochDay(task.completedAt))
            }
        }
        sessions.forEach { session ->
            activeDays.add(toEpochDay(session.completedAt))
        }

        if (activeDays.isEmpty()) return 0

        val todayEpoch = toEpochDay(System.currentTimeMillis())
        var streak = 0

        // If today is active, streak includes today and looks back
        // If today not yet active, check if yesterday was active to preserve streak
        val startDay = if (activeDays.contains(todayEpoch)) todayEpoch else todayEpoch - 1

        var checkDay = startDay
        while (activeDays.contains(checkDay)) {
            streak++
            checkDay--
        }

        return streak
    }

    private fun calculateWeeklyActivity(tasks: List<Task>, sessions: List<FocusSession>): List<DailyActivity> {
        val list = mutableListOf<DailyActivity>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            val end = start + TimeUnit.DAYS.toMillis(1)

            val dayTasks = tasks.count {
                it.isCompleted && it.completedAt != null && it.completedAt in start until end
            }
            val dayMins = sessions.filter { it.completedAt in start until end }.sumOf { it.durationMinutes }

            list.add(
                DailyActivity(
                    dayLabel = dayFormat.format(cal.time),
                    dateString = dateFormat.format(cal.time),
                    focusMinutes = dayMins,
                    tasksCompleted = dayTasks,
                    isToday = i == 0
                )
            )
        }
        return list
    }

    private fun getMotivationalQuote(completed: Int, total: Int, focusMinutes: Int, goalMinutes: Int): String {
        return when {
            total > 0 && completed >= total -> "Outstanding! You've crushed all of today's study goals! 🚀"
            focusMinutes >= goalMinutes -> "Daily study goal achieved! Consistency is your superpower. ⚡"
            completed > 0 -> "Great momentum! Keep the flow going, one step at a time. ✨"
            total > 0 -> "Today is full of potential. Begin with one focused 25m session! 🎯"
            else -> "Ready to elevate your learning? Add your first task or generate an AI plan! 💡"
        }
    }

    private fun defaultStats() = DashboardStats(
        greeting = "Welcome back",
        formattedDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(System.currentTimeMillis()),
        todayCompletedTasks = 0,
        todayTotalTasks = 0,
        todayCompletionRate = 0f,
        overallCompletionRate = 0f,
        todayFocusMinutes = 0,
        todayFocusSessions = 0,
        totalFocusMinutes = 0,
        totalFocusSessions = 0,
        currentStreakDays = 0,
        motivationalQuote = "Plan your work, work your plan.",
        weeklyActivity = emptyList()
    )

    // Task Actions
    fun createTask(
        title: String,
        subject: String,
        description: String,
        dueDateMillis: Long,
        dueTime: String,
        priority: Priority
    ) {
        viewModelScope.launch {
            val cleanTitle = title.trim()
            if (cleanTitle.isEmpty()) {
                _userMessage.emit("Task title cannot be empty")
                return@launch
            }
            val task = Task(
                title = cleanTitle,
                subject = subject.trim().ifEmpty { "General" },
                description = description.trim(),
                dueDateMillis = dueDateMillis,
                dueTime = dueTime.trim(),
                priority = priority,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
            _userMessage.emit("Task created successfully")
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            _userMessage.emit("Task updated")
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _userMessage.emit("Task deleted")
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    // AI Study Plan Generation
    fun generateStudyPlan(
        subjects: List<String>,
        totalMinutes: Int,
        startTime: String,
        examDate: String?,
        difficulty: String,
        weakSubjects: List<String>,
        breakPreference: String
    ) {
        viewModelScope.launch {
            if (subjects.isEmpty()) {
                _planGenerationError.value = "Please add at least one subject to study."
                return@launch
            }
            _isGeneratingPlan.value = true
            _planGenerationError.value = null

            try {
                val input = StudyPlanInput(
                    subjects = subjects,
                    totalMinutes = totalMinutes.coerceIn(20, 600),
                    startTime = startTime.ifEmpty { "09:00" },
                    examDate = examDate,
                    difficulty = difficulty,
                    weakSubjects = weakSubjects,
                    breakPreference = breakPreference
                )
                val result = planGenerator.generate(input)
                _generatedPlan.value = result
            } catch (e: Exception) {
                _planGenerationError.value = "Failed to generate schedule: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isGeneratingPlan.value = false
            }
        }
    }

    fun saveGeneratedPlanAsTasks() {
        val plan = _generatedPlan.value ?: return
        viewModelScope.launch {
            val nowCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayDate = nowCal.timeInMillis

            val tasksToCreate = plan.items
                .filter { !it.isBreak }
                .map { item ->
                    Task(
                        title = "${item.subject}: ${item.topic}",
                        subject = item.subject,
                        description = "${item.activity} (${item.timeRange})",
                        dueDateMillis = todayDate,
                        dueTime = item.timeRange.split("-").firstOrNull()?.trim() ?: "",
                        priority = item.priority,
                        createdAt = System.currentTimeMillis()
                    )
                }

            if (tasksToCreate.isNotEmpty()) {
                repository.insertTasks(tasksToCreate)
                _userMessage.emit("Saved ${tasksToCreate.size} study tasks to your schedule! 📚")
            } else {
                _userMessage.emit("No study tasks to save.")
            }
        }
    }

    // Settings actions
    fun updateDisplayName(name: String) = repository.updateDisplayName(name)
    fun updateDailyGoal(minutes: Int) = repository.updateDailyGoal(minutes)
    fun updateThemeMode(mode: String) = repository.updateThemeMode(mode)
    fun updateSoundEnabled(enabled: Boolean) = repository.updateSoundEnabled(enabled)
    fun updateHapticEnabled(enabled: Boolean) = repository.updateHapticEnabled(enabled)

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _userMessage.emit("All data has been reset.")
        }
    }
}
