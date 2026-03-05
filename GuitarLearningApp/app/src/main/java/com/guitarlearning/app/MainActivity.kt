package com.guitarlearning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.guitarlearning.app.data.LessonRepository
import com.guitarlearning.app.data.ProgressStore
import com.guitarlearning.app.ui.screens.*
import com.guitarlearning.app.ui.theme.GuitarLearningTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home    : Screen("home",    "Home",    Icons.Default.Home)
    data object Lessons : Screen("lessons", "Lessons", Icons.Default.MenuBook)
    data object Chords  : Screen("chords",  "Chords",  Icons.Default.LibraryMusic)
    data object Scales  : Screen("scales",  "Scales",  Icons.Default.Piano)
    data object Tuner   : Screen("tuner",   "Tuner",   Icons.Default.GraphicEq)
}

private val topLevelScreens = listOf(
    Screen.Home, Screen.Lessons, Screen.Chords, Screen.Scales, Screen.Tuner
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val progressStore = ProgressStore(applicationContext)

        setContent {
            GuitarLearningTheme {
                GuitarApp(progressStore)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuitarApp(progressStore: ProgressStore) {
    val navController = rememberNavController()
    val scope         = rememberCoroutineScope()

    val completedIds by progressStore.completedLessonIds.collectAsStateWithLifecycle(emptySet())

    // State for the lesson detail sheet
    var selectedLessonId by remember { mutableStateOf<Int?>(null) }
    val selectedLesson    = selectedLessonId?.let { id ->
        LessonRepository.lessons.firstOrNull { it.id == id }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest       = navBackStackEntry?.destination

    // Determine if we're showing the detail screen
    val showDetail = selectedLessonId != null && selectedLesson != null

    if (showDetail) {
        LessonDetailScreen(
            lesson           = selectedLesson!!,
            isCompleted      = selectedLesson.id in completedIds,
            onBack           = { selectedLessonId = null },
            onMarkComplete   = {
                scope.launch { progressStore.markLessonComplete(selectedLesson.id) }
            },
            onMarkIncomplete = {
                scope.launch { progressStore.markLessonIncomplete(selectedLesson.id) }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentDest?.route) {
                            Screen.Home.route    -> "Guitar Learning"
                            Screen.Lessons.route -> "Lessons"
                            Screen.Chords.route  -> "Chord Library"
                            Screen.Scales.route  -> "Scales & Modes"
                            Screen.Tuner.route   -> "Chromatic Tuner"
                            else                 -> "Guitar Learning"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                topLevelScreens.forEach { screen ->
                    NavigationBarItem(
                        icon     = { Icon(screen.icon, contentDescription = screen.label) },
                        label    = { Text(screen.label) },
                        selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    completedLessonIds   = completedIds,
                    onNavigateToLessons  = { navController.navigate(Screen.Lessons.route) },
                    onNavigateToChords   = { navController.navigate(Screen.Chords.route) },
                    onNavigateToScales   = { navController.navigate(Screen.Scales.route) },
                    onNavigateToTuner    = { navController.navigate(Screen.Tuner.route) }
                )
            }
            composable(Screen.Lessons.route) {
                LessonsScreen(
                    completedLessonIds = completedIds,
                    onLessonClick      = { lesson -> selectedLessonId = lesson.id }
                )
            }
            composable(Screen.Chords.route)  { ChordsScreen() }
            composable(Screen.Scales.route)  { ScalesScreen() }
            composable(Screen.Tuner.route)   { TunerScreen() }
        }
    }
}
