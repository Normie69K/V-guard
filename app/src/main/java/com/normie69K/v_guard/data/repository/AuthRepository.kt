package com.normie69K.v_guard.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.normie69K.v_guard.data.models.User
import java.util.concurrent.TimeUnit

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    // Variables for Phone Auth State
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

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

    // ── Forgot Password ───────────────────────────────────────────────────────

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to send reset email") }
    }

    // ── Phone Authentication (OTP) ────────────────────────────────────────────

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onVerificationFailed: (Exception) -> Unit,
        onVerificationCompleted: () -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(credential, onVerificationCompleted, onVerificationFailed)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onVerificationFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    onCodeSent()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val verificationId = storedVerificationId
        if (verificationId == null) {
            onFailure(Exception("Verification ID is null. Request OTP first."))
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneCredential(credential, onSuccess, onFailure)
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                if (result.additionalUserInfo?.isNewUser == true) {
                    val user = result.user
                    if (user != null) {
                        val phone = user.phoneNumber ?: "Unknown Phone"

                        createUserProfile("V-Guard User", phone)
                    }
                }
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    // ── Sign out ──────────────────────────────────────────────────────────────

    fun logout() = auth.signOut()
}