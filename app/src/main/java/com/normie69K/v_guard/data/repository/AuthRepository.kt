package com.normie69K.v_guard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.normie69K.v_guard.data.models.User

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Get current User ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Create a new user profile in the database after registration
    fun createUserProfile(name: String, email: String) {
        val uid = auth.currentUser?.uid ?: return
        val user = User(uid = uid, name = name, email = email)
        db.child(uid).setValue(user)
    }

    // Sign Out
    fun logout() {
        auth.signOut()
    }
}