@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.gymtracker.app.R
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.gymtracker.app.ui.screens.achievements.AchievementsScreen
import com.gymtracker.app.ui.screens.exercises.ExerciseLibraryScreen
import com.gymtracker.app.ui.screens.history.HistoryScreen
import com.gymtracker.app.ui.screens.home.HomeScreen
import com.gymtracker.app.ui.screens.onboarding.OnboardingScreen
import com.gymtracker.app.ui.screens.plans.PlanDetailScreen
import com.gymtracker.app.ui.screens.plans.PlansScreen
import com.gymtracker.app.ui.screens.progress.ProgressScreen
import com.gymtracker.app.ui.screens.settings.SettingsScreen
import com.gymtracker.app.ui.screens.workout.ActiveWorkoutScreen
import com.gymtracker.app.ui.screens.workout.WorkoutSummaryScreen
import com.gymtracker.app.ui.viewmodel.SettingsViewModel

private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun NavGraph(
    onboardingDone: Boolean,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val startDestination = if (onboardingDone) Screen.Home.route else Screen.Onboarding.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home,            R.string.nav_home,      Icons.Default.Home),
        BottomNavItem(Screen.Plans,           R.string.nav_plans,     Icons.Default.FitnessCenter),
        BottomNavItem(Screen.History,         R.string.nav_history,   Icons.Default.History),
        BottomNavItem(Screen.Progress,        R.string.nav_progress,  Icons.Default.TrendingUp),
        BottomNavItem(Screen.ExerciseLibrary, R.string.nav_exercises, Icons.Default.List)
    )

    val bottomBarRoutes = bottomNavItems.map { it.screen.route }.toSet()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    settingsViewModel = settingsViewModel
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    },
                    onOpenAchievements = {
                        navController.navigate(Screen.Achievements.route)
                    },
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(Screen.Plans.route) {
                PlansScreen(
                    onOpenPlan = { planId ->
                        navController.navigate(Screen.PlanDetail.createRoute(planId))
                    }
                )
            }
            composable(Screen.PlanDetail.route) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId")?.toLong() ?: return@composable
                PlanDetailScreen(
                    planId = planId,
                    onStartWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.ActiveWorkout.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                ActiveWorkoutScreen(
                    sessionId = sessionId,
                    onWorkoutFinished = { finishedId ->
                        navController.navigate(Screen.WorkoutSummary.createRoute(finishedId)) {
                            popUpTo(Screen.ActiveWorkout.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.WorkoutSummary.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) {
                WorkoutSummaryScreen(
                    onDone = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onRepeatWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    }
                )
            }
            composable(Screen.Progress.route) { ProgressScreen() }
            composable(Screen.ExerciseLibrary.route) { ExerciseLibraryScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Achievements.route) {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
