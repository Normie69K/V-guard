package com.normie69K.v_guard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseDbHelper {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun linkDeviceToUser(espId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Save the ESP ID under the user's profile in the database
            database.child("users").child(userId).child("linked_esp").setValue(espId)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception.message ?: "Failed to link device")
                }
        } else {
            onFailure("User not logged in")
        }
    }
}