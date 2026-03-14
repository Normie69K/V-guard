package com.normie69K.v_guard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.normie69K.v_guard.data.repository.AuthRepository
import com.normie69K.v_guard.ui.screens.auth.LoginScreen
import com.normie69K.v_guard.ui.screens.auth.OtpScreen
import com.normie69K.v_guard.ui.screens.auth.SplashScreen
import com.normie69K.v_guard.ui.screens.dashboard.AlertScreen
import com.normie69K.v_guard.ui.screens.dashboard.DashboardScreen
import com.normie69K.v_guard.ui.screens.setup.EmergencyContactsScreen
import com.normie69K.v_guard.ui.screens.setup.LinkDeviceScreen
import com.normie69K.v_guard.ui.screens.auth.RegisterScreen
import com.normie69K.v_guard.ui.screens.auth.ForgotPasswordScreen
import com.normie69K.v_guard.ui.screens.settings.SettingsScreen
import com.normie69K.v_guard.ui.screens.settings.TripHistoryScreen
import com.normie69K.v_guard.ui.screens.settings.AboutScreen

@Composable
fun AppNavigation(startDestination: String = "splash") {
    val navController  = rememberNavController()
    val authRepository = AuthRepository()

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
                }
            )
        }

        // ── Login (email/password) ────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToPhoneAuth = { navController.navigate("otp") },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        // ── Forgot Password (new) ─────────────────────────────────────────────
        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // ── Register (new) ────────────────────────────────────────────────────
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("link_device") { popUpTo("register") { inclusive = true } }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── OTP / Phone auth ──────────────────────────────────────────────────
        composable("otp") {
            OtpScreen(
                onBackToLogin = { navController.popBackStack() },
                onAuthSuccess = {
                    // After phone login → ask user to link a device
                    navController.navigate("link_device") { popUpTo("login") { inclusive = true } }
                }
            )
        }

        // ── Link device ───────────────────────────────────────────────────────
        composable("link_device") {
            LinkDeviceScreen(
                onDeviceLinked = {
                    navController.navigate("emergency_contacts") {
                        popUpTo("link_device") { inclusive = true }
                    }
                }
            )
        }

        // ── Emergency contacts setup ──────────────────────────────────────────
        composable("emergency_contacts") {
            EmergencyContactsScreen(
                onSaveComplete = {
                    navController.navigate("dashboard") {
                        popUpTo("emergency_contacts") { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ─────────────────────────────────────────────────────────
        composable("dashboard") {
            DashboardScreen(
                onCrashDetected = { lat, lng ->
                    // Pass crash coordinates through nav args to AlertScreen
                    navController.navigate("alert/$lat/$lng")
                },
                onLogout = {
                    authRepository.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable("settings") {
            SettingsScreen(
                onNavigateToLinkDevice = { navController.navigate("link_device") },
                onNavigateToContacts   = { navController.navigate("emergency_contacts") },
                onBack                 = { navController.popBackStack() },
                onNavigateToHistory    = { navController.navigate("trip_history") },
                onNavigateToAbout      = { navController.navigate("about") }
            )
        }

        // ── Trip History ──────────────────────────────────────────────────────
        composable("trip_history") {
            TripHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── About Screen ──────────────────────────────────────────────────────
        composable("about") {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Alert  —  receives lat & lng from crash event ─────────────────────
        composable(
            route     = "alert/{lat}/{lng}",
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lng") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lng = backStackEntry.arguments?.getFloat("lng")?.toDouble() ?: 0.0

            AlertScreen(
                crashLat      = lat,
                crashLng      = lng,
                onCancelAlert = {
                    navController.navigate("dashboard") {
                        popUpTo("alert/{lat}/{lng}") { inclusive = true }
                    }
                },
                onSosSent = {
                    navController.navigate("dashboard") {
                        popUpTo("alert/{lat}/{lng}") { inclusive = true }
                    }
                }
            )
        }
    }
}