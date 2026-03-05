package com.normie69K.v_guard.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun DashboardScreen() {
    // Defaulting to a central coordinate for now.
    // Later, this will be dynamically updated from Firebase!
    val vehicleLocation = LatLng(19.0760, 72.8777)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vehicleLocation, 14f)
    }

    // State variables representing data we will pull from Firebase
    var isOnline by remember { mutableStateOf(true) }
    var hasAccident by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Top Status Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (hasAccident) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vehicle Status",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (hasAccident) "ACCIDENT DETECTED" else if (isOnline) "System Online - Safe" else "System Offline",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasAccident) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (hasAccident) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // 2. Google Map Integration
        GoogleMap(
            modifier = Modifier.weight(1f), // Takes up the rest of the screen
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            Marker(
                state = MarkerState(position = vehicleLocation),
                title = "ESP32 Node",
                snippet = if (hasAccident) "Crash Location!" else "Last Known Location"
            )
        }
    }
}