package com.gymtracker.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Plans : Screen("plans")
    object PlanDetail : Screen("plan_detail/{planId}") {
        fun createRoute(planId: Long) = "plan_detail/$planId"
    }
    object ActiveWorkout : Screen("active_workout/{sessionId}") {
        fun createRoute(sessionId: Long) = "active_workout/$sessionId"
    }
    object History : Screen("history")
    object Progress : Screen("progress")
    object ExerciseLibrary : Screen("exercise_library")
    object Settings : Screen("settings")
    object Achievements : Screen("achievements")
}
