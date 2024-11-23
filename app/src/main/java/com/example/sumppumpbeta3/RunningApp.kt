package com.example.sumppumpbeta3

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.sumppump3.R
import java.security.AccessController.getContext

class RunningApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("RunningApp","RunningApp.kt...About to run")
       // WorkManager.initialize(this, Configuration.Builder().build())
        createNotificationChannels()
    }
    fun createNotificationChannels(){

        Log.i("createNotifications", Build.VERSION.SDK_INT.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            Log.i("createNotifications", "software requirements met!")
            val mChannelAA = NotificationChannel(
                "Pump Errors/Warnings",
                "Most Urgent Warnings", NotificationManager.IMPORTANCE_HIGH
            )
            mChannelAA.description =
                "e.g. \"Pump is running on no water\" or \"flooding in basement\""

            val mChannelA = NotificationChannel(
                "11111",
                "Pump Errors/Warnings",
                NotificationManager.IMPORTANCE_HIGH
            )

            mChannelA.description = "E.g. pump has run too long"
            val mChannelB = NotificationChannel(
                "22222",
                "Pump Errors/Warnings",
                NotificationManager.IMPORTANCE_HIGH
            )
            mChannelB.description = "E.g. server issue, sensor error"
            val mChannelC =
                NotificationChannel("33333", "General Info", NotificationManager.IMPORTANCE_DEFAULT)
            mChannelC.description = "E.G. Main pump has run"

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.


            notificationManager.createNotificationChannel(mChannelAA)
            notificationManager.createNotificationChannel(mChannelA)
            notificationManager.createNotificationChannel(mChannelB)
            notificationManager.createNotificationChannel(mChannelC)


        }
    }









}