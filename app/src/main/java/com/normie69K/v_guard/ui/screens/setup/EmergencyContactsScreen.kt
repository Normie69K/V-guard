package com.normie69K.v_guard.ui.screens.setup

import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.normie69K.v_guard.data.repository.FirebaseDbHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(onSaveComplete: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { FirebaseDbHelper() }
    val scrollState = rememberScrollState()

    var newContact by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val contacts = remember { mutableStateListOf<String>() }

    // Fetch existing contacts when the screen loads
    LaunchedEffect(Unit) {
        dbHelper.getEmergencyContacts { existingContacts ->
            contacts.clear()
            contacts.addAll(existingContacts)
        }
    }

    // ── Contact Picker Logic ──────────────────────────────────────────────────
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val phones = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null,
                            null
                        )
                        phones?.use { pCursor ->
                            if (pCursor.moveToFirst()) {
                                val number = pCursor.getString(pCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                val cleanNumber = number.replace(Regex("[^0-9+]"), "")
                                if (!contacts.contains(cleanNumber)) {
                                    if (contacts.size < 5) {
                                        contacts.add(cleanNumber)
                                        errorMessage = ""
                                    } else {
                                        errorMessage = "Max 5 contacts allowed"
                                    }
                                } else {
                                    errorMessage = "Contact already added"
                                }
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                errorMessage = "Contact permission denied. Please enable it in Settings."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // ── Header with Dynamic Counter ───────────────────────────────────
            Icon(Icons.Default.Contacts, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Emergency Contacts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                // ── The Counter Badge ──
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (contacts.size == 5) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${contacts.size}/5",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (contacts.size == 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Add up to 5 numbers to notify in case of an accident.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // ── Input Row with Contact Picker Button ─────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = newContact,
                    onValueChange = { newContact = it; errorMessage = "" },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = contacts.size < 5, // Disable input if max reached
                    trailingIcon = {
                        IconButton(
                            onClick = { contactPickerLauncher.launch(null) },
                            enabled = contacts.size < 5
                        ) {
                            Icon(Icons.Default.ContactPage, "Pick from contacts", tint = if (contacts.size < 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val cleanNum = newContact.trim()
                        when {
                            cleanNum.isBlank() -> errorMessage = "Enter a number"
                            contacts.contains(cleanNum) -> errorMessage = "Number already added"
                            contacts.size >= 5 -> errorMessage = "Max 5 contacts allowed"
                            else -> {
                                contacts.add(cleanNum)
                                newContact = ""
                            }
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    enabled = contacts.size < 5 && newContact.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, "Add")
                }
            }

            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── List of Added Contacts ────────────────────────────────────────
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No contacts added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                contacts.forEach { contact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(contact, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            }
                            IconButton(onClick = {
                                contacts.remove(contact)
                                errorMessage = "" // Clear error if they remove a contact to free up space
                            }) {
                                Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // ── Save Button ───────────────────────────────────────────────────────
        Button(
            onClick = {
                isSaving = true
                dbHelper.saveEmergencyContacts(contacts.toList(), { onSaveComplete() }, {
                    isSaving = false
                    errorMessage = "Failed to save to database."
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = contacts.isNotEmpty() && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Save Contacts", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}