package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.GeneratedPlanResult
import com.example.data.model.Priority
import com.example.data.model.StudyPlanItem
import com.example.ui.theme.AmberPriorityMed
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RosePriorityHigh
import com.example.ui.theme.VioletPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiPlanScreen(
    isGenerating: Boolean,
    generatedPlan: GeneratedPlanResult?,
    errorMessage: String?,
    onGenerate: (subjects: List<String>, totalMinutes: Int, startTime: String, examDate: String?, difficulty: String, weakSubjects: List<String>, breakPref: String) -> Unit,
    onSavePlanAsTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Form States
    val subjects = remember { mutableStateListOf("Calculus", "Physics", "Computer Science") }
    var newSubjectInput by remember { mutableStateOf("") }
    var availableMinutes by remember { mutableFloatStateOf(150f) } // 2.5 hours
    var startTime by remember { mutableStateOf("09:00") }
    var examDate by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Moderate") }
    val weakSubjects = remember { mutableStateListOf<String>("Calculus") }
    var breakPreference by remember { mutableStateOf("Pomodoro (25/5)") }

    val presetSuggested = listOf("Biology", "Chemistry", "World History", "Literature", "Psychology", "Economics")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Study Planner",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Generate optimal study blocks powered by AI intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Subjects Section
                    Text(
                        text = "Target Subjects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Selected Subject Badges
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        subjects.forEach { sub ->
                            val isWeak = weakSubjects.contains(sub)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isWeak) RosePriorityHigh.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                                border = if (isWeak) androidx.compose.foundation.BorderStroke(1.dp, RosePriorityHigh) else null,
                                modifier = Modifier.clickable {
                                    // Toggle weak subject priority
                                    if (weakSubjects.contains(sub)) {
                                        weakSubjects.remove(sub)
                                    } else {
                                        weakSubjects.add(sub)
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isWeak) "⚡ $sub (Priority)" else sub,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isWeak) RosePriorityHigh else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                subjects.remove(sub)
                                                weakSubjects.remove(sub)
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tip: Tap a subject chip to mark as a Weak / High Priority target.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add Subject Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubjectInput,
                            onValueChange = { newSubjectInput = it },
                            placeholder = { Text("Add another subject...") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_add_subject_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newSubjectInput.isNotBlank() && !subjects.contains(newSubjectInput.trim())) {
                                    subjects.add(newSubjectInput.trim())
                                    newSubjectInput = ""
                                }
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Suggested Subjects
                    Text(
                        text = "Quick Suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetSuggested.forEach { sug ->
                            if (!subjects.contains(sug)) {
                                FilterChip(
                                    selected = false,
                                    onClick = { subjects.add(sug) },
                                    label = { Text("+ $sug", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Available Study Time Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Study Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val hours = (availableMinutes.toInt() / 60)
                        val mins = (availableMinutes.toInt() % 60)
                        Text(
                            text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = availableMinutes,
                        onValueChange = { availableMinutes = it },
                        valueRange = 30f..360f,
                        steps = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_time_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Start Time & Optional Exam Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            placeholder = { Text("09:00") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = examDate,
                            onValueChange = { examDate = it },
                            label = { Text("Exam Date (Opt)") },
                            placeholder = { Text("e.g. Next Friday") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Difficulty selector
                    Text(
                        text = "Session Intensity",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Light", "Moderate", "Intensive").forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff },
                                label = { Text(diff, modifier = Modifier.padding(horizontal = 4.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Break preference
                    Text(
                        text = "Break Schedule Strategy",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Pomodoro (25/5)", "Standard (50/10)", "Deep (90/15)").forEach { pref ->
                            FilterChip(
                                selected = breakPreference.startsWith(pref.substring(0, 4)),
                                onClick = { breakPreference = pref },
                                label = { Text(pref, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Generate Button
                    Button(
                        onClick = {
                            onGenerate(
                                subjects.toList(),
                                availableMinutes.toInt(),
                                startTime,
                                examDate.ifBlank { null },
                                difficulty,
                                weakSubjects.toList(),
                                breakPreference
                            )
                        },
                        enabled = !isGenerating && subjects.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_plan_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Synthesizing Schedule...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Study Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Error Banner
        errorMessage?.let { err ->
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RosePriorityHigh.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RosePriorityHigh.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = RosePriorityHigh)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = RosePriorityHigh
                        )
                    }
                }
            }
        }

        // Generated Schedule Output Section
        generatedPlan?.let { plan ->
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Mode Badge: AI vs Local
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (plan.isAiGenerated) ElectricCyan.copy(alpha = 0.2f) else VioletPrimary.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (plan.isAiGenerated) Icons.Default.AutoAwesome else Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = if (plan.isAiGenerated) ElectricCyan else VioletPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (plan.isAiGenerated) "Gemini AI" else "Smart Rule Engine",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (plan.isAiGenerated) ElectricCyan else VioletPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Actions: Save as Tasks & Regenerate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSavePlanAsTasks,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_plan_tasks_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Plan as Tasks", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                onGenerate(
                                    subjects.toList(),
                                    availableMinutes.toInt(),
                                    startTime,
                                    examDate.ifBlank { null },
                                    difficulty,
                                    weakSubjects.toList(),
                                    breakPreference
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("regenerate_plan_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Regen", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Schedule Items list
            items(plan.items, key = { it.id }) { item ->
                StudyScheduleItemCard(item = item)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StudyScheduleItemCard(
    item: StudyPlanItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBreak) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (item.isBreak) VioletPrimary.copy(alpha = 0.15f) else ElectricCyan.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isBreak) Icons.Default.Coffee else Icons.Default.School,
                    contentDescription = null,
                    tint = if (item.isBreak) VioletPrimary else ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.timeRange,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.topic,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.activity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
