package com.normie69K.v_guard.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
// import com.google.firebase.auth.FirebaseAuth

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2000) // 2-second delay for the branding to show

        // TODO: Uncomment when Firebase is added
        // val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUser = null // Simulating no user for now

        if (currentUser != null) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "GuardianDrive",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}