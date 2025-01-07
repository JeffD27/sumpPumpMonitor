package com.example.sumppumpbeta3

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.sumppump3.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


val notificationStringToDeployed = LinkedHashMap<String, Pair<Boolean, Instant>>()

//to calculate if notification needs to be reset <if deployed, time deployed>
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    val context = context
    override fun doWork(): Result {
        initiateDeployedVariables()

        notificationStringToDeployed["serverError"] = notificationServerErrorDeployed
        notificationStringToDeployed["sensorError"] = notificationWaterLevelSensorErrorDeployed
        notificationStringToDeployed["noPower"] = notificationACPowerDeployed
        notificationStringToDeployed["highWater"] = notificationHighWaterDeployed
        notificationStringToDeployed["mainRunTime"] = notificationMainRunWarnDeployed
        notificationStringToDeployed["backupRun"] = notificationBackupRan
        notificationStringToDeployed["noWater"] = notificationWaterTooLow
        notificationStringToDeployed["lowBattery12"] = notificationBattery12Low
        notificationStringToDeployed["noPumpControl"] = notificationNoPumpControl
        notificationStringToDeployed["mainRunning"] = notificationMainRunning



        Log.d("NotificationsSettings", "onCreate")

        // Get the string input from WorkManager
        val title = inputData.getString("title") ?: "Sump Pump Monitor"
        val message = inputData.getString("message") ?: "No message provided"
        val channelID = inputData.getString("channelID") ?: "General Info"
        val notificationString = inputData.getString("notificationString") ?: "noStringProvided"
        val priority = inputData.getString("priority") ?: "High"
        val notifid = inputData.getString("notifid") ?: "11111"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //setupChannels(notificationManager)




        deployNotification(notificationString, title, message, priority , channelID, notifid, notificationManager)

        return Result.success()

    }
    private fun initiateDeployedVariables(){  //check if initialized and if they aren't initialize them //yes there are 2 identical functions...see CallServerWorker
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
        if (!lateClass.isNotificationMainRunningInitialized()) {
            notificationMainRunning = Pair(false, Clock.System.now())
        }
    }
    /*
    private fun setupChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            Log.d("createNotifications", "software requirements met!")

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


            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.


            notificationManager.createNotificationChannel(mChannelA)
            notificationManager.createNotificationChannel(mChannelB)
            notificationManager.createNotificationChannel(mChannelC)


        }
    }*/

    private fun notificationBuilder(
        context: Context,
        title: String,
        content: String,
        priority: String,
        CHANNEL_ID: String,
        notifid: String,
        notificationManager: NotificationManager
    ) { //priority: high default low
        Log.d("notificationBuilder()", "starting notification builder")
        Log.d("channel_ID", CHANNEL_ID)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val intent = Intent(context, Notification::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setContentIntent(pendingIntent)

        Log.d("notificationBuilder", content)
        builder.setSmallIcon(R.drawable.floodedhouse)
        if (priority == "high") {
            builder.priority = NotificationCompat.PRIORITY_HIGH
        } else if (priority == "default") {
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
        } else if (priority == "low") {
            builder.priority = NotificationCompat.PRIORITY_LOW
        }
        else if (priority == "min") { //no sound
            builder.priority = NotificationCompat.PRIORITY_MIN
        }
        //return builder here
        notificationManager.notify(notifid.toInt(), builder.build())
    }
    private fun callFullScreenNotification(context: Context, title: String, message: String, notifid: String): Boolean {
       // if(Clock.System.now() - fullScreenDeployedTime < 5.seconds){
         //   Log.d("callFullScreenNotification", "self muting")
          //  return false
        //}
        Log.d("start fullScreen", message)
        val appContext = context.applicationContext
        fullScreenDeployedTime = Clock.System.now()
        val serviceIntent = Intent(applicationContext, FullScreenNotificationService::class.java)

        // You can pass any extra data to the service here if needed
        serviceIntent.putExtra("MESSAGE", message)
        serviceIntent.putExtra("TITLE", title)

        // Start the service in the foreground
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
       /* val fullScreenIntent = Intent(appContext, FullScreenNotificationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("MESSAGE", "Backup Pump has run! Please Check Main Pump")}*/
        /*val fullScreenPendingIntent = PendingIntent.getActivity(
            appContext, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("NotificationWorker", "will this run?1")*/
        /*
        val notification = NotificationCompat.Builder(context, context.getString(R.string.fullScreenChannel))
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.floodedhouse)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL) // Use CATEGORY_CALL for incoming call-style notifications
            .setFullScreenIntent(fullScreenPendingIntent, true) // This triggers the full-screen Activity
            //.setSilent(true) //from stackoverflow...0 votes on it but worth a try because Idk what else to do
            .build()

        with(NotificationManagerCompat.from(appContext)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("callFullScreen", "permission DENIED")
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false
            }
            Log.d("fullScreenNotification", "calling notify")
            notify(2020, notification)


        }*/
        Log.d("NotificationWorker", message)
        Log.d("callFullScreen", "done with calling fullscreen notification")
        return true
    }
    private fun deployNotification( notificationString: String, title: String, message: String, priority: String, CHANNEL_ID: String, notifid: String, notificationManager: NotificationManager,) {

        Log.d("notifstring in depl", notificationString)
        Log.d("notifstring in depl",Clock.System.now().toString())
        var deployed: Boolean = false
        var timeDeployed = Clock.System.now()

        val deployedPair =
            notificationStringToDeployed[notificationString]!! //returns a pair //causes null pointer exception
        Log.d("deployNotification", CHANNEL_ID)
        deployed = deployedPair.first
        timeDeployed = deployedPair.second
        Log.d("time and deploy", timeDeployed.toString())
        Log.d("time and deploy", deployed.toString())



        if (!deployed) {
            Log.d("deployNotification", "starting notification for $notificationString")

            if (CHANNEL_ID == context.getString(R.string.fullScreenChannel)){
                Log.d("FullScreenCHannel", "fullScreenChannel")

                Log.d("notifywork channelIDCheck", notificationString)
                if (!callFullScreenNotification(context, title, message, notifid)){
                    return //makes sure we do not set ErrorDeployed since nothing was deployed
                }
            }
            else{
                notificationBuilder(
                    applicationContext,
                    title,
                    message,
                    priority,
                    CHANNEL_ID,
                    notifid,
                    notificationManager
                )
            }


            when (notificationString) {
                "serverError" -> notificationServerErrorDeployed = Pair(true, Clock.System.now())
                "sensorError" -> notificationWaterLevelSensorErrorDeployed = Pair (true, Clock.System.now())
                "noPower" -> notificationACPowerDeployed = Pair(true, Clock.System.now())
                "highWater" -> notificationHighWaterDeployed = Pair(true, Clock.System.now())
                "mainRunTime" -> notificationMainRunWarnDeployed = Pair(true, Clock.System.now())
                "backupRun" -> notificationBackupRan = Pair(true, Clock.System.now())
                "noWater" -> notificationWaterTooLow = Pair(true, Clock.System.now())
                "lowBattery12" -> notificationBattery12Low = Pair(true, Clock.System.now())
                "noPumpControl" -> notificationNoPumpControl = Pair(true, Clock.System.now())
                "mainRunning" -> notificationMainRunning = Pair(true, Clock.System.now())
            }



        }


    }





}








