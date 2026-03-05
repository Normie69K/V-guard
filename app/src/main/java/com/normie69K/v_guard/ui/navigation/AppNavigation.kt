package com.normie69K.v_guard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.normie69K.v_guard.ui.screens.auth.LoginScreen
import com.normie69K.v_guard.ui.screens.auth.SplashScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToLogin = {
                navController.navigate("login") { popUpTo("splash") { inclusive = true } }
            }, onNavigateToDashboard = {
                navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
            })
        }
        composable("login") {
            LoginScreen(onLoginSuccess = {
                // After login, we usually check if a device is linked.
                // For now, let's navigate to the linking screen.
                navController.navigate("link_device")
            })
        }
        composable("link_device") {
            // LinkDeviceScreen()
        }
        composable("dashboard") {
            // DashboardScreen()
        }
        composable("alert") {
            // EmergencyAlertScreen()
        }
    }
}