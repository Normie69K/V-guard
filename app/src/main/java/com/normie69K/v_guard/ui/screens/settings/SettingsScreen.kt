package com.normie69K.v_guard.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.normie69K.v_guard.data.repository.FirebaseDbHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLinkDevice: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit
) {
    val dbHelper = remember { FirebaseDbHelper() }
    var deviceCountText by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        dbHelper.getLinkedDevices { devices ->
            deviceCountText = if (devices.isNotEmpty()) "${devices.size} vehicle(s) linked" else "No vehicles linked"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            ListItem(
                modifier = Modifier.clickable { onNavigateToHistory() },
                headlineContent = { Text("Trip History") },
                supportingContent = { Text("View recent routes and tracking data") },
                leadingContent = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            HorizontalDivider()

            ListItem(
                modifier = Modifier.clickable { onNavigateToLinkDevice() },
                headlineContent = { Text("Manage ESP32 Devices") },
                supportingContent = { Text(deviceCountText) },
                leadingContent = { Icon(Icons.Default.DeveloperBoard, null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            HorizontalDivider()

            ListItem(
                modifier = Modifier.clickable { onNavigateToContacts() },
                headlineContent = { Text("Emergency Contacts") },
                supportingContent = { Text("Add or remove SOS numbers") },
                leadingContent = { Icon(Icons.Default.Contacts, null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            HorizontalDivider()

            // ── About Button ──
            ListItem(
                modifier = Modifier.clickable { onNavigateToAbout() },
                headlineContent = { Text("About") },
                supportingContent = { Text("App info, setup guide, and support") },
                leadingContent = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            HorizontalDivider()
        }
    }
}