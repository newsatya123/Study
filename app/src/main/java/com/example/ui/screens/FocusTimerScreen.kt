package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusSession
import com.example.ui.theme.AmberPriorityMed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.PomodoroMode
import kotlinx.coroutines.flow.SharedFlow
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusTimerScreen(
    currentMode: PomodoroMode,
    totalDurationSeconds: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    currentSubject: String,
    todayFocusSessions: List<FocusSession>,
    sessionCompletedEvent: SharedFlow<String>,
    onSelectMode: (PomodoroMode) -> Unit,
    onSetSubject: (String) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var completionCelebrationMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionCompletedEvent) {
        sessionCompletedEvent.collect { message ->
            completionCelebrationMessage = message
        }
    }

    val progress = if (totalDurationSeconds > 0) {
        (totalDurationSeconds - remainingSeconds).toFloat() / totalDurationSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "timer_ring"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Computer Science", "Biology", "Literature", "General")

    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val endOfToday = startOfToday + TimeUnit.DAYS.toMillis(1)

    val todaySessions = todayFocusSessions.filter { it.completedAt in startOfToday until endOfToday }
    val todayMinutes = todaySessions.sumOf { it.durationMinutes }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header
        Text(
            text = "Pomodoro Focus Flow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Deep work with spaced intervals to maximize cognitive retention",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selector (Focus, Short Break, Long Break)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PomodoroMode.entries.forEach { mode ->
                val isSelected = currentMode == mode
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectMode(mode) }
                        .testTag("timer_mode_${mode.name}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                PomodoroMode.FOCUS -> Icons.Default.Timer
                                PomodoroMode.SHORT_BREAK -> Icons.Default.Coffee
                                PomodoroMode.LONG_BREAK -> Icons.Default.SelfImprovement
                            },
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${mode.defaultMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject selector chip row
        Text(
            text = "Focusing on Subject:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            subjects.forEach { subj ->
                FilterChip(
                    selected = currentSubject.equals(subj, ignoreCase = true),
                    onClick = { onSetSubject(subj) },
                    label = { Text(subj, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Big Circular Countdown Clock
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .testTag("circular_timer_container")
        ) {
            // Background track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(260.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                strokeWidth = 14.dp
            )

            // Progress stroke
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(260.dp),
                color = when (currentMode) {
                    PomodoroMode.FOCUS -> ElectricCyan
                    PomodoroMode.SHORT_BREAK -> VioletPrimary
                    PomodoroMode.LONG_BREAK -> EmeraldSuccess
                },
                strokeWidth = 14.dp
            )

            // Inner Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isRunning && !isPaused -> ElectricCyan.copy(alpha = 0.15f)
                        isPaused -> AmberPriorityMed.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            isRunning && !isPaused -> "IN PROGRESS"
                            isPaused -> "PAUSED"
                            else -> "READY"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isRunning && !isPaused -> ElectricCyan
                            isPaused -> AmberPriorityMed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Digits
                Text(
                    text = formattedTime,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("timer_countdown_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentSubject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer Controls Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRunning) {
                // Big Start Button
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .width(180.dp)
                        .testTag("timer_start_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Focus", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else if (isPaused) {
                // Resume Button
                Button(
                    onClick = onResume,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess
                    ),
                    modifier = Modifier
                        .height(54.dp)
                        .width(140.dp)
                        .testTag("timer_resume_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resume", fontWeight = FontWeight.Bold)
                }

                // Reset Button
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("timer_reset_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }
            } else {
                // Pause Button
                Button(
                    onClick = onPause,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .height(54.dp)
                        .width(140.dp)
                        .testTag("timer_pause_button")
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pause", fontWeight = FontWeight.Bold)
                }

                // Reset Button
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("timer_reset_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Today's Focus Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${todaySessions.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Text(
                        text = "Sessions Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${todayMinutes}m",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = VioletPrimary
                    )
                    Text(
                        text = "Focus Time Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Completion celebration banner
        completionCelebrationMessage?.let { celebration ->
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EmeraldSuccess.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = celebration, style = MaterialTheme.typography.bodyMedium, color = EmeraldSuccess, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
