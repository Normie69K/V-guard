package com.normie69K.v_guard.data.models

data class VehicleStatus(
    val isAccident: Boolean = false,
    val lastSeen: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val wifiSignal: Int = 0
)