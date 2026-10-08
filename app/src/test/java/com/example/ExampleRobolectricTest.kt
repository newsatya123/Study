package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.StudyPlanGenerator
import com.example.data.remote.StudyPlanInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StudyFlow AI", appName)
    }

    @Test
    fun `test local study plan generator produces valid timetable`() {
        val generator = StudyPlanGenerator()
        val input = StudyPlanInput(
            subjects = listOf("Mathematics", "Physics"),
            totalMinutes = 120,
            startTime = "10:00",
            difficulty = "Moderate",
            weakSubjects = listOf("Mathematics"),
            breakPreference = "Pomodoro (25/5)"
        )
        val result = generator.generateLocalRuleBasedPlan(input)
        assertTrue(result.items.isNotEmpty())
        assertTrue(result.items.any { it.subject == "Mathematics" })
        assertTrue(result.items.any { it.isBreak })
    }
}
