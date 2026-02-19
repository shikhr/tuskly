# Minimal Daily Goals & Tasks Android App

## Project Overview

Offline-first, minimal Android app with two screens: **Daily Goals** (recurring habits that reset at midnight) and **One-time Tasks** (standard to-do list). Kotlin + Jetpack Compose + Room + Hilt. No cloud, no analytics, no telemetry. Target APK < 5 MB.

## Toolchain

- **Java**: OpenJDK 25 (system-installed)
- **Gradle**: 9.3.1 (via wrapper)
- **AGP**: 9.0.1 — has built-in Kotlin support; do NOT apply `org.jetbrains.kotlin.android` plugin
- **Kotlin**: 2.3.0 (managed by AGP 9.0; no separate Kotlin plugin needed)
- **KSP**: 2.3.6
- **Hilt / Dagger**: 2.59.1
- **Compose BOM**: 2025.05.01
- **Room**: 2.7.1
- **Android SDK**: platforms 34 & 35, build-tools 36 (auto-installed by AGP)
- **`kotlinOptions {}` is removed** in AGP 9.0 — use `compilerOptions {}` in the `kotlin {}` block instead

## Build / Lint / Test Commands

All commands run from the project root using the Gradle wrapper.

```bash
# Full debug build
./gradlew assembleDebug

# Release build (with R8 minification)
./gradlew assembleRelease

# Lint (Android Lint)
./gradlew lintDebug

# Run ALL unit tests
./gradlew testDebugUnitTest

# Run a SINGLE test class
./gradlew testDebugUnitTest --tests "com.example.minimaltodo.data.repository.GoalRepositoryTest"

# Run a SINGLE test method
./gradlew testDebugUnitTest --tests "com.example.minimaltodo.data.repository.GoalRepositoryTest.insertGoal_returnsFlow"

# Run ALL instrumented (on-device / emulator) tests
./gradlew connectedDebugAndroidTest

# Run a single instrumented test class
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.minimaltodo.ui.goals.DailyGoalsScreenTest

# Clean build
./gradlew clean

# Dependency tree (useful for checking bloat)
./gradlew app:dependencies --configuration debugRuntimeClasspath

# Check for version catalog updates (if plugin configured)
./gradlew versionCatalogUpdate
```

## Package Structure

```
com.example.minimaltodo
├── data
│   ├── dao/          # Room DAOs (GoalDao, TaskDao, CompletionLogDao)
│   ├── entity/       # Room entities (Goal, Task, CompletionLog)
│   ├── db/           # AppDatabase definition
│   └── repository/   # Repository classes wrapping DAOs
├── di/               # Hilt modules (DatabaseModule)
├── ui
│   ├── components/   # Reusable composables (CheckRow, SwipeToDismissItem, AddItemField)
│   ├── goals/        # DailyGoalsScreen, GoalsViewModel, GoalsUiState
│   ├── tasks/        # TasksScreen, TasksViewModel, TasksUiState
│   └── theme/        # Theme, Color, Typography definitions
├── util/             # Extension functions, date helpers
├── MinimalTodoApp.kt # @HiltAndroidApp Application class
└── MainActivity.kt   # Single Activity entry point
```

## Code Style & Conventions

### Language & SDK
- Kotlin 100%. No Java files.
- Min SDK 26 (Android 8.0). Target latest stable SDK.

### Naming
- **Files**: PascalCase for `@Composable` files (`DailyGoalsScreen.kt`), camelCase for utilities (`dateUtils.kt`).
- **Classes/Objects**: PascalCase (`GoalRepository`, `AppDatabase`).
- **Functions**: camelCase. Composable functions use PascalCase (`@Composable fun GoalRow()`).
- **Constants**: UPPER_SNAKE_CASE in companion objects.
- **Packages**: lowercase, no underscores.

### Formatting
- 4-space indentation (no tabs).
- Max line length: 120 characters.
- Trailing commas in parameter lists and when-branches.
- Use expression-body (`= ...`) for single-expression functions.

