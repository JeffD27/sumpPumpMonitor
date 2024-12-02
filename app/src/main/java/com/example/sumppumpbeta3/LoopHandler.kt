package com.example.sumppumpbeta3

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class LoopHandler {

    @RequiresApi(Build.VERSION_CODES.O)
    fun run(context: Context){
        initiateDeployedVariables()
        setNotificationMuteTimes(context)
        resetNotifications()

    }

    private val client = OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    @RequiresApi(Build.VERSION_CODES.O)


    fun resetNotifications() {
        //this should run as often as the server is called
        val lateClass = LateClass()
        if (lateClass.isNotificationServerErrorDeployedInitialized()) {
            val (deployed, timeDeployed) = notificationServerErrorDeployed
            Log.i("resetNotifications() server", timeDeployed.toString())
            val timeDif = (Clock.System.now() - timeDeployed)
            Log.i("timeDIFf_ResetNotifications", timeDif.toString())
            if (deployed && timeDif > notificationServerErrorMuteDuration) {
                notificationServerErrorDeployed = Pair(false, Clock.System.now())
            }
        }
    }

    private fun initiateDeployedVariables(){  //check if initialized and if they aren't initialize them  //yes there are 2 identical functions...see NotificationWorker
        val lateClass = LateClass()
        /*
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
    }*/


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

    private fun setNotificationMuteTimes(context: Context){
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



                val durationInt = updateNotificationMuteTimes(string, context)!! //initiated in settings

                Log.i("notifyString", string)

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

    private suspend fun updateNotificationMuteTimes(notification:String, context: Context): Int? {
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

        val apContext = context

        val notificationSettingsFlow: Flow<Int> = apContext.dataStore.data //read data in saved data store
            .map { settings: Preferences ->
                // No type safety.
                settings[prefKey] ?: defaultMuteInt   //this sets the value (in exampleCounterFlow not datastore) if null
            }

        //Log.i("readDurationData", exampleCounterFlow.first().toString())
        Log.i("valueinRead", notificationSettingsFlow.first().toString())
        return notificationSettingsFlow.first()
    }
}