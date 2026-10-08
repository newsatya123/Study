package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.ui.theme.AmberPriorityMed
import com.example.ui.theme.CyanPriorityLow
import com.example.ui.theme.RosePriorityHigh
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditDialog(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, subject: String, description: String, dueDateMillis: Long, dueTime: String, priority: Priority) -> Unit
) {
    val initialDueDate = taskToEdit?.dueDateMillis ?: run {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var subject by remember { mutableStateOf(taskToEdit?.subject ?: "Mathematics") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var dueDateMillis by remember { mutableLongStateOf(initialDueDate) }
    var dueTime by remember { mutableStateOf(taskToEdit?.dueTime ?: "17:00") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: Priority.MEDIUM) }
    var titleError by remember { mutableStateOf(false) }

    val presetSubjects = listOf("Mathematics", "Physics", "Chemistry", "Computer Science", "Biology", "History", "Literature")

    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (taskToEdit == null) "Create New Task" else "Edit Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Chapter 4 Problem Set") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("Title is required", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject input & chips
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_subject_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Quick Subjects:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetSubjects.forEach { subj ->
                        FilterChip(
                            selected = subject.equals(subj, ignoreCase = true),
                            onClick = { subject = subj },
                            label = { Text(subj, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Key focus points, page numbers, links...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Due Date Quick Selectors
                Text(
                    text = "Due Date",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = dateFormat.format(dueDateMillis),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val todayCal = Calendar.getInstance()
                    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                    val nextWeekCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }

                    OutlinedButton(
                        onClick = { dueDateMillis = todayCal.timeInMillis },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Today", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { dueDateMillis = tomorrowCal.timeInMillis },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tomorrow", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { dueDateMillis = nextWeekCal.timeInMillis },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+7 Days", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Due Time
                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Due Time (e.g. 15:30)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { p ->
                        val isSelected = priority == p
                        val color = when (p) {
                            Priority.LOW -> CyanPriorityLow
                            Priority.MEDIUM -> AmberPriorityMed
                            Priority.HIGH -> RosePriorityHigh
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(title, subject, description, dueDateMillis, dueTime, priority)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text(if (taskToEdit == null) "Create Task" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_task_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
