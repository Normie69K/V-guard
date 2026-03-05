package com.normie69K.v_guard.services

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log

class SmsManagerHelper(private val context: Context) {

    companion object {
        private const val TAG = "SmsManagerHelper"
    }

    fun sendEmergencySms(phoneNumbers: List<String>, lat: Double, lng: Double) {
        if (phoneNumbers.isEmpty()) {
            Log.w(TAG, "No emergency contacts configured — SMS not sent")
            return
        }

        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31+ — use context-based factory
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val mapsLink = "https://maps.google.com/?q=$lat,$lng"
            val message  = buildString {
                append("🚨 EMERGENCY ALERT\n")
                append("A vehicle has detected a severe accident!\n")
                append("Immediate assistance may be required.\n\n")
                append("📍 Last known location:\n$mapsLink")
            }

            // SmsManager.sendTextMessage has a ~160-char limit per part.
            // Use sendMultipartTextMessage to avoid truncation.
            val parts = smsManager.divideMessage(message)

            for (number in phoneNumbers) {
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                Log.d(TAG, "SOS dispatched → $number")
            }
        } catch (e: Exception) {
            Log.e(TAG, "SMS dispatch failed: ${e.message}", e)
        }
    }
}
