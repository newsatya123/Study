package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Priority
import com.example.data.model.Task
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.TaskEditDialog
import com.example.ui.components.TaskItemCard
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class TaskFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed")
}

enum class TaskSort(val label: String) {
    DUE_DATE("Due Date"),
    PRIORITY("Priority"),
    TITLE("Title")
}

@Composable
fun TasksScreen(
    tasks: List<Task>,
    onToggleTask: (Task) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onCreateTask: (String, String, String, Long, String, Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var selectedSort by remember { mutableStateOf(TaskSort.DUE_DATE) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val endOfToday = startOfToday + TimeUnit.DAYS.toMillis(1)

    // Filter Logic
    val filteredTasks = tasks.filter { task ->
        val matchesSearch = searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.subject.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.TODAY -> task.dueDateMillis in startOfToday until endOfToday
            TaskFilter.UPCOMING -> !task.isCompleted && task.dueDateMillis >= startOfToday
            TaskFilter.COMPLETED -> task.isCompleted
        }

        matchesSearch && matchesTab
    }.sortedWith { a, b ->
        when (selectedSort) {
            TaskSort.DUE_DATE -> a.dueDateMillis.compareTo(b.dueDateMillis)
            TaskSort.PRIORITY -> {
                // High (2) > Med (1) > Low (0)
                val orderA = when (a.priority) { Priority.HIGH -> 3; Priority.MEDIUM -> 2; Priority.LOW -> 1 }
                val orderB = when (b.priority) { Priority.HIGH -> 3; Priority.MEDIUM -> 2; Priority.LOW -> 1 }
                orderB.compareTo(orderA)
            }
            TaskSort.TITLE -> a.title.compareTo(b.title, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header title & counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Study Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${tasks.count { it.isCompleted }} of ${tasks.size} completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sort Dropdown
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("sort_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort tasks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        TaskSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Sort by ${sort.label}",
                                        fontWeight = if (selectedSort == sort) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedSort = sort
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by task title or subject...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_bar")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TaskFilter.entries) { filter ->
                    val count = when (filter) {
                        TaskFilter.ALL -> tasks.size
                        TaskFilter.TODAY -> tasks.count { it.dueDateMillis in startOfToday until endOfToday }
                        TaskFilter.UPCOMING -> tasks.count { !it.isCompleted && it.dueDateMillis >= startOfToday }
                        TaskFilter.COMPLETED -> tasks.count { it.isCompleted }
                    }

                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text("${filter.label} ($count)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_chip_${filter.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (selectedFilter == TaskFilter.COMPLETED) Icons.Default.CheckCircle else Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (selectedFilter) {
                                    TaskFilter.COMPLETED -> "No completed tasks yet"
                                    TaskFilter.TODAY -> "No tasks due today"
                                    TaskFilter.UPCOMING -> "No upcoming tasks pending"
                                    TaskFilter.ALL -> if (searchQuery.isNotEmpty()) "No tasks match your search" else "No study tasks created yet"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stay ahead of your coursework by adding your study milestones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleCompletion = onToggleTask,
                            onEdit = { taskToEdit = it },
                            onDelete = { taskToDelete = it }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
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
            message = "Are you sure you want to delete '${task.title}'? This cannot be undone.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = { onDeleteTask(task) },
            onDismiss = { taskToDelete = null }
        )
    }
}
