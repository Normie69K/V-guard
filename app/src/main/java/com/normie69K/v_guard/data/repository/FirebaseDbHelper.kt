package com.normie69K.v_guard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseDbHelper {

    private val auth     = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // ── Link ESP32 device ─────────────────────────────────────────────────────

    fun linkDeviceToUser(
        espId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) { onFailure("User not logged in"); return }

        // Field name must match the User data-class field  →  linkedEspId
        database.child("users").child(userId).child("linkedEspId").setValue(espId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to link device") }
    }

    // ── Emergency contacts ────────────────────────────────────────────────────

    fun saveEmergencyContacts(
        contacts: List<String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) { onFailure("User not logged in"); return }

        database.child("users").child(userId).child("emergencyContacts").setValue(contacts)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to save contacts") }
    }

    fun getEmergencyContacts(onResult: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid ?: run { onResult(emptyList()); return }

        database.child("users").child(userId).child("emergencyContacts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contacts = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    onResult(contacts)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    // ── Fetch linked ESP ID ───────────────────────────────────────────────────

    fun getLinkedEspId(onResult: (String?) -> Unit) {
        val userId = auth.currentUser?.uid ?: run { onResult(null); return }

        database.child("users").child(userId).child("linkedEspId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.getValue(String::class.java))
                }
                override fun onCancelled(error: DatabaseError) { onResult(null) }
            })
    }
}
