package com.normie69K.v_guard.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.FirebaseDbHelper

@Composable
fun EmergencyContactsScreen(onSaveComplete: () -> Unit) {
    val dbHelper = remember { FirebaseDbHelper() }

    var newContact    by remember { mutableStateOf("") }
    var isSaving      by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf("") }
    val contacts      = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Icon(
            Icons.Default.Contacts, null,
            Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text("Emergency Contacts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "These numbers receive an SOS SMS with your GPS location if an accident is detected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // ── Add contact row ───────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value         = newContact,
                onValueChange = { newContact = it; errorMessage = "" },
                label         = { Text("Phone Number (+91…)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                isError       = errorMessage.isNotEmpty()
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    when {
                        newContact.isBlank()          -> errorMessage = "Enter a number"
                        contacts.size >= 5            -> errorMessage = "Max 5 contacts"
                        contacts.contains(newContact) -> errorMessage = "Already added"
                        else -> { contacts.add(newContact.trim()); newContact = "" }
                    }
                },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Default.PersonAdd, "Add contact")
            }
        }

        AnimatedVisibility(errorMessage.isNotEmpty()) {
            Text(
                errorMessage,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Contact list ──────────────────────────────────────────────────────
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(contacts, key = { it }) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(contact, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        IconButton(onClick = { contacts.remove(contact) }) {
                            Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            if (contacts.isEmpty()) {
                item {
                    Text(
                        "No contacts added yet",
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Save button ───────────────────────────────────────────────────────
        Button(
            onClick = {
                isSaving = true
                dbHelper.saveEmergencyContacts(
                    contacts  = contacts.toList(),
                    onSuccess = { isSaving = false; onSaveComplete() },
                    onFailure = { err -> isSaving = false; errorMessage = err }
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(14.dp),
            enabled  = contacts.isNotEmpty() && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Save & Continue", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
