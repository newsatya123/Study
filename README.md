# StudyFlow AI - Smart Student Productivity & Focus

StudyFlow AI is a modern native Android application built with Jetpack Compose, Kotlin, Material 3, and Room. It helps students master their academic schedule with AI-driven personalized timetable generation, full CRUD task tracking, genuine activity streaks, and a Pomodoro focus timer.

## 🌟 Key Features

1. **Dashboard & Habit Progress**:
   - Time-of-day contextual greeting and formatted date.
   - Animated daily study completion ring and metrics.
   - Genuine study streak counter calculated from completed tasks and focus sessions.
   - Quick actions to Add Task, Generate AI Plan, or Start a Focus Session.

2. **Full CRUD Task Management**:
   - Create, edit, and delete tasks with confirmation dialogs.
   - Filter by All, Today, Upcoming, and Completed.
   - Sort by Due Date, Priority (High/Medium/Low), and Title.
   - Search bar across task titles, subjects, and descriptions.
   - Overdue tasks highlighted with warnings.

3. **AI & Smart Study Plan Generator**:
   - Powered by Gemini 3.5 Flash via REST API with a local rule-based scheduling fallback.
   - Custom inputs for subjects, total time, start time, upcoming exam dates, difficulty, and weak subject prioritization.
   - Break strategy selection (Pomodoro 25/5, Standard 50/10, Deep Work 90/15).
   - "Save Plan as Tasks" button to convert generated timetable blocks into persistent tasks.

4. **Pomodoro Focus Timer**:
   - 25m Focus, 5m Short Break, and 15m Long Break intervals.
   - Precision timestamp-based countdown with Start, Pause, Resume, and Reset.
   - Subject tagging for tracked focus sessions.
   - Haptic vibration feedback on completion.
   - Persistent focus minutes and session counts saved to Room database.

5. **Profile, Settings & Analytics**:
   - 7-day study activity chart displaying focus minutes and tasks completed.
   - Daily study goal slider.
   - Light / Dark / System theme switching.
   - 100% private on-device data persistence with option to reset data.

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3
- **Local Persistence:** Room Database (SQLite) + SharedPreferences
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Reactive State:** Kotlin Coroutines, StateFlow, SharedFlow
- **AI Integration:** Google Gemini REST API (gemini-3.5-flash) + Local Rule Engine Fallback

## 🚀 Building & Testing

### Compile the project:
```bash
gradle assembleDebug
```

### Run local JVM tests (Robolectric & Unit Tests):
```bash
gradle :app:testDebugUnitTest
```
