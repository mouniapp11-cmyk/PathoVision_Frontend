package com.simats.pathovision.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.simats.pathovision.ui.login.LoginScreen
import com.simats.pathovision.ui.main.MainScreen
import com.simats.pathovision.ui.register.RegisterScreen
import com.simats.pathovision.ui.roleselect.SelectRoleScreen
import com.simats.pathovision.ui.splash.SplashScreen

@Composable
fun PathoVisionNavGraph(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable("splash") {
            SplashScreen(
                onNavigateToNext = {
                    navController.navigate(Screen.SelectRole.route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Select Role Screen
        composable(Screen.SelectRole.route) {
            SelectRoleScreen(
                onRoleSelected = { role ->
                    navController.navigate(Screen.Login.createRoute(role))
                }
            )
        }

        // Login Screen — receives role as argument
        composable(
            route = Screen.Login.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "pathologist"
            LoginScreen(
                role = role,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { userRole ->
                    // Route all roles to MainScreen, which handles role-based dashboard display
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SelectRole.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = { role ->
                    // Route all roles to MainScreen, which handles role-based dashboard display
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SelectRole.route) { inclusive = true }
                    }
                }
            )
        }

        // Main app shell — shared bottom nav with role-based content
        composable(Screen.Main.route) {
            MainScreen(
                onLogout = {
                    navController.navigate(Screen.SelectRole.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
