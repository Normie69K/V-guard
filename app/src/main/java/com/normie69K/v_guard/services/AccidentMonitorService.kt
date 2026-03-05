package com.normie69K.v_guard.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccidentMonitorService : Service() {

    private val CHANNEL_ID = "GuardianDrive_Monitor"
    // TODO: In production, retrieve this from the user's profile
    private val espMacAddress = "ESP32_MAC_123"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        listenForAccidents()
    }

    private fun listenForAccidents() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("devices/$espMacAddress/status/is_accident")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hasCrashed = snapshot.getValue(Boolean::class.java) ?: false
                if (hasCrashed) {
                    triggerEmergencyUI()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read error
            }
        })
    }

    private fun triggerEmergencyUI() {
        // This launches the app immediately, even from the background
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("CRASH_DETECTED", true)
        }
        startActivity(intent)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GuardianDrive Active")
            .setContentText("Monitoring vehicle status in real-time...")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon later
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vehicle Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Ensures the service restarts if the system kills it
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't need bound services for this
    }
}