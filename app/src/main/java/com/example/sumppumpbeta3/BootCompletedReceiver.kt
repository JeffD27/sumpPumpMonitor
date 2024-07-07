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


class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        StrictMode.enableDefaults();
        Log.i("onReceive", "Hello World...boot is complete")
        val pkgName = context!!.packageName
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {

            //val i = Intent().setClassName(pkgName, "RunningService")
            /*
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startService(i)
            */
            Intent(context, RunningService::class.java).also {
                it.action = RunningService.Actions.START.toString()
                context.startService(it)
            }
            Log.i("onReceive", "starting service")
        }
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