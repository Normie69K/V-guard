package com.normie69K.v_guard.ui.screens.dashboard

import androidx.compose.ui.unit.Velocity
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

    private val _linkedDevices = MutableStateFlow<List<String>>(emptyList())
    val linkedDevices: StateFlow<List<String>> = _linkedDevices

    private val _selectedEspId = MutableStateFlow("")
    val selectedEspId: StateFlow<String> = _selectedEspId

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
                val status = snapshot.getValue(VehicleStatus::class.java)
                if(status!= null){
                    _vehicleStatus.value = status
                }else{
                    _vehicleStatus.value = VehicleStatus()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)
        vehicleRef      = ref
        vehicleListener = listener
    }

    init {
        loadDevicesAndObserve()
    }

    private fun loadDevicesAndObserve() {
        val uid = auth.currentUser?.uid ?: return

        database.child("users").child(uid).child("linkedDevices")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val devices = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    _linkedDevices.value = devices
                    if(devices.isEmpty()){
                        _selectedEspId.value = ""
                        _vehicleStatus.value = VehicleStatus()
                        vehicleListener?.let { vehicleRef?.removeEventListener(it) }
                    } else if(_selectedEspId.value !in devices){
                        selectDevice(devices.first())
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun selectDevice(espId: String) {
        _selectedEspId.value = espId
        observeVehicle(espId) // Fetch GPS data for the newly selected device
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
