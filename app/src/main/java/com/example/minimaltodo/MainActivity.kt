package com.example.minimaltodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minimaltodo.ui.completed.CompletedScreen
import com.example.minimaltodo.ui.completed.CompletedViewModel
import com.example.minimaltodo.ui.deleted.DeletedViewModel
import com.example.minimaltodo.ui.deleted.RecentlyDeletedScreen
import com.example.minimaltodo.ui.goals.AddGoalDialog
import com.example.minimaltodo.ui.goals.DailyGoalsScreen
import com.example.minimaltodo.ui.goals.GoalsViewModel
import com.example.minimaltodo.ui.settings.SettingsScreen
import com.example.minimaltodo.ui.settings.SettingsViewModel
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
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    var showAddDialog by remember { mutableStateOf(false) }

    // Derive selectedTab from pager so it's always in sync
    val selectedTab by remember { derivedStateOf { pagerState.currentPage } }

    // Catch pager overscroll at page 0 to open drawer
    val drawerOpenNestedScroll = remember(drawerState, scope) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.x > 0f && drawerState.isClosed) {
                    scope.launch { drawerState.open() }
                }
                return Offset.Zero
            }
        }
    }

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

    // Drawer native gestures only for drag-to-close when open;
    // opening is handled by a custom edge swipe overlay on Goals page.
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                selectedTab = selectedTab,
                onNavigateToMain = { tab ->
                    currentScreen = AppScreen.MAIN
                    scope.launch { drawerState.close() }
                    scope.launch { pagerState.animateScrollToPage(tab) }
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
                            onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_goals)) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
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
                targetState = currentScreen,
                modifier = Modifier.padding(innerPadding),
                transitionSpec = {
                    val forward = targetState != AppScreen.MAIN
                    val direction = if (forward) 1 else -1
                    (slideInHorizontally { direction * it / 3 } + fadeIn())
                        .togetherWith(slideOutHorizontally { -direction * it / 3 } + fadeOut())
                },
                label = "screen_transition",
            ) { screen ->
                when (screen) {
                    AppScreen.MAIN -> {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(drawerOpenNestedScroll),
                            overscrollEffect = null,
                            flingBehavior = PagerDefaults.flingBehavior(
                                state = pagerState,
                                pagerSnapDistance = PagerSnapDistance.atMost(1),
                                snapAnimationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                ),
                                snapPositionalThreshold = 0.35f,
                            ),
                        ) { page ->
                            when (page) {
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
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        val resetHour by settingsViewModel.resetHour
                            .collectAsStateWithLifecycle(settingsViewModel.getResetHour())
                        SettingsScreen(
                            isDynamicColor = dynamicColor,
                            onDynamicColorToggle = onDynamicColorToggle,
                            resetHour = resetHour,
                            onResetHourChange = settingsViewModel::setResetHour,
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
