package com.normie69K.v_guard.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.normie69K.v_guard.data.repository.FirebaseDbHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLinkDevice: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onBack: () -> Unit
) {
    val dbHelper = remember { FirebaseDbHelper() }
    var espId by remember { mutableStateOf<String>("Loading...") }

    // Fetch the currently linked device ID to display it
    LaunchedEffect(Unit) {
        dbHelper.getLinkedEspId { id ->
            espId = if (!id.isNullOrBlank()) id else "No device linked"
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ── Update Device Menu Item ───────────────────────────────────────
            ListItem(
                modifier = Modifier.clickable { onNavigateToLinkDevice() },
                headlineContent = { Text("Manage ESP32 Device") },
                supportingContent = { Text("Current: $espId") },
                leadingContent = { Icon(Icons.Default.DeveloperBoard, null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )

            HorizontalDivider()

            // ── Update Contacts Menu Item ─────────────────────────────────────
            ListItem(
                modifier = Modifier.clickable { onNavigateToContacts() },
                headlineContent = { Text("Emergency Contacts") },
                supportingContent = { Text("Add or remove SOS numbers") },
                leadingContent = { Icon(Icons.Default.Contacts, null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )

            HorizontalDivider()
        }
    }
}