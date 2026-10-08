package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.GeneratedPlanResult
import com.example.data.model.Priority
import com.example.data.model.StudyPlanItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class StudyPlanInput(
    val subjects: List<String>,
    val totalMinutes: Int,
    val startTime: String, // e.g. "09:00"
    val examDate: String? = null,
    val difficulty: String = "Moderate",
    val weakSubjects: List<String> = emptyList(),
    val breakPreference: String = "Pomodoro (25/5)"
)

class StudyPlanGenerator {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generate(input: StudyPlanInput): GeneratedPlanResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val isKeyConfigured = apiKey.isNotBlank() &&
                !apiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true) &&
                !apiKey.contains("PLACEHOLDER", ignoreCase = true)

        if (isKeyConfigured) {
            try {
                val aiResult = callGeminiApi(apiKey, input)
                if (aiResult != null && aiResult.items.isNotEmpty()) {
                    return@withContext aiResult
                }
            } catch (e: Exception) {
                // Graceful fallback to rule-based schedule
                e.printStackTrace()
            }
        }

        // Offline / Fallback rule-based schedule engine
        generateLocalRuleBasedPlan(input)
    }

    private fun callGeminiApi(apiKey: String, input: StudyPlanInput): GeneratedPlanResult? {
        val prompt = buildString {
            append("You are an expert academic tutor. Create a realistic, highly effective study schedule for a student.\n")
            append("Subjects: ${input.subjects.joinToString(", ")}\n")
            append("Total Available Time: ${input.totalMinutes} minutes\n")
            append("Start Time: ${input.startTime}\n")
            if (!input.examDate.isNullOrBlank()) append("Upcoming Exam Date: ${input.examDate}\n")
            append("Difficulty/Intensity: ${input.difficulty}\n")
            if (input.weakSubjects.isNotEmpty()) append("Weak / High Priority Subjects: ${input.weakSubjects.joinToString(", ")}\n")
            append("Break style: ${input.breakPreference}\n\n")
            append("Return ONLY a JSON array with NO markdown ticks or formatting. Format:\n")
            append("""
                [
                  {
                    "timeRange": "09:00 - 09:25",
                    "subject": "Physics",
                    "topic": "Newtonian Dynamics",
                    "activity": "Solve kinematics problem set with active recall",
                    "durationMinutes": 25,
                    "isBreak": false,
                    "priority": "HIGH"
                  }
                ]
            """.trimIndent())
        }

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", contentsArray)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null
        val text = parts.getJSONObject(0).optString("text")

        val cleanJson = cleanJsonString(text)
        val jsonArray = JSONArray(cleanJson)

        val items = mutableListOf<StudyPlanItem>()
        for (i in 0 until jsonArray.length()) {
            val itemObj = jsonArray.getJSONObject(i)
            items.add(
                StudyPlanItem(
                    timeRange = itemObj.optString("timeRange", "Scheduled"),
                    subject = itemObj.optString("subject", "Study"),
                    topic = itemObj.optString("topic", "Focus Area"),
                    activity = itemObj.optString("activity", "Review material"),
                    durationMinutes = itemObj.optInt("durationMinutes", 25),
                    isBreak = itemObj.optBoolean("isBreak", false),
                    priority = Priority.fromString(itemObj.optString("priority", "MEDIUM"))
                )
            )
        }

        return GeneratedPlanResult(
            title = "Personalized AI Study Schedule",
            isAiGenerated = true,
            summary = "Optimized by Gemini 3.5 Flash for cognitive endurance and subject mastery.",
            items = items
        )
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    fun generateLocalRuleBasedPlan(input: StudyPlanInput): GeneratedPlanResult {
        val items = mutableListOf<StudyPlanItem>()
        val startParts = input.startTime.split(":")
        val startHour = startParts.getOrNull(0)?.toIntOrNull() ?: 9
        val startMin = startParts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMin)
            set(Calendar.SECOND, 0)
        }

        val (studyBlockDuration, breakDuration) = when {
            input.breakPreference.contains("50", ignoreCase = true) -> Pair(50, 10)
            input.breakPreference.contains("90", ignoreCase = true) -> Pair(90, 15)
            else -> Pair(25, 5) // Pomodoro default
        }

        var remainingMinutes = input.totalMinutes
        val activeSubjects = if (input.subjects.isEmpty()) listOf("Core Focus") else input.subjects

        // Prioritize weak subjects by repeating them or scheduling first
        val subjectQueue = mutableListOf<String>()
        input.weakSubjects.forEach { weak ->
            if (activeSubjects.contains(weak)) {
                subjectQueue.add(weak)
                subjectQueue.add(weak) // Double weight for weak subjects
            }
        }
        activeSubjects.forEach { sub ->
            if (!subjectQueue.contains(sub)) {
                subjectQueue.add(sub)
            }
        }

        val sampleActivities = listOf(
            "Active recall & key concept flashcards",
            "Solve targeted practice problem sets",
            "Formula derivation & summary mind map",
            "Past exam question review & error log",
            "Deep reading and chapter synthesis",
            "Spaced repetition quiz & self-test"
        )

        var subjectIndex = 0
        var blockCount = 0

        while (remainingMinutes > 10) {
            val studyDuration = studyBlockDuration.coerceAtMost(remainingMinutes)
            val startTimeStr = formatTime(calendar)
            calendar.add(Calendar.MINUTE, studyDuration)
            val endTimeStr = formatTime(calendar)
            remainingMinutes -= studyDuration
            blockCount++

            val currentSubject = subjectQueue[subjectIndex % subjectQueue.size]
            val isWeak = input.weakSubjects.contains(currentSubject)
            val priority = if (isWeak || !input.examDate.isNullOrBlank()) Priority.HIGH else Priority.MEDIUM
            val activity = sampleActivities[(blockCount - 1) % sampleActivities.size]

            items.add(
                StudyPlanItem(
                    timeRange = "$startTimeStr - $endTimeStr",
                    subject = currentSubject,
                    topic = if (isWeak) "Priority Target: Core Mastery" else "Chapter Review & Practice",
                    activity = activity,
                    durationMinutes = studyDuration,
                    isBreak = false,
                    priority = priority
                )
            )

            subjectIndex++

            // Add break if there's enough time left
            if (remainingMinutes > breakDuration) {
                val breakStart = formatTime(calendar)
                calendar.add(Calendar.MINUTE, breakDuration)
                val breakEnd = formatTime(calendar)
                remainingMinutes -= breakDuration

                items.add(
                    StudyPlanItem(
                        timeRange = "$breakStart - $breakEnd",
                        subject = "Rest & Recharge",
                        topic = "Cognitive Reset",
                        activity = "Hydrate, step away from screens, light stretch",
                        durationMinutes = breakDuration,
                        isBreak = true,
                        priority = Priority.LOW
                    )
                )
            }
        }

        return GeneratedPlanResult(
            title = "Structured Flow Schedule",
            isAiGenerated = false,
            summary = "Smart local plan using spaced cognitive intervals & priority weighting for ${input.subjects.size} subjects.",
            items = items
        )
    }

    private fun formatTime(calendar: Calendar): String {
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }
}
