package com.example.minimaltodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.minimaltodo.ui.completed.CompletedScreen
import com.example.minimaltodo.ui.completed.CompletedViewModel
import com.example.minimaltodo.ui.deleted.DeletedViewModel
import com.example.minimaltodo.ui.deleted.RecentlyDeletedScreen
import com.example.minimaltodo.ui.goals.AddGoalDialog
import com.example.minimaltodo.ui.goals.DailyGoalsScreen
import com.example.minimaltodo.ui.goals.GoalsViewModel
import com.example.minimaltodo.ui.settings.SettingsScreen
import com.example.minimaltodo.ui.tasks.AddTaskDialog
import com.example.minimaltodo.ui.tasks.TasksScreen
import com.example.minimaltodo.ui.tasks.TasksViewModel
import com.example.minimaltodo.ui.theme.MinimalTodoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private enum class AppScreen {
    MAIN,
    COMPLETED,
    DELETED,
    SETTINGS,
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var dynamicColor by rememberSaveable { mutableStateOf(true) }
            MinimalTodoTheme(dynamicColor = dynamicColor) {
                MinimalTodoContent(
                    dynamicColor = dynamicColor,
                    onDynamicColorToggle = { dynamicColor = it },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MinimalTodoContent(
    dynamicColor: Boolean,
    onDynamicColorToggle: (Boolean) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MAIN) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Hoist ViewModels so they're accessible from both tabs and dialog
    val goalsViewModel: GoalsViewModel = hiltViewModel()
    val tasksViewModel: TasksViewModel = hiltViewModel()

    // Back button: close drawer if open, navigate to MAIN if on sub-screen
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.MAIN) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            currentScreen != AppScreen.MAIN -> currentScreen = AppScreen.MAIN
        }
    }

    val currentTitle = when (currentScreen) {
        AppScreen.MAIN -> if (selectedTab == 0) {
            stringResource(R.string.daily_goals_title)
        } else {
            stringResource(R.string.tasks_title)
        }
        AppScreen.COMPLETED -> stringResource(R.string.drawer_completed)
        AppScreen.DELETED -> stringResource(R.string.drawer_deleted)
        AppScreen.SETTINGS -> stringResource(R.string.drawer_settings)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                selectedTab = selectedTab,
                onNavigateToMain = { tab ->
                    currentScreen = AppScreen.MAIN
                    selectedTab = tab
                    scope.launch { drawerState.close() }
                },
                onNavigateToScreen = { screen ->
                    currentScreen = screen
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        if (currentScreen == AppScreen.MAIN) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.cd_open_menu),
                                )
                            }
                        } else {
                            IconButton(onClick = { currentScreen = AppScreen.MAIN }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_go_back),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
            bottomBar = {
                if (currentScreen == AppScreen.MAIN) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_goals)) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_tasks)) },
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentScreen == AppScreen.MAIN) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_add_item),
                        )
                    }
                }
            },
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentScreen to selectedTab,
                modifier = Modifier.padding(innerPadding),
                transitionSpec = {
                    val (oldScreen, oldTab) = initialState
                    val (newScreen, newTab) = targetState
                    if (oldScreen == AppScreen.MAIN && newScreen == AppScreen.MAIN) {
                        // Tab switch: slide based on tab direction
                        val direction = if (newTab > oldTab) 1 else -1
                        (slideInHorizontally { direction * it / 3 } + fadeIn())
                            .togetherWith(slideOutHorizontally { -direction * it / 3 } + fadeOut())
                    } else {
                        // Screen switch: slide right for forward, left for back
                        val forward = newScreen != AppScreen.MAIN
                        val direction = if (forward) 1 else -1
                        (slideInHorizontally { direction * it / 3 } + fadeIn())
                            .togetherWith(slideOutHorizontally { -direction * it / 3 } + fadeOut())
                    }
                },
                label = "screen_transition",
            ) { (screen, tab) ->
                when (screen) {
                    AppScreen.MAIN -> {
                        when (tab) {
                            0 -> DailyGoalsScreen(
                                viewModel = goalsViewModel,
                                modifier = Modifier.fillMaxSize(),
                            )
                            1 -> TasksScreen(
                                viewModel = tasksViewModel,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    AppScreen.COMPLETED -> {
                        val viewModel: CompletedViewModel = hiltViewModel()
                        CompletedScreen(viewModel = viewModel)
                    }
                    AppScreen.DELETED -> {
                        val viewModel: DeletedViewModel = hiltViewModel()
                        RecentlyDeletedScreen(viewModel = viewModel)
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            isDynamicColor = dynamicColor,
                            onDynamicColorToggle = onDynamicColorToggle,
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        if (selectedTab == 0) {
            AddGoalDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, targetType, targetValue ->
                    goalsViewModel.addGoal(name, targetType, targetValue)
                    showAddDialog = false
                },
            )
        } else {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, dueDate ->
                    tasksViewModel.addTask(title, dueDate)
                    showAddDialog = false
                },
            )
        }
    }
}

@Composable
private fun AppDrawerContent(
    currentScreen: AppScreen,
    selectedTab: Int,
    onNavigateToMain: (Int) -> Unit,
    onNavigateToScreen: (AppScreen) -> Unit,
) {
    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_goals)) },
            selected = currentScreen == AppScreen.MAIN && selectedTab == 0,
            onClick = { onNavigateToMain(0) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_tasks)) },
            selected = currentScreen == AppScreen.MAIN && selectedTab == 1,
            onClick = { onNavigateToMain(1) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            label = { Text(stringResource(R.string.drawer_completed)) },
            selected = currentScreen == AppScreen.COMPLETED,
            onClick = { onNavigateToScreen(AppScreen.COMPLETED) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            label = { Text(stringResource(R.string.drawer_deleted)) },
            selected = currentScreen == AppScreen.DELETED,
            onClick = { onNavigateToScreen(AppScreen.DELETED) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.drawer_settings)) },
            selected = currentScreen == AppScreen.SETTINGS,
            onClick = { onNavigateToScreen(AppScreen.SETTINGS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}
