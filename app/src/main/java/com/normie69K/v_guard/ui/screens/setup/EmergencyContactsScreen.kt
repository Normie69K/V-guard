package com.normie69K.v_guard.ui.screens.setup

import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    // ── Contact Picker Logic ──────────────────────────────────────────────────
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    // This gets the ID of the contact, then we find the phone number
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
                            // Clean the number (remove spaces/dashes) and add it
                            val cleanNumber = number.replace(Regex("[^0-9+]"), "")
                            if (!contacts.contains(cleanNumber)) {
                                if (contacts.size < 5) contacts.add(cleanNumber)
                                else errorMessage = "Max 5 contacts allowed"
                            }
                        }
                    }
                }
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
            // ── Header ────────────────────────────────────────────────────────
            Icon(Icons.Default.Contacts, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text("Emergency Contacts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
                    trailingIcon = {
                        IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                            Icon(Icons.Default.ContactPage, "Pick from contacts", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (newContact.isNotBlank() && contacts.size < 5) {
                            contacts.add(newContact.trim())
                            newContact = ""
                        }
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Add, "Add")
                }
            }

            // ... (Rest of the contact list and Save button code as before) ...

            Spacer(Modifier.height(20.dp))

            contacts.forEach { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(contact, fontWeight = FontWeight.Medium)
                        IconButton(onClick = { contacts.remove(contact) }) {
                            Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                isSaving = true
                dbHelper.saveEmergencyContacts(contacts.toList(), { onSaveComplete() }, { isSaving = false })
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = contacts.isNotEmpty() && !isSaving
        ) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Save & Continue", fontWeight = FontWeight.Bold)
        }
    }
}