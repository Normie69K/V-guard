package com.normie69K.v_guard.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.normie69K.v_guard.data.repository.FirebaseDbHelper
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(onBack: () -> Unit) {
    val dbHelper = remember { FirebaseDbHelper() }
    val coroutineScope = rememberCoroutineScope()

    var linkedDevices by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedEspId by remember { mutableStateOf("") }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Crash prevention flag
    var isMapLoaded by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()

    // 1. Load the user's vehicles on startup
    LaunchedEffect(Unit) {
        dbHelper.getLinkedDevices { devices ->
            linkedDevices = devices
            if (devices.isNotEmpty()) {
                selectedEspId = devices.first()
            }
        }
    }

    // 2. Fetch the route history
    LaunchedEffect(selectedEspId) {
        if (selectedEspId.isNotBlank()) {
            isLoading = true
            dbHelper.getDeviceHistory(selectedEspId) { points ->
                routePoints = points
                isLoading = false
            }
        }
    }

    // 3. Move camera ONLY if the map is fully loaded and we have points
    LaunchedEffect(routePoints, isMapLoaded) {
        if (isMapLoaded && routePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            routePoints.forEach { boundsBuilder.include(it) }

            try {
                // Animate smoothly instead of jumping instantly
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150))
            } catch (e: Exception) {
                // Fallback for extremely small bounds (e.g. all points are identical)
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(routePoints.last(), 15f))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Spacer(Modifier.width(8.dp))
            Text("Trip History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        // ── Vehicle Selector ────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            OutlinedButton(
                onClick = { dropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedEspId.isNotBlank()) "Vehicle: $selectedEspId" else "No vehicles linked")
            }
            DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                linkedDevices.forEach { deviceId ->
                    DropdownMenuItem(
                        text = { Text(deviceId) },
                        onClick = {
                            selectedEspId = deviceId
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Map ─────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = { isMapLoaded = true }
                ) {
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = MaterialTheme.colorScheme.primary,
                            width = 12f
                        )
                        Marker(state = MarkerState(position = routePoints.first()), title = "Start")
                        Marker(state = MarkerState(position = routePoints.last()), title = "Current")
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (routePoints.isEmpty() && selectedEspId.isNotBlank()) {
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("No recent history found.", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}