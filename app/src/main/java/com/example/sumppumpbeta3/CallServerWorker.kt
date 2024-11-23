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
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.sumppump3.R
import com.example.sumppump3.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class CallServerWorker(context: Context, workerParameter: WorkerParameters): CoroutineWorker(context, workerParameter) {
    //val mContext = context
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        Log.i("doWork", "starting")



        //val notificationManager = getForegroundInfo()
        /* call server should be here in this class (worker). let's try that...here goes nothing
        Handler(Looper.getMainLooper()).post {
            NotificationsSettings().onCreate(null)
            //this is calling for a notification before notificationSettings oncreate() has run!! this is your problem
            MainActivity().callServer( null, null, notificationManager)
        }
        */
        initiateDeployedVariables() //setup variables to determine if notifications should run...
        callServer()
        resetNotifications()
        // Do your periodic work here
        println("Periodic work is running!")


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
    private val client = OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    @RequiresApi(Build.VERSION_CODES.O)
    fun callServer(){
        Log.i("callServer", "calling Server")

        var firstRun: Boolean = true
        var responseString:String? = null

        val threadServer = Thread {
            //THIS USED TO BE IN A WHILE LOOP...MAYBE IT SHOULD BE?
            Log.i("callServer", "thread starting")

            //val notifications = NotificationsSettings()

            try {
                Log.i("callServer", "trying")
                val parameters = mapOf<String, String>("firstRun" to firstRun.toString())
                Log.i("mainactivity oncreate", "calling get on sumppump.jeffs-handyman.net")
                //important note that getFromServer also applies data. So mainRunning_ will be be altered (if req) for example
                responseString = getFromServer("https://sumppump.jeffs-handyman.net/", parameters, null)


                preServerError = Pair(false, Clock.System.now())

            } catch (e: java.lang.Exception) {
                Log.i("serverError@#$*", Clock.System.now().toString())
                warningVisibilities["serverErrorWarning"] = Pair(1, Clock.System.now())
                Log.i("serverError", "yep that's a server error.")
                e.printStackTrace()
                if (!serverError.first) {
                    serverError = Pair(true, Clock.System.now())
                }

            //now in evaluateResp. we need to kick back data to here in order to run a notificatin

            }
            if (responseString == null){Log.i("callserverworker", "ResponseString is null!!!")}
            else{Log.i("responseString(())", responseString!!)
                Log.i("callserverWorker", "responseString is not null")
                EvaluateResponse().onCreate(responseString!!, activity = null )} //Can't create handler inside thread Thread[Thread-4,5,main] that has not called Looper.prepare()
        }
        threadServer.start()




        println("Hello")
    }
    private fun getFromServer(url: String?, params: Map<String, String>? = null, responseCallback: Unit?): String {
        if (url != null) {
            Log.i("url", url)
        }
        val httpBuilder: HttpUrl.Builder = url!!.toHttpUrlOrNull()!!.newBuilder()
        Log.i("in Get", "starting get")

        if (params != null) {
            for ((key, value) in params) {
                if (key != null) {
                    Log.i("in Get: key", key)
                    Log.i("in Get: value", value)
                    httpBuilder.addQueryParameter(key, value)
                }
            }
        }
        var responseString: String? = "No Response"
        val request: Request = Request.Builder().url(httpBuilder.build()).build()
        Log.i("request", request.toString())

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")


            responseString = response.body?.string()

            if (responseString != null) {
                Log.i("response string", responseString!!)
            }

        }
        if (responseString != null){ return responseString as String}
        else(return "ERROR: no response")
    }
    private fun initiateDeployedVariables(){  //check if initialized and if they aren't initialize them  //yes there are 2 identical functions...see NotificationWorker
        val lateClass = LateClass()
        if (!lateClass.isNotificationServerErrorDeployedInitialized()) {
            notificationServerErrorDeployed = Pair(false, Clock.System.now())
        }
        if (!lateClass.isNotificationWaterLevelSensorErrorDeployedInitialized()) {
            notificationWaterLevelSensorErrorDeployed = Pair(false, Clock.System.now())
        }
        if (!lateClass.isNotificationACPowerDeployedInitialized()) {
            notificationACPowerDeployed = Pair(false, Clock.System.now())
        }
        if (!lateClass.isNotificationWaterLevelSensorErrorBDeployedInitialized()) {
            notificationWaterLevelSensorErrorBDeployed = Pair(false, Clock.System.now())
        }
        if (!lateClass.isNotificationHighWaterDeployedInitialized()) {
            notificationHighWaterDeployed = Pair(false, Clock.System.now())
        }
        if (!lateClass.isNotificationMainRunWarnDeployedInitialized()) {
            notificationMainRunWarnDeployed = Pair( false, Clock.System.now())
        }
        if (!lateClass.isNotificationBackupRanInitialized()) {
            notificationBackupRan = Pair(false, Clock.System.now())
        }
        if (!lateClass.isNotificationWaterTooLowInitialized()) {
            notificationWaterTooLow = Pair( false, Clock.System.now())
        }
        if (!lateClass.isNotificationBattery12LowInitialized()) {
            notificationBattery12Low = Pair(false, Clock.System.now())
        }
        if (!lateClass.isNotificationNoPumpControlInitialized()) {
            notificationNoPumpControl = Pair(false, Clock.System.now())
        }
    }
    fun resetNotifications() {
        //this should run as often as the server is called
        val lateClass = LateClass()
        if (lateClass.isNotificationServerErrorDeployedInitialized()) {
            val (deployed, timeDeployed) = notificationServerErrorDeployed
            Log.i("resetNotifications() server", timeDeployed.toString())
            val timeDif = (Clock.System.now() - timeDeployed)
            Log.i("timeDIFf_ResetNotifications", timeDif.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationServerErrorMuteDuration) {
                notificationServerErrorDeployed = Pair(false, Clock.System.now())
            }
        }



        if (lateClass.isNotificationWaterLevelSensorErrorDeployedInitialized()) { //this notification was never tested
            val (deployed, timeDeployed) = notificationWaterLevelSensorErrorDeployed
            Log.i("resetNotifications() wl sensor", timeDeployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationWaterLevelSensorErrorMuteDuration) {
                notificationWaterLevelSensorErrorDeployed = Pair(false, Clock.System.now())
            }
        }
        if (lateClass.isNotificationWaterLevelSensorErrorBDeployedInitialized()) { //this notification was never tested
            val (deployed, timeDeployed) = notificationWaterLevelSensorErrorDeployed
            Log.i("resetNotifications() wl sensor", timeDeployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationWaterLevelSensorErrorMuteDuration) {
                notificationWaterLevelSensorErrorBDeployed = Pair(false, Clock.System.now())
            }
        }
        if (lateClass.isNotificationACPowerDeployedInitialized()) {
            val (deployed, timeDeployed) = notificationACPowerDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationACPowerMuteDuration) {
                notificationACPowerDeployed = Pair(false, Clock.System.now())
            }
        }
        if (lateClass.isNotificationHighWaterDeployedInitialized()) {
            val (deployed, timeDeployed) = notificationHighWaterDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationHighWaterMuteDuration) {
                notificationHighWaterDeployed = Pair(false, Clock.System.now())
            }
        }

        if (lateClass.isNotificationMainRunWarnDeployedInitialized()) {
            val (deployed, timeDeployed) = notificationMainRunWarnDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationMainRunWarnMuteDuration) {
                notificationMainRunWarnDeployed = Pair(false, Clock.System.now())
            }
        }

        if (lateClass.isNotificationBackupRanInitialized()) {
            val (deployed, timeDeployed) = notificationBackupRan
            if (deployed && (Clock.System.now() - timeDeployed) > notificationBackupRanMuteDuration) {
                notificationBackupRan = Pair(false, Clock.System.now())
            }
        }

        if (lateClass.isNotificationWaterTooLowInitialized()) {
            val (deployed, timeDeployed) = notificationWaterTooLow
            if (deployed && (Clock.System.now() - timeDeployed) > notificationWaterTooLowMuteDuration) {
                notificationWaterTooLow = Pair(false, Clock.System.now())
            }
        }
        if (lateClass.isNotificationBattery12LowInitialized()) {
            val (deployed, timeDeployed) = notificationBattery12Low
            if (deployed && (Clock.System.now() - timeDeployed) > notificationBattery12LowMuteDuration) {
                notificationBattery12Low = Pair(false, Clock.System.now())
            }
        }

        if (lateClass.isNotificationNoPumpControlInitialized()) {
            val (deployed, timeDeployed) = notificationNoPumpControl
            if (deployed && (Clock.System.now() - timeDeployed) > notificationNoPumpControlMuteDuration) {
                notificationNoPumpControl = Pair(false, Clock.System.now())
            }
        }


    }
}