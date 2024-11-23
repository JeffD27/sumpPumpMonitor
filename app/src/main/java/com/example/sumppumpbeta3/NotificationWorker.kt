package com.example.sumppumpbeta3

import android.app.Activity
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
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


val notificationStringToDeployed = LinkedHashMap<String, Pair<Boolean, Instant>>()

//to calculate if notification needs to be reset <if deployed, time deployed>
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {


        Log.i("NotificationsSettings", "onCreate")
        notificationStringToDeployed["serverError"] = notificationServerErrorDeployed
        notificationStringToDeployed["sensorError"] = notificationWaterLevelSensorErrorDeployed
        notificationStringToDeployed["noPower"] = notificationACPowerDeployed
        notificationStringToDeployed["highWater"] = notificationHighWaterDeployed
        notificationStringToDeployed["mainRunTime"] = notificationMainRunWarnDeployed
        notificationStringToDeployed["backupRun"] = notificationBackupRan
        notificationStringToDeployed["noWater"] = notificationWaterTooLow
        notificationStringToDeployed["lowBattery12"] = notificationBattery12Low
        notificationStringToDeployed["noPumpControl"] = notificationNoPumpControl
        // Get the string input from WorkManager
        val title = inputData.getString("title") ?: "Sump Pump Monitor"
        val message = inputData.getString("message") ?: "No message provided"
        val channelID = inputData.getString("channelID") ?: "General Info"
        val notificationString = inputData.getString("notificationString") ?: "noStringProvide"
        val priority = inputData.getString("priority") ?: "High"
        val notifid = inputData.getString("notifid") ?: "11111"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setupChannels(notificationManager)
        initiateDeployedVariables()




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
    }

    private fun setupChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            Log.i("createNotifications", "software requirements met!")

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
    }

    private fun notificationBuilder(
        context: Context,
        title: String,
        content: String,
        priority: String,
        CHANNEL_ID: String,
        notifid: String,
        notificationManager: NotificationManager
    ) { //priority: high default low
        Log.i("notificationBuilder()", "starting notification builder")
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val intent = Intent(context, Notification::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setContentIntent(pendingIntent)

        Log.i("notificationBuilder", content)
        builder.setSmallIcon(R.drawable.floodedhouse)
        if (priority == "high") {
            builder.priority = NotificationCompat.PRIORITY_HIGH
        } else if (priority == "default") {
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
        } else if (priority == "low") {
            builder.priority = NotificationCompat.PRIORITY_LOW
        }
        //return builder here
        notificationManager.notify(notifid.toInt(), builder.build())
    }

    private fun deployNotification(
        notificationString: String,
        title: String,
        message: String,
        priority: String,
        CHANNEL_ID: String,
        notifid: String,
        notificationManager: NotificationManager
    ) {

        Log.i("notifstring in depl", notificationString)
        Log.i(
            "notifstring in depl",
            Clock.System.now().toString()
        )

        val deployedPair =
            notificationStringToDeployed[notificationString]!! //returns a pair //causes null pointer exception
        Log.i("deployNotification", "getting notif manager")
        val (deployed, timeDeployed) = deployedPair
        Log.i("time and deploy", timeDeployed.toString())
        Log.i("time and deploy", deployed.toString())
        if (!deployed) {
            Log.i("deployNotification", "starting notification for $notificationString")
            notificationBuilder(
                applicationContext,
                title,
                message,
                priority,
                CHANNEL_ID,
                notifid,
                notificationManager
            )

            when (notificationString) {
                "serverError" -> notificationServerErrorDeployed = Pair(true, Clock.System.now())
                "sensorError" -> notificationWaterLevelSensorErrorDeployed = Pair(
                    true,
                    Clock.System.now()
                )

                "noPower" -> notificationACPowerDeployed = Pair(true, Clock.System.now())
                "highWater" -> notificationHighWaterDeployed = Pair(true, Clock.System.now())
                "mainRunTime" -> notificationMainRunWarnDeployed = Pair(true, Clock.System.now())
                "backupRun" -> notificationBackupRan = Pair(true, Clock.System.now())
                "noWater" -> notificationWaterTooLow = Pair(true, Clock.System.now())
                "lowBattery12" -> notificationBattery12Low = Pair(true, Clock.System.now())
                "noPumpControl" -> notificationNoPumpControl = Pair(true, Clock.System.now())
            }


        }


    }


    fun setNotificationMuteTimes(){
        val intDurationDict = LinkedHashMap<Int, kotlin.time.Duration>()
        intDurationDict[5] = 5.minutes
        intDurationDict[10] = 10.minutes
        intDurationDict[15] = 15.minutes
        intDurationDict[30] = 30.minutes
        intDurationDict[1] = 1.hours
        intDurationDict[2] = 2.hours
        intDurationDict[4] = 4.hours
        intDurationDict[8] = 8.hours
        intDurationDict[12] = 12.hours
        intDurationDict[24] = 24.hours
        intDurationDict[48] = 48.hours


        runBlocking {
            for (string in notificationStrings) {


                val durationInt = updateNotificationMuteTimes(string)!! //initiated in settings

                //Log.i("notifyString", string)

                // Log.i("durationInt", durationInt.toString())

                when (string) {
                    "serverError" -> {
                        notificationServerErrorMuteDuration = intDurationDict[durationInt]!!
                    }

                    "sensorError" -> {
                        notificationWaterLevelSensorErrorMuteDuration =
                            intDurationDict[durationInt]!!
                    }

                    "noPower" -> {
                        notificationACPowerMuteDuration = intDurationDict[durationInt]!!
                    }

                    "highWater" -> {
                        notificationHighWaterMuteDuration = intDurationDict[durationInt]!!
                    }

                    "mainRunTime" -> {
                        notificationMainRunWarnMuteDuration = intDurationDict[durationInt]!!
                    }

                    "backupRun" -> {
                        notificationBackupRanMuteDuration = intDurationDict[durationInt]!!
                    }

                    "noWater" -> {
                        notificationWaterTooLowMuteDuration = intDurationDict[durationInt]!!
                    }

                    "lowBattery12" -> {
                        notificationBattery12LowMuteDuration = intDurationDict[durationInt]!!
                    }

                    "noPumpControl" -> {
                        notificationNoPumpControlMuteDuration = intDurationDict[durationInt]!!

                    }


                }
            }
        }
    }
    suspend fun updateNotificationMuteTimes(notification:String): Int? {
        Log.i("updateNotifcationMuteTimes", notification)

        val durationIntDict = LinkedHashMap<kotlin.time.Duration, Int>()
        durationIntDict[5.minutes] = 5
        durationIntDict[10.minutes] = 10
        durationIntDict[15.minutes] = 15
        durationIntDict[30.minutes] = 30
        durationIntDict[1.hours] = 1
        durationIntDict[2.hours] = 2
        durationIntDict[4.hours] = 4
        durationIntDict[8.hours] = 8
        durationIntDict[12.hours] = 12
        durationIntDict[24.hours] = 24
        durationIntDict[48.hours] = 24
        durationIntDict[1.days] = 24
        durationIntDict[2.days] = 48

        if (defaultMuteTimes.isEmpty()) {

            defaultMuteTimes["serverError"] = 1.days
            defaultMuteTimes["sensorError"] = 1.days
            defaultMuteTimes["noPower"] = 1.hours
            defaultMuteTimes["highWater"] = 15.minutes
            defaultMuteTimes["mainRunTime"] = 10.minutes
            defaultMuteTimes["backupRun"] = 10.minutes
            defaultMuteTimes["noWater"] = 10.minutes
            defaultMuteTimes["lowBattery12"] = 1.days
            defaultMuteTimes["noPumpControl"] = 12.hours
        }
        val defaultMuteTime = defaultMuteTimes[notification]

        Log.i("defaultMuteTime", defaultMuteTime.toString())

        val defaultMuteInt = durationIntDict[defaultMuteTime]!!
        val prefKey = intPreferencesKey(notification)
        /*
       val constraints = Constraints.Builder()
           //.setRequiredNetworkType(NetworkType.UNMETERED) //for example you can have the service run if network is unmetered
           //.setRequiresCharging(true)
           .build()

      val myWorkRequest: WorkRequest =
           OneTimeWorkRequestBuilder<MyWork>()
               .setConstraints(constraints)
               .build()*/

        val apContext = applicationContext
//problem is here
        val notificationSettingsFlow: Flow<Int> = apContext.dataStore.data //read data in saved data store
            .map { settings: Preferences  ->
                // No type safety.
                settings[prefKey] ?: defaultMuteInt   //this sets the value (in exampleCounterFlow not datastore) if null
            }

        //Log.i("readDurationData", exampleCounterFlow.first().toString())
        Log.i("valueinRead", notificationSettingsFlow.first().toString())
        return notificationSettingsFlow.first()
    }

}








