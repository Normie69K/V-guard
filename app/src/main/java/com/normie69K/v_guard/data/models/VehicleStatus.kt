package com.normie69K.v_guard.data.models

import com.google.firebase.database.PropertyName

/**
 * Mirrors the JSON structure written by the ESP32 firmware.
 * Snake-case keys in Firebase → camelCase Kotlin fields via @PropertyName.
 *
 * Expected Firebase schema under  devices/{espId}/status :
 * {
 *   "is_accident": false,
 *   "last_seen":   1700000000000,
 *   "latitude":    19.076,
 *   "longitude":   72.8777,
 *   "wifi_signal": -65
 * }
 */
data class VehicleStatus(
    @get:PropertyName("is_accident") @set:PropertyName("is_accident")
    var isAccident: Boolean = false,

    @get:PropertyName("last_seen") @set:PropertyName("last_seen")
    var lastSeen: Long = 0L,

    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    @get:PropertyName("wifi_signal") @set:PropertyName("wifi_signal")
    var wifiSignal: Int = 0
)