### Imports
- No wildcard imports. Explicit imports only.
- Order: `android.*`, `androidx.*`, `com.example.*`, `java/javax.*`, `kotlin.*`. Sorted alphabetically within groups.
- Remove unused imports.

### Types
- Prefer `val` over `var`. Immutable data structures by default.
- Use `data class` for entities and UI state.
- Use `sealed class` / `sealed interface` for UI state hierarchies.
- Avoid nullable types when a sensible default exists.
- Use `Flow<T>` for reactive data from Room; never expose `LiveData` from repositories.

### Compose
- State hoisting mandatory: composables receive state + event lambdas, never create ViewModels.
- Use `collectAsStateWithLifecycle()` to collect Flows in composables.
- Keep composable functions under 100 lines. Extract sub-composables liberally.
- Use `remember {}` and `derivedStateOf {}` to avoid unnecessary recompositions.
- Use `LazyColumn` for any list of items.
- All user-visible strings go in `res/values/strings.xml` (use `stringResource()`).

### ViewModels
- Extend `androidx.lifecycle.ViewModel`.
- Inject dependencies via Hilt `@HiltViewModel` + `@Inject constructor`.
- Expose UI state as `StateFlow`. Use `MutableStateFlow` internally.
- Handle one-off events via `Channel<Event>` consumed as `Flow`.

### Room / Data Layer
- DAOs return `Flow<List<T>>` for queries, `suspend` functions for inserts/updates/deletes.
- Repositories wrap DAOs and add business logic. No direct DAO access from ViewModels.
- Entity field naming: camelCase matching Kotlin conventions; Room handles column mapping.

### Error Handling
- Use `try/catch` sparingly and only where recovery is possible.
- Prefer `Result<T>` or sealed state classes (`Loading`, `Success`, `Error`) over throwing exceptions.
- Log errors with `android.util.Log` (debug builds only). No crashlytics/analytics.

### Testing
- Unit tests live in `app/src/test/`. Instrumented tests in `app/src/androidTest/`.
- Test file naming: `<ClassUnderTest>Test.kt` (e.g., `GoalRepositoryTest.kt`).
- Use fakes for DAOs in repository tests; don't mock Room directly.
- ViewModel tests: inject fake repositories, assert StateFlow emissions.
- Compose UI tests: use `createComposeRule()`, test only critical flows.
- Assertions: prefer `assertEquals`, `assertTrue` from `kotlin.test` or JUnit 4.

### Git
- Default branch: `main`.
- Commit messages: Conventional Commits (`feat:`, `fix:`, `chore:`, `refactor:`, `test:`, `docs:`).

## Design Constraints (AI Agents Must Follow)

1. **Preserve minimalism**: Do NOT add features like logins, notifications (unless asked), graphs, streaks, social features, complex recurrence, sub-task nesting, or priority levels.
2. **Gestures over buttons**: Prefer swipe and long-press interactions over FABs or toolbar buttons.
3. **No bloat libraries**: Avoid Coil/Glide (no images), Accompanist (use M3 equivalents), Retrofit/OkHttp (offline-only), Firebase, or any analytics SDK.
4. **Privacy first**: No telemetry. Room DB stays on-device. Optional backup uses SAF + user-passphrase encryption.
5. **Performance**: Use `LazyColumn`, `remember`, `derivedStateOf`. Avoid unnecessary recompositions. Aggressive R8 minification.
6. **Typography**: Minimum 18sp body text. High-contrast AMOLED-friendly dark theme.
7. **Architecture**: Single Activity + Navigation Compose. Two destinations only (goals, tasks).
8. **When suggesting code**: Show full functions with imports. Prefer declarative style. Add KDoc only for non-obvious logic.
9. **APK size**: Keep under 5 MB. Prefer stdlib over external libraries. Check dependency tree when adding anything.
