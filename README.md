# Tuskly

A minimal, offline-first Android app for daily goal tracking and one-time tasks. Built with Kotlin, Jetpack Compose, and Material 3. No accounts, no cloud, no telemetry — your data stays on your device.

## Features

**Daily Goals** — Recurring habits that automatically reset each day.
- Binary (check/uncheck) and quantity-based (slider) goal types
- Configurable day reset time (midnight by default, adjustable in Settings)
- Swipe between Goals and Tasks with a horizontal pager

**Tasks** — A straightforward to-do list.
- Optional due date and time with overdue highlighting
- Swipe to complete or delete
- Collapsible completed section

**Home Screen Widgets** — Glance-based widgets for both Goals and Tasks, with inline toggle support.

**Privacy by Design**
- Zero permissions required
- No network access, no analytics, no tracking
- All data stored locally in a Room database
- Material You dynamic color support
- AMOLED-friendly dark theme

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose (BOM 2025.05.01), Material 3 |
| Architecture | Single Activity, MVVM, Hilt 2.59.1 |
| Database | Room 2.7.1 |
| Widgets | Glance 1.1.1 |
| Language | Kotlin 2.3.0 (100%) |
| Build | AGP 9.0.1, Gradle 9.3.1, KSP 2.3.6 |
| Min SDK | 26 (Android 8.0) |

## Build

```bash
# Debug
./gradlew assembleDebug

# Release (R8 minified)
./gradlew assembleRelease

# Tests
./gradlew testDebugUnitTest
```

## Project Structure

```
com.example.minimaltodo
├── data
│   ├── dao/           # GoalDao, TaskDao, CompletionLogDao
│   ├── entity/        # Goal, Task, CompletionLog
│   ├── db/            # AppDatabase
│   └── repository/    # GoalRepository, TaskRepository, SettingsRepository
├── di/                # Hilt modules
├── ui
│   ├── components/    # SwipeToDismissItem, CheckRow, EmptyState, etc.
│   ├── goals/         # DailyGoalsScreen, GoalsViewModel, GoalCard
│   ├── tasks/         # TasksScreen, TasksViewModel
│   ├── completed/     # CompletedScreen
│   ├── deleted/       # RecentlyDeletedScreen
│   ├── settings/      # SettingsScreen, SettingsViewModel
│   └── theme/         # Material 3 theme, colors, typography
├── util/              # Date helpers (logical date, reset time)
├── widget/            # Glance widgets for Goals and Tasks
├── MinimalTodoApp.kt
└── MainActivity.kt
```

## License

MIT
