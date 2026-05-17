package com.gymlog.app.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gymlog.app.presentation.viewmodel.OnboardingViewModel
import com.gymlog.app.presentation.screens.body.BodyDataScreen
import com.gymlog.app.presentation.screens.dashboard.DashboardScreen
import com.gymlog.app.presentation.screens.exercise.ExerciseDetailScreen
import com.gymlog.app.presentation.screens.history.HistoryScreen
import com.gymlog.app.presentation.screens.onboarding.OnboardingScreen
import com.gymlog.app.presentation.screens.review.ReviewScreen
import com.gymlog.app.presentation.screens.session.SessionScreen
import com.gymlog.app.presentation.screens.settings.SettingsScreen
import com.gymlog.app.presentation.screens.template.TemplateListScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val SESSION = "session"
    const val SESSION_FROM_TEMPLATE = "session_from_template/{templateId}"
    const val REVIEW = "review/{sessionId}"
    const val HISTORY = "history"
    const val EXERCISE_DETAIL = "exercise/{exerciseId}"
    const val BODY_DATA = "body_data"
    const val TEMPLATES = "templates"
    const val SETTINGS = "settings"

    fun review(sessionId: String) = "review/$sessionId"
    fun exercise(exerciseId: String) = "exercise/$exerciseId"
    fun sessionFromTemplate(templateId: String) = "session_from_template/$templateId"
}

@Composable
fun GymLogNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onComplete = {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onStartSession = { navController.navigate(Routes.SESSION) },
                onViewReview = { sessionId -> navController.navigate(Routes.review(sessionId)) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToBodyData = { navController.navigate(Routes.BODY_DATA) },
                onNavigateToTemplates = { navController.navigate(Routes.TEMPLATES) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToExerciseDetail = { id -> navController.navigate(Routes.exercise(id)) }
            )
        }

        composable(Routes.SESSION) {
            SessionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            Routes.SESSION_FROM_TEMPLATE,
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            SessionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            Routes.REVIEW,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ReviewScreen(sessionId = sessionId, onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            HistoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            Routes.EXERCISE_DETAIL,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) {
            ExerciseDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.BODY_DATA) {
            BodyDataScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.TEMPLATES) {
            TemplateListScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartFromTemplate = { templateId ->
                    navController.navigate(Routes.sessionFromTemplate(templateId))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
