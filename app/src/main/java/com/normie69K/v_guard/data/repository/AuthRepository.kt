package com.normie69K.v_guard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.normie69K.v_guard.data.models.User

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    // ── Auth state ────────────────────────────────────────────────────────────

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ── Sign-in with email & password ─────────────────────────────────────────

    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Sign-in failed") }
    }

    // ── Create account (register) ─────────────────────────────────────────────

    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                createUserProfile(name, email)
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Registration failed") }
    }

    // ── Write user profile to Realtime DB ─────────────────────────────────────

    fun createUserProfile(name: String, email: String) {
        val uid = auth.currentUser?.uid ?: return
        val user = User(uid = uid, name = name, email = email)
        usersRef.child(uid).setValue(user)
    }

    // ── Sign-in with Google ───────────────────────────────────────────────────

    fun signInWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                if (result.additionalUserInfo?.isNewUser == true) {
                    val user = result.user
                    if (user != null) {
                        val name = user.displayName ?: "V-Guard User"
                        val email = user.email ?: ""
                        createUserProfile(name, email)
                    }
                }
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Google Sign-in failed") }
    }

    // ── Sign out ──────────────────────────────────────────────────────────────

    fun logout() = auth.signOut()
}