package com.normie69K.v_guard.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AccidentMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID  = "VGuard_Monitor"
        private const val NOTIF_ID    = 1001
        private const val TAG         = "AccidentMonitorService"
    }

    private var accidentListener: ValueEventListener? = null
    private var accidentRef: DatabaseReference?       = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        fetchEspIdAndStartListening()
    }

    // ── Step 1: read the user's linked ESP ID from Firebase ──────────────────

    private fun fetchEspIdAndStartListening() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No authenticated user — service cannot monitor")
            stopSelf()
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("users/$uid/linkedEspId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val espId = snapshot.getValue(String::class.java)
                    if (!espId.isNullOrBlank()) {
                        listenForAccident(espId)
                    } else {
                        Log.w(TAG, "No ESP ID linked to account — stopping service")
                        stopSelf()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read ESP ID: ${error.message}")
                    stopSelf()
                }
            })
    }

    // ── Step 2: attach a persistent listener to the crash flag ───────────────

    private fun listenForAccident(espId: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("devices/$espId/status/is_accident")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hasCrashed = snapshot.getValue(Boolean::class.java) ?: false
                if (hasCrashed) {
                    Log.d(TAG, "Crash flag detected for $espId — launching alert UI")
                    launchAlertScreen()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Listener cancelled: ${error.message}")
            }
        }

        ref.addValueEventListener(listener)
        accidentRef      = ref
        accidentListener = listener
    }

    // ── Launch the alert screen even from the background ────────────────────

    private fun launchAlertScreen() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("CRASH_DETECTED", true)
        }
        startActivity(intent)
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("V-Guard Active")
            .setContentText("Monitoring vehicle status in real-time…")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vehicle Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        accidentListener?.let { accidentRef?.removeEventListener(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
