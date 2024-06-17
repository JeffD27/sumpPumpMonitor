package com.example.sumppumpbeta3


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.sumppump3.R

class RunningService: Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null

    }
    //this gets triggered whenever an android component sends an intent to the running service
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("RunningService","onStartCommand in Running Service")

        when(intent?.action){
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
            Actions.CALLSERVER.toString() -> MainActivity().callServer( null, null)
        }

        return super.onStartCommand(intent, flags, startId)
    }
    //this space is an activity area...just without viewing. you can override oncreate and other functions
    private fun start(){
        Log.i("RunningService","instart in Running Service")
        val channel = NotificationChannel(
            "11113",
            "Foreground",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "PennSkanvTic channel for foreground service notification"



        val foregroundNotificationChannel: NotificationChannel = NotificationChannel("11113","foreground", IMPORTANCE_HIGH )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notification =  notificationBuilder("Sump Pump Monitor", "Sump Pump Monitor is Monitoring", "High", "11113", "11114", notificationManager )

        startForeground(11114, notification)
    }
    private fun notificationBuilder(
        title: String,
        content: String,
        priority: String,
        channelid: String,
        notifid: String,
        notificationManager: NotificationManager
    ): Notification { //priority: high default low
        Log.i("notificationBuilder()", "starting notification builder")
        val builder = NotificationCompat.Builder(this, channelid)
        val intent = Intent(this, RunningService::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setContentIntent(pendingIntent)
        builder.setSmallIcon(R.drawable.floodedhouse)
        Log.i("notificationBuilder", title)
        Log.i("notificationBuilder", content)
        builder.setSmallIcon(R.drawable.floodedhouse)
        if (priority == "high") {
            builder.priority = NotificationCompat.PRIORITY_HIGH
        } else if (priority == "default") {
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
        } else if (priority == "low") {
            builder.priority = NotificationCompat.PRIORITY_LOW
        }
        return builder.build()
    }


    enum class Actions{
        START, STOP, CALLSERVER
    }


}