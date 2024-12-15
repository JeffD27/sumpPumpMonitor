package com.example.sumppumpbeta3

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sumppump3.R

class FullScreenNotificationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Handle null Intent case
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val message = intent.getStringExtra("MESSAGE") ?: "No message available"
        val title = intent.getStringExtra("TITLE") ?: "Urgent Sump Pump Warning"
        val fullScreenIntent = Intent(this, FullScreenNotificationActivity::class.java).apply {
            action = "com.example.sumppumpbeta3.ACTION_FULL_SCREEN"
            putExtra("MESSAGE", message)
        }
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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