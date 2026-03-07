package com.normie69K.v_guard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseDbHelper {

    private val auth     = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // ── Link ESP32 device (Appends to list) ───────────────────────────────────
    fun linkDeviceToUser(
        espId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) { onFailure("User not logged in"); return }

        // Fetch current list first, then append
        getLinkedDevices { currentDevices ->
            if (currentDevices.contains(espId)) {
                onFailure("Device is already linked!")
                return@getLinkedDevices
            }

            val updatedList = currentDevices + espId
            database.child("users").child(userId).child("linkedDevices").setValue(updatedList)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.message ?: "Failed to link device") }
        }
    }

    // ── Fetch all linked devices ──────────────────────────────────────────────
    fun getLinkedDevices(onResult: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid ?: run { onResult(emptyList()); return }

        database.child("users").child(userId).child("linkedDevices")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val devices = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    onResult(devices)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
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
// ── Fetch Trip History ────────────────────────────────────────────────────

    fun getDeviceHistory(espId: String, onResult: (List<com.google.android.gms.maps.model.LatLng>) -> Unit) {
        database.child("devices").child(espId).child("history")
            .orderByKey()
            .limitToLast(500)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val points = mutableListOf<com.google.android.gms.maps.model.LatLng>()
                    for (child in snapshot.children) {
                        // SAFELY handle both Longs and Doubles to prevent DatabaseException crashes
                        val lat = child.child("latitude").value?.toString()?.toDoubleOrNull()
                        val lng = child.child("longitude").value?.toString()?.toDoubleOrNull()

                        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                            points.add(com.google.android.gms.maps.model.LatLng(lat, lng))
                        }
                    }
                    onResult(points)
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }

    // ── Remove ESP32 device ───────────────────────────────────────────────────
    fun removeLinkedDevice(
        espId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) { onFailure("User not logged in"); return }

        getLinkedDevices { currentDevices ->
            val updatedList = currentDevices.filter { it != espId }
            database.child("users").child(userId).child("linkedDevices").setValue(updatedList)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.message ?: "Failed to remove device") }
        }
    }
}
