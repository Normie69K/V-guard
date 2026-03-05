package com.normie69K.v_guard // Ensure this matches your actual project package

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.normie69K.v_guard.ui.navigation.AppNavigation
import com.normie69K.v_guard.ui.theme.VguardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Check the intent INSIDE onCreate
        val isCrashDetected = intent.getBooleanExtra("CRASH_DETECTED", false)

        // 2. Decide where the app should start
        val startRoute = if (isCrashDetected) "alert" else "splash"

        setContent {
            VguardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Pass the route into your Navigation graph
                    AppNavigation(startDestination = startRoute)
                }
            }
        }
    }
}