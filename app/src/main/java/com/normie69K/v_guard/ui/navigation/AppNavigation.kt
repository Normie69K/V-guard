package com.normie69K.v_guard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.normie69K.v_guard.ui.screens.auth.LoginScreen
import com.normie69K.v_guard.ui.screens.auth.SplashScreen


@Composable
fun AppNavigation(startDestination : String = "splash") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            SplashScreen(onNavigateToLogin = {
                navController.navigate("login") { popUpTo("splash") { inclusive = true } }
            }, onNavigateToDashboard = {
                navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
            })
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("link_device") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToPhoneAuth = {
                    navController.navigate("otp") // Routes to our new screen
                }
            )
        }

        composable("otp") {
            OtpScreen(
                onBackToLogin = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    navController.navigate("link_device") { popUpTo("login") { inclusive = true } }
                }
            )
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