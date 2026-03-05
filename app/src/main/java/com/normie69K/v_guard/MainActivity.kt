package com.normie69K.v_guard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.normie69K.v_guard.services.AccidentMonitorService
import com.normie69K.v_guard.ui.navigation.AppNavigation
import com.normie69K.v_guard.ui.theme.VguardTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions handled gracefully inside each feature */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()
        startMonitoringService()

        // Determine start destination based on whether the service triggered a crash alert
        val isCrashDetected = intent.getBooleanExtra("CRASH_DETECTED", false)
        val startRoute      = if (isCrashDetected) "alert/0.0/0.0" else "splash"

        setContent {
            VguardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(startDestination = startRoute)
                }
            }
        }
    }

    // ── Start the foreground monitoring service ───────────────────────────────

    private fun startMonitoringService() {
        val serviceIntent = Intent(this, AccidentMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    // ── Request all permissions the app needs ─────────────────────────────────

    private fun requestRequiredPermissions() {
        val required = buildList {
            add(Manifest.permission.SEND_SMS)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(Manifest.permission.FOREGROUND_SERVICE)
            }
        }

        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}
