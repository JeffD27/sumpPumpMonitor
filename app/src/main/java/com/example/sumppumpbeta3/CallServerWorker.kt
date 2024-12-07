package com.example.sumppumpbeta3

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.sumppump3.R
import com.example.sumppump3.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class CallServerWorker(context: Context, workerParameter: WorkerParameters): CoroutineWorker(context, workerParameter) {
    //val mContext = context
    @RequiresApi(Build.VERSION_CODES.O)

    override suspend fun doWork(): Result {
        Log.i("doWork", "starting")

        val context = applicationContext
        LoopHandler().run(context)//doesn't call server, but this preps the variables for the loop

        //initiateDeployedVariables() handled in loopHandler
        //setup variables to determine if notifications should run...
        Log.i("callServer", "calling Server")



        val responseString = CallServer().run()

        if (responseString == null){
            Log.i("callserverworker", "ResponseString is null!!!")
            if (!serverError.first) {
                serverError = Pair( true, Clock.System.now())}
            EvaluateResponse().checkServerError(context)
        }
        else{
            Log.i("responseString(())", responseString!!)
            Log.i("callserverWorker", "responseString is not null")
            EvaluateResponse().onCreate(context, responseString!!,null )
        }


        // Do your periodic work here
        println("doWork is running")


        return Result.success()


    }

/*
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1114, setupRunningNotification()
        )
    }

    fun getNotificationManager(): NotificationManager {
        return mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRunningNotification(): Notification {
        Log.i("setupRunningNotif", "starting running Notif")



        val foregroundNotificationChannel: NotificationChannel =
            NotificationChannel("11113", "foreground", NotificationManager.IMPORTANCE_HIGH)

        val notificationManager = getNotificationManager()
        notificationManager.createNotificationChannel(foregroundNotificationChannel)
        val notification =  notificationBuilder("Sump Pump Monitor", "Sump Pump Monitor is Monitoring", "High", "11113", "11114", notificationManager )
        return notification

    }*/
    /*
    private fun notificationBuilder(
        title: String,
        content: String,
        priority: String,
        channelid: String,
        notifid: String,
        notificationManager: NotificationManager
    ): Notification { //priority: high default low
        Log.i("notificationBuilder()", "starting notification builder")
        val builder = NotificationCompat.Builder(mContext, channelid)
        val intent = Intent(mContext, RunningService::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
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
        Log.i("runningService", "complete")
        return builder.build()
    }
*/

}