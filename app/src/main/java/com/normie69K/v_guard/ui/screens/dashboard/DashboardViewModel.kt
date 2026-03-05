package com.normie69K.v_guard.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.normie69K.v_guard.data.models.VehicleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth     = FirebaseAuth.getInstance()

    private val _vehicleStatus = MutableStateFlow(VehicleStatus())
    val vehicleStatus: StateFlow<VehicleStatus> = _vehicleStatus

    private val _espId = MutableStateFlow("")
    val espId: StateFlow<String> = _espId

    private var vehicleListener: ValueEventListener? = null
    private var vehicleRef: DatabaseReference?       = null

    init {
        loadEspIdAndObserve()
    }

    // ── Auto-load the linked ESP ID from the authenticated user's profile ─────

    private fun loadEspIdAndObserve() {
        val uid = auth.currentUser?.uid ?: return

        database.child("users").child(uid).child("linkedEspId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val id = snapshot.getValue(String::class.java) ?: return
                    if (id.isNotBlank() && id != _espId.value) {
                        _espId.value = id
                        observeVehicle(id)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ── Attach a live listener to the device's status node ───────────────────

    fun observeVehicle(espId: String) {
        // Remove any existing listener before attaching a new one
        vehicleListener?.let { vehicleRef?.removeEventListener(it) }

        val ref = database.child("devices").child(espId).child("status")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(VehicleStatus::class.java)?.let {
                    _vehicleStatus.value = it
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)
        vehicleRef      = ref
        vehicleListener = listener
    }

    // ── Reset the crash flag in Firebase so the alert doesn't re-trigger ─────

    fun clearCrashFlag() {
        val espId = _espId.value.ifBlank { return }
        database.child("devices").child(espId).child("status").child("is_accident")
            .setValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        vehicleListener?.let { vehicleRef?.removeEventListener(it) }
    }
}
