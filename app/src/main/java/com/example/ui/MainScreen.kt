package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiPlanScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.StudyFlowTheme
import com.example.ui.viewmodel.StudyFlowViewModel
import com.example.ui.viewmodel.TimerViewModel

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    TASKS("Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    AI_PLAN("AI Plan", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    FOCUS("Focus", Icons.Filled.Timer, Icons.Outlined.Timer),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainScreen(
    studyFlowViewModel: StudyFlowViewModel = viewModel(),
    timerViewModel: TimerViewModel = viewModel()
) {
    val settings by studyFlowViewModel.userSettings.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemDark
    }

    StudyFlowTheme(darkTheme = isDark) {
        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        val snackbarHostState = remember { SnackbarHostState() }

        // Observe ViewModel States
        val stats by studyFlowViewModel.dashboardStats.collectAsStateWithLifecycle()
        val allTasks by studyFlowViewModel.allTasks.collectAsStateWithLifecycle()
        val focusSessions by studyFlowViewModel.focusSessions.collectAsStateWithLifecycle()
        val isGeneratingPlan by studyFlowViewModel.isGeneratingPlan.collectAsStateWithLifecycle()
        val generatedPlan by studyFlowViewModel.generatedPlan.collectAsStateWithLifecycle()
        val planError by studyFlowViewModel.planGenerationError.collectAsStateWithLifecycle()

        // Timer States
        val timerMode by timerViewModel.currentMode.collectAsStateWithLifecycle()
        val timerTotalSeconds by timerViewModel.totalDurationSeconds.collectAsStateWithLifecycle()
        val timerRemainingSeconds by timerViewModel.remainingSeconds.collectAsStateWithLifecycle()
        val timerIsRunning by timerViewModel.isRunning.collectAsStateWithLifecycle()
        val timerIsPaused by timerViewModel.isPaused.collectAsStateWithLifecycle()
        val timerSubject by timerViewModel.currentSubject.collectAsStateWithLifecycle()

        // Request Notification Permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { _ -> }
            LaunchedEffect(Unit) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Listen for snackbar notifications
        LaunchedEffect(studyFlowViewModel.userMessage) {
            studyFlowViewModel.userMessage.collect { msg ->
                snackbarHostState.showSnackbar(msg)
            }
        }

        // BackHandler: Return to Home if on another screen
        BackHandler(enabled = currentScreen != AppScreen.HOME) {
            currentScreen = AppScreen.HOME
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    AppScreen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.name}")
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.HOME -> HomeScreen(
                        stats = stats,
                        tasks = allTasks,
                        onToggleTask = { studyFlowViewModel.toggleTask(it) },
                        onUpdateTask = { studyFlowViewModel.updateTask(it) },
                        onDeleteTask = { studyFlowViewModel.deleteTask(it) },
                        onCreateTask = { title, sub, desc, due, time, priority ->
                            studyFlowViewModel.createTask(title, sub, desc, due, time, priority)
                        },
                        onNavigateToTasks = { currentScreen = AppScreen.TASKS },
                        onNavigateToAiPlan = { currentScreen = AppScreen.AI_PLAN },
                        onNavigateToFocus = { currentScreen = AppScreen.FOCUS }
                    )

                    AppScreen.TASKS -> TasksScreen(
                        tasks = allTasks,
                        onToggleTask = { studyFlowViewModel.toggleTask(it) },
                        onUpdateTask = { studyFlowViewModel.updateTask(it) },
                        onDeleteTask = { studyFlowViewModel.deleteTask(it) },
                        onCreateTask = { title, sub, desc, due, time, priority ->
                            studyFlowViewModel.createTask(title, sub, desc, due, time, priority)
                        }
                    )

                    AppScreen.AI_PLAN -> AiPlanScreen(
                        isGenerating = isGeneratingPlan,
                        generatedPlan = generatedPlan,
                        errorMessage = planError,
                        onGenerate = { subjects, totalMin, start, exam, diff, weak, breakPref ->
                            studyFlowViewModel.generateStudyPlan(subjects, totalMin, start, exam, diff, weak, breakPref)
                        },
                        onSavePlanAsTasks = { studyFlowViewModel.saveGeneratedPlanAsTasks() }
                    )

                    AppScreen.FOCUS -> FocusTimerScreen(
                        currentMode = timerMode,
                        totalDurationSeconds = timerTotalSeconds,
                        remainingSeconds = timerRemainingSeconds,
                        isRunning = timerIsRunning,
                        isPaused = timerIsPaused,
                        currentSubject = timerSubject,
                        todayFocusSessions = focusSessions,
                        sessionCompletedEvent = timerViewModel.sessionCompletedEvent,
                        onSelectMode = { timerViewModel.selectMode(it) },
                        onSetSubject = { timerViewModel.setSubject(it) },
                        onStart = { timerViewModel.startTimer() },
                        onPause = { timerViewModel.pauseTimer() },
                        onResume = { timerViewModel.resumeTimer() },
                        onReset = { timerViewModel.resetTimer() }
                    )

                    AppScreen.PROFILE -> ProfileScreen(
                        settings = settings,
                        stats = stats,
                        onUpdateDisplayName = { studyFlowViewModel.updateDisplayName(it) },
                        onUpdateDailyGoal = { studyFlowViewModel.updateDailyGoal(it) },
                        onUpdateThemeMode = { studyFlowViewModel.updateThemeMode(it) },
                        onUpdateSoundEnabled = { studyFlowViewModel.updateSoundEnabled(it) },
                        onUpdateHapticEnabled = { studyFlowViewModel.updateHapticEnabled(it) },
                        onResetAllData = { studyFlowViewModel.resetAllData() }
                    )
                }
            }
        }
    }
}
