package com.example.sumppumpbeta3

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class NotificationChannels: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("RunningApp", "notificationChannels.kt...About to run")
        // WorkManager.initialize(this, Configuration.Builder().build())
        LateClass()
        createNotificationChannels()
    }

    //default priority: sensorError, serverError,
    // Low priority: low battery12,
    // high: noPower, highWater, mainRunTime, BackupRun
    private fun createNotificationChannels() {

        Log.i("createNotifications", Build.VERSION.SDK_INT.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            Log.i("createNotifications", "software requirements met!")
            val mChannelAA = NotificationChannel(
                "00000",
                "Most Urgent Warnings", NotificationManager.IMPORTANCE_HIGH
            )
            mChannelAA.description =
                "e.g. \"Pump is running on no water\" or \"flooding in basement\""

            val mChannelA = NotificationChannel(
                "19999",
                "sensorError",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            mChannelA.description = "Water level sensor error"

            val mChannelB = NotificationChannel(
                "22222",
                "serverError",
                NotificationManager.IMPORTANCE_LOW
            )
            mChannelB.description = "E.g. server issue, sensor error"
            val mChannelC =
                NotificationChannel(
                    "33333",
                    "General Info",
                    NotificationManager.IMPORTANCE_LOW
                )
            mChannelC.description = "E.G. Main pump has run"
            val mChannelAlpha =
                NotificationChannel(
                    "000111000",
                    "Full Screen Urgent",
                    NotificationManager.IMPORTANCE_HIGH
                )
            mChannelAlpha.description = "Urgent Attention Needed"


            val foregroundServiceNotification =
                NotificationChannel(
                    "foregroundService",
                    "Full Screen Urgent",
                    NotificationManager.IMPORTANCE_HIGH
                )
            foregroundServiceNotification.description =
                "Urgent Notification!" //this is not the actual notification. this is notification for the foreground service to show the fullscreen notification (000111000)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.


            notificationManager.createNotificationChannel(mChannelAA)
            notificationManager.createNotificationChannel(mChannelA)
            notificationManager.createNotificationChannel(mChannelB)
            notificationManager.createNotificationChannel(mChannelC)
            notificationManager.createNotificationChannel(mChannelAlpha)
            notificationManager.createNotificationChannel(foregroundServiceNotification)
            Log.i("NotificationChannels", "channels built!")


        }
    }

}