package com.example.sumppumpbeta3

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sumppump3.R

//this is a service that starts when a full screen notification is needed.
class FullScreenNotificationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val message = intent?.getStringExtra("MESSAGE") ?: "No message available"
        val title = intent?.getStringExtra("TITLE")?: "Urgent Sump Pump Warning"
        val fullScreenIntent = Intent(this, FullScreenNotificationActivity::class.java).apply {
            putExtra("MESSAGE", message)
        }

// Create a PendingIntent that wraps the intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        //just the notification to display the realy notification
        //go to FullScreenNotification.kt to see the real notification
        val notification = NotificationCompat.Builder(this, "foregroundService")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.floodedhouse)
            .setFullScreenIntent(pendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}