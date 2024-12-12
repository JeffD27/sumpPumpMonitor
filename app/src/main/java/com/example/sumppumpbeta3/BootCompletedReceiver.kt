package com.example.sumppumpbeta3
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.StrictMode
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.i("bootCompletedReceiver", "Hello World...boot is complete")
        if (intent != null && context != null) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                Log.i("bootCompletedReceiver", "about to startPeriodicWork")

                //val i = Intent().setClassName(pkgName, "RunningService")
                /*
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context!!.startService(i)
                */
                NotificationChannels()
                startPeriodicWork(context)

            }
        }
    }

    private fun startPeriodicWork(context: Context){
        // Create the periodic work request
        Log.i("startPeriodicWork", "Starting")
        val periodicWorkRequest = PeriodicWorkRequestBuilder<CallServerWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(5, TimeUnit.SECONDS)  // Optional initial delay
            .build()

        // Enqueue the periodic work request
        WorkManager.getInstance(context).enqueue(periodicWorkRequest)
        Log.i("startPeriodicWork", "enqued")
    }
}

/*
class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED){
            Log.i("onReceive", "Hello World...boot is complete")


            // Start the new activity
            if (context != null) {

                val runningService: RunningService by RunningService()
                val intent = Intent("RunningService")
                intent.setClass(context, com.example.sumppumpbeta3.RunningService)


                //val intent = Intent(context, MainActivity()::class.java)
                Log.i("newActivity", "starting New activity")
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)

                // Start the new activity
                startActivity(context, intent, null)
                Log.i("newActivity", "Completed New Activity")
            }
        }
    }
}
*/