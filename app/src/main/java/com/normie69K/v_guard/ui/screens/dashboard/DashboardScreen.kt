package com.normie69K.v_guard.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onCrashDetected: (lat: Double, lng: Double) -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val status by viewModel.vehicleStatus.collectAsState()
    val linkedDevices by viewModel.linkedDevices.collectAsState()
    val selectedEspId by viewModel.selectedEspId.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Navigate to alert when crash flag fires — only once per event
    var crashHandled by remember { mutableStateOf(false) }
    LaunchedEffect(status.isAccident) {
        if (status.isAccident && !crashHandled) {
            crashHandled = true
            onCrashDetected(status.latitude, status.longitude)
        }
        if (!status.isAccident) crashHandled = false
    }

    val fallbackLat = 19.0760
    val fallbackLng = 72.8777
    val vehicleLocation = LatLng(
        if (status.latitude  != 0.0) status.latitude  else fallbackLat,
        if (status.longitude != 0.0) status.longitude else fallbackLng
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vehicleLocation, 15f)
    }
    LaunchedEffect(vehicleLocation) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(vehicleLocation))
    }

    val isAlert = status.isAccident

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "V-Guard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )

                // Dropdown to select vehicle
                Box {
                    TextButton(
                        onClick = { dropdownExpanded = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.offset(x = (-8).dp)
                    ) {
                        Text(
                            text = if (selectedEspId.isNotBlank()) "Vehicle: ${selectedEspId.take(8)} ▼" else "No devices linked",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        Text(
                            text = "Connected Devices: ${linkedDevices.size}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()
                        linkedDevices.forEach { deviceId ->
                            DropdownMenuItem(
                                text = { Text(if (deviceId == selectedEspId) "✅ $deviceId" else deviceId) },
                                onClick = {
                                    viewModel.selectDevice(deviceId)
                                    dropdownExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Link New Vehicle") },
                            onClick = {
                                dropdownExpanded = false
                                onNavigateToSettings()
                            }
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pass accurate state to the badge
                StatusBadge(espId = selectedEspId, isAlert = isAlert, lastSeen = status.lastSeen)
                Spacer(Modifier.width(8.dp))

                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, "Logout", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Status card ──────────────────────────────────────────────────────
        AnimatedContent(
            targetState = isAlert,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { alert ->

            // Smarter Card Text logic
            val cardTitle = when {
                selectedEspId.isBlank() -> "No Vehicle Linked"
                alert -> "⚠️  CRASH DETECTED"
                status.lastSeen == 0L -> "⏳  Awaiting Signal..."
                else -> "✅  Vehicle Secured"
            }

            val cardIcon = when {
                selectedEspId.isBlank() -> Icons.Default.Info
                alert -> Icons.Default.Warning
                status.lastSeen == 0L -> Icons.Default.Wifi
                else -> Icons.Default.Security
            }

            val cardColor = when {
                alert -> MaterialTheme.colorScheme.errorContainer
                selectedEspId.isBlank() || status.lastSeen == 0L -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primaryContainer
            }

            val contentColor = when {
                alert -> MaterialTheme.colorScheme.error
                selectedEspId.isBlank() || status.lastSeen == 0L -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.primary
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = cardIcon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = contentColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = cardTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = contentColor
                        )
                        Spacer(Modifier.height(2.dp))
                        val timeText = when {
                            selectedEspId.isBlank() -> "Go to Settings to add a device"
                            status.lastSeen > 0 -> "Updated: ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(status.lastSeen))}"
                            else -> "Hardware is offline"
                        }
                        Text(
                            timeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Stats row ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.GpsFixed,
                label       = "GPS",
                value       = if (status.latitude != 0.0) "Active" else "N/A",
                highlighted = status.latitude != 0.0
            )
            StatCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Default.Wifi,
                label       = "Signal",
                value       = if (status.wifiSignal != 0) "${status.wifiSignal} dBm" else "—",
                highlighted = false
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Map ──────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .padding(bottom = 16.dp),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state   = MarkerState(position = vehicleLocation),
                    title   = if (selectedEspId.isNotBlank()) "Vehicle (${selectedEspId.take(8)})" else "No Vehicle",
                    snippet = if (status.lastSeen > 0)
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(status.lastSeen))
                    else
                        "Awaiting signal"
                )
            }
        }
    }
}

// ── Reusable stat card ────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    modifier: Modifier    = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    highlighted: Boolean
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier        = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (highlighted)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    modifier = Modifier.size(18.dp),
                    tint     = if (highlighted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Live status badge ─────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(espId: String, isAlert: Boolean, lastSeen: Long) {
    // Determine the exact state text and colors
    val (text, color, bg) = when {
        espId.isBlank() -> Triple("NO DEVICE", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        isAlert -> Triple("ALERT", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
        lastSeen == 0L -> Triple("WAITING", MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant)
        else -> Triple("LIVE", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style       = MaterialTheme.typography.labelLarge,
            color       = color,
            fontWeight  = FontWeight.ExtraBold
        )
    }
}