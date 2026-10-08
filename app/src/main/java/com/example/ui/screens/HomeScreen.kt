package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.Task
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.StatCard
import com.example.ui.components.TaskEditDialog
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.AmberPriorityMed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.DashboardStats
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    stats: DashboardStats,
    tasks: List<Task>,
    onToggleTask: (Task) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onCreateTask: (String, String, String, Long, String, Priority) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToAiPlan: () -> Unit,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // Filter today's tasks
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val endOfToday = startOfToday + TimeUnit.DAYS.toMillis(1)

    val todayTasks = tasks.filter { it.dueDateMillis in startOfToday until endOfToday }
    val upcomingTasks = tasks.filter { !it.isCompleted }.take(4)

    val animatedProgress by animateFloatAsState(
        targetValue = stats.todayCompletionRate,
        animationSpec = tween(durationMillis = 800),
        label = "progress_animation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header: Greeting, Date, Streak Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stats.greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stats.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberPriorityMed.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberPriorityMed.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("streak_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = AmberPriorityMed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats.currentStreakDays} Day Streak",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberPriorityMed
                        )
                    }
                }
            }
        }

        // Hero Study Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_progress_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Progress
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(80.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            strokeWidth = 8.dp,
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(80.dp),
                            color = ElectricCyan,
                            strokeWidth = 8.dp,
                        )
                        Text(
                            text = "${(stats.todayCompletionRate * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Study Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.todayCompletedTasks} of ${stats.todayTotalTasks} tasks completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stats.motivationalQuote,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Focus Time",
                    value = "${stats.todayFocusMinutes}m",
                    subtitle = "${stats.todayFocusSessions} sessions today",
                    icon = Icons.Default.Timer,
                    iconTint = ElectricCyan,
                    iconBgColor = ElectricCyan.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Tasks Left",
                    value = "${(stats.todayTotalTasks - stats.todayCompletedTasks).coerceAtLeast(0)}",
                    subtitle = "${tasks.size} total active",
                    icon = Icons.Default.CheckCircle,
                    iconTint = VioletPrimary,
                    iconBgColor = VioletPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_task_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Task", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onNavigateToAiPlan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_ai_plan_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Plan", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onNavigateToFocus,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_focus_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Focus", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Section Title: Today's Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Study Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (tasks.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onNavigateToTasks,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View All (${tasks.size})", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Empty state or Task list
        if (upcomingTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No study tasks pending!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ Add Task' or generate a personalized AI schedule to start your study flow.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddTaskDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create Your First Task")
                        }
                    }
                }
            }
        } else {
            items(upcomingTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onToggleCompletion = onToggleTask,
                    onEdit = { taskToEdit = it },
                    onDelete = { taskToDelete = it }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        TaskEditDialog(
            taskToEdit = null,
            onDismiss = { showAddTaskDialog = false },
            onSave = { title, subject, desc, dueMillis, dueTime, priority ->
                onCreateTask(title, subject, desc, dueMillis, dueTime, priority)
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { task ->
        TaskEditDialog(
            taskToEdit = task,
            onDismiss = { taskToEdit = null },
            onSave = { title, subject, desc, dueMillis, dueTime, priority ->
                onUpdateTask(
                    task.copy(
                        title = title,
                        subject = subject,
                        description = desc,
                        dueDateMillis = dueMillis,
                        dueTime = dueTime,
                        priority = priority
                    )
                )
            }
        )
    }

    // Delete Task Confirmation
    taskToDelete?.let { task ->
        ConfirmationDialog(
            title = "Delete Task",
            message = "Are you sure you want to delete '${task.title}'? This action cannot be undone.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = { onDeleteTask(task) },
            onDismiss = { taskToDelete = null }
        )
    }
}
