package com.normie69K.v_guard.services

import android.content.Context
import android.telephony.SmsManager
import android.util.Log

class SmsManagerHelper(private val context: Context) {

    fun sendEmergencySms(phoneNumbers: List<String>, lat: Double, lng: Double) {
        try {
            // Modern way to call SmsManager for Android 12+ (API 31+)
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)

            // Format the exact coordinates into a clickable URL
            val mapsLink = "https://maps.google.com/?q=$lat,$lng"

            // The SOS Payload
            val message = "🚨 EMERGENCY ALERT: Karan's vehicle has detected a severe accident! Immediate assistance required. Location: $mapsLink"

            // Dispatch to all saved contacts
            for (number in phoneNumbers) {
                smsManager.sendTextMessage(number, null, message, null, null)
                Log.d("SmsManagerHelper", "SOS payload dispatched to $number")
            }
        } catch (e: Exception) {
            Log.e("SmsManagerHelper", "Critical Failure - SMS not sent: ${e.message}")
        }
    }
}