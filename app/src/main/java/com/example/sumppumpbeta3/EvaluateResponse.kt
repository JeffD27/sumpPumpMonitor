package com.example.sumppumpbeta3

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.sumppump3.R
import kotlinx.datetime.*
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import java.time.LocalDateTime
import java.time.Duration


class EvaluateResponse() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun onCreate(context: Context, responseString: String, activity: Activity?) {
        if (responseString == "null"){
                responseStringReceived = false
                checkServerError(context)
                return}
        val activityNull = activity == null //this is just for the log below...

        val mainRunWarnReg: Regex = "main_run_warning.:\\s*(\\w+)".toRegex()
        val mainRunningReg: Regex = "mainRunning.:\\s*(\\w+)".toRegex()
        val backupRunningReg: Regex = "backupRunning.:\\s*(\\w+)".toRegex()
        val backupRunWarnReg: Regex = "backup_run_warning.:\\s*(\\w+)".toRegex()
        val backupRunTimeReg: Regex = "backup_run_time\"?:\\s*\"?(\\d*):(\\d*):(\\d*)".toRegex()
        val backupRunTimeNullReg: Regex = "backup_run_time\":\\s*\"?([\\w\\s]*)".toRegex()
        val mainTimeStartedReg: Regex =
            "timeStartedMain.:\\s*.\\w{3,},\\s*(\\d{1,2}\\s\\w{3,}\\s\\d{4}\\s*\\d{1,2}:\\d{1,2}:\\d{1,2})".toRegex()
        val mainRunTimeReg: Regex = "main_run_time\"?:\\s*\"?(\\d*):(\\d*):(\\d*)".toRegex()
        val mainRunTimeNullReg: Regex = "main_run_time\":\\s*\"?([\\w\\s]*)".toRegex()
        val backupTimeStartedReg: Regex =
            "timeStartedBackup.:\\s*.\\w{3,},\\s*(\\d{1,2}\\s\\w{3,}\\s\\d{4}\\s*\\d{1,2}:\\d{1,2}:\\d{1,2})".toRegex()


        val highFloodingReg: Regex = "highFlooding.?:\\s*(\\w+)".toRegex()
        val midFloodingReg: Regex = "midFlooding.:\\s*(\\w+)".toRegex()
        val lowFloodingReg: Regex = "lowFlooding.:\\s*(\\w+)".toRegex()

        val voltage5Reg: Regex = "voltage5.:\\s*\"(\\d*)".toRegex()
        val voltage12Reg: Regex = "voltage12.:\\s*\"(\\d*)".toRegex()
        val charging5Reg: Regex = "charging5.:\\s*(\\w+)".toRegex()


        val timeStampCheckPumpControReg: Regex =
            "timeStampCheckRunning\":\\[\\[{0,1}\"(\\d{4}),\\s(\\d{1,2}),\\s(\\d{1,2}),\\s(\\d{1,2}),\\s(\\d{1,2}),\\s(\\d{1,2})".toRegex()



        var match = responseString?.let { mainRunningReg.find(it) }
        val mainRunningStr = match?.groupValues?.get(1)
        if (mainRunningStr != null) {
        }
        match = responseString?.let { backupRunningReg.find(it) }
        val backupRunningStr = match?.groupValues?.get(1)
        match = responseString?.let { mainRunWarnReg.find(it) }
        val mainRunningWarnStr = match?.groupValues?.get(1)
        if (mainRunningWarnStr != null) {
            applyMainPumpWarn(mainRunningWarnStr)
        }
        match = responseString?.let { backupRunWarnReg.find(it) }
        val backupRunningWarnStr = match?.groupValues?.get(1)
        if (backupRunningWarnStr != null) {
            applyBackupPumpWarn(backupRunningWarnStr)
        }
        match = responseString?.let { mainTimeStartedReg.find(it) }
        mainTimeStartedStr = match?.groupValues?.get(1).toString()
        if (mainTimeStartedStr != null) {
            //val mainRunTime = getRunTime(mainTimeStartedStr)
        }
        match = responseString?.let { backupTimeStartedReg.find(it) }
        backupTimeStartedStr = match?.groupValues?.get(1).toString()
        if (backupTimeStartedStr != null) {
            Log.d("backupTimeStartedStr", backupTimeStartedStr)
            //getRunTime(backupTimeStartedStr)
        }
        match = responseString?.let { mainRunTimeReg.find(it) }
        match?.let {
            mainRunTime_ = "Runtime:\n" + match?.groupValues?.get(1)
                .toString() + ":" + match?.groupValues?.get(2)
                .toString() + ":" + match?.groupValues?.get(3).toString()
        } ?: run { //run will run if match is null

            match = responseString?.let { mainRunTimeNullReg.find(it) }
            mainRunTime_ = "Runtime: \n" + match?.groupValues?.get(1).toString()
        }

        Log.d("mainRunTimeMatch", mainRunTime_)

        match = responseString?.let { backupRunTimeReg.find(it) }
        match?.let {
            backupRunTime_ = "Runtime:\n" + match?.groupValues?.get(1)
                .toString() + ":" + match?.groupValues?.get(2)
                .toString() + ": " + match?.groupValues?.get(3).toString()
        } ?: run { //run will run if match is null
            match = responseString?.let { backupRunTimeNullReg.find(it) }
            backupRunTime_ = "Runtime:\n" + match?.groupValues?.get(1).toString()
        }

        //add function to get last run

        match = responseString?.let { timeStampCheckPumpControReg.find(it) }
        Log.d("timePumpControl", match.toString())
        if (match?.let { checkPumpControlRunning(context, it) } == false) {
            //if pumpcontrol is not running
            pumpControlActive = false
            Log.d("generalWarnSilence", generalWarnSilence.toString())

        }
        else{pumpControlActive = true}




        match = responseString?.let { highFloodingReg.find(it) }
        val highFloodingStr = match?.groupValues?.get(1)
        if (highFloodingStr != null) {
            Log.d("highFloodingString", highFloodingStr)
        }
        match = responseString?.let { midFloodingReg.find(it) }
        val midFloodingStr = match?.groupValues?.get(1)
        match = responseString?.let { lowFloodingReg.find(it) }
        val lowFloodingStr = match?.groupValues?.get(1)

        match = responseString?.let { voltage5Reg.find(it) }
        val voltage5Str = match?.groupValues?.get(1)

        match = responseString?.let { voltage12Reg.find(it) }
        val voltage12Str = match?.groupValues?.get(1)
        match = responseString?.let { charging5Reg.find(it) }
        val charging5Str = match?.groupValues?.get(1)

        //  match = re

        // val isPumpControlRunning = PumpControlRunningCheck()

        applyRelayData(mainRunningStr, backupRunningStr)
        if (voltage5Str != null) {
            if (voltage12Str != null) {
                if (charging5Str != null) {
                    applyBatteryData( voltage5Str, voltage12Str, charging5Str )
                }
            }
        }


        applyWaterLevel(context, highFloodingStr, midFloodingStr,lowFloodingStr, activity)
        checkNoWaterPumpRunningNotify(context)
        checkMainPumpRuntime(context)
        checkVoltagesNotify(context)
        checkMainRunning(context)
        checkServerError(context)
        checkWaterLevelForNotify(context)
        Log.d("backupRun_777", backupRunning_.toString())
        checkBackupRunNotify(context)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun checkPumpControlRunning(context: Context, dateTimeMatch: MatchResult): Boolean {
        Log.d("checkingPumpControl", dateTimeMatch.toString())

        // Extract the values from the match result
        val year = dateTimeMatch.groupValues[1].padStart(4, '0')
        val month = dateTimeMatch.groupValues[2].padStart(2, '0')
        val day = dateTimeMatch.groupValues[3].padStart(2, '0')
        val hour = dateTimeMatch.groupValues[4].padStart(2, '0')
        val minute = dateTimeMatch.groupValues[5].padStart(2, '0')
        val second = dateTimeMatch.groupValues[6].padStart(2, '0')

        // Format the date and time string in UTC (no hardcoded timezone)
        val timeToParse = "$year-$month-$day"+"T$hour:$minute:$second.000Z"
        Log.d("timeTOPARSE", timeToParse)

        // Parse the timestamp to Instant using Kotlin's Instant (assumes UTC)
        val timeStamp = Instant.parse(timeToParse)

        // Convert to ZonedDateTime in UTC using ZoneId.of("UTC")
        val zonedTimeStamp = ZonedDateTime.ofInstant(timeStamp.toJavaInstant(), ZoneId.of("UTC"))
        Log.d("timestampCheckPump", zonedTimeStamp.toString())

        // Convert ZonedDateTime to LocalDateTime (removes timezone)
        val localTimeStamp = zonedTimeStamp.toLocalDateTime()
        Log.d("localTimeStamp", localTimeStamp.toString())

        // Get the current time as LocalDateTime (without timezone information)
        val currentLocalDateTime = LocalDateTime.now()  // This uses the system's default time zone, no offset
        Log.d("currentLocalDateTime", currentLocalDateTime.toString())

        // Calculate the duration between the timestamps (now comparing LocalDateTime to LocalDateTime)
        val duration = Duration.between(localTimeStamp, currentLocalDateTime)
        Log.d("duration&^%", duration.toString())

        // Check if the duration exceeds 1 minute
        if (duration > Duration.ofMinutes(1)) {
            Log.d("noPumpControl!", "Pump Control is not running")

            // Set the warning visibility and deactivate pump control
            warningVisibilities["noPumpControlWarning"] = Pair(1, Clock.System.now())
            pumpControlActive = false

            // Call the notification function
            callDeployNotification(
                context,
                "noPumpControl",
                "Warning: No Pump Control!",
                "Pump Control is not running. The pumps will not work until this is fixed!",
                "high",
                context.getString(R.string.mostUrgentWarningsChannelID),
                context.getString(R.string.noACPowerNotificationID),
            )
            return false
        }
        return true
    }
    private fun applyMainPumpWarn(mainRunWarnStr: String) { //if pump has run > 10 min gets eval in python server side as boolean. boolean is applied here
        Log.d("applymainpumpwarn", "starting apply main pump warn")
        if (mainPumpWarnSilence) {
            val now = Clock.System.now()
            if ((now - mainPumpSilenceTime) > 30.minutes) {
                mainPumpWarnSilence = false
            }
        }
        Log.d("applyMainPumpWarn", mainPumpWarnSilence.toString())
        if (mainRunWarnStr == "true") {
            Log.d("applyMainPumpWarn", mainRunWarnStr)
            mainPumpRuntimeOver10 = true
            warningVisibilities["mainRunTimeWarning"] = Pair(1, Clock.System.now())
        } else {
            Log.d("applyMainPumpWarn", mainRunWarnStr)
            mainPumpRuntimeOver10 = false
        }
    }

    private fun applyRelayData(mainRunningStr: String?, backupRunningStr: String?) {
        Log.d("applyRelayData", "starting apply relay data")
        var mainRunningApply: Boolean = false
        var backupRunningApply: Boolean = false
        if (mainRunningStr != null) {
            Log.d("mainRunningStr applyRelayData", mainRunningStr)
        }
        if (mainRunningStr != null) {
            Log.d("mainRunningStr in applyRelayData", mainRunningStr)
        }
        if (mainRunningStr == "false") {
            mainRunningApply = false
        } else if (mainRunningStr == "true") {
            mainRunningApply = true
        } else {
            Log.d("Failure in applyRelayData", "failure in applyRelayData")
        }

        if (backupRunningStr == "false") {
            backupRunningApply = false
            Log.d("EvalResp","backupRunningApply is false")

        } else if (backupRunningStr == "true") {
            backupRunningApply = true
            Log.d("EvalResp","backupRunningApply is true")
        } else {
            Log.d("Failure in applyRelayData", "failure in applyRelayData")
        }
        Log.d("applyRelayData", "done with apply relay data")
        Log.d("mainRunning", mainRunningApply.toString())
        Log.d("backupRunning", backupRunningApply.toString())
        mainRunning_ = mainRunningApply
        backupRunning_ = backupRunningApply

    }

    private fun applyBatteryData( voltage5Str: String, voltage12Str: String, charging5Str: String) {
        if (voltage5Str != null) {
            Log.d("voltage5Eval", voltage5Str)
        }
        if (voltage12Str != null) {
            Log.d("voltage12Eval", voltage12Str)
        }


        var charging5Apply: Boolean


        var voltage5Apply: Int = voltage5Str?.toInt()!!
        var voltage12Apply: Int = voltage12Str?.toInt()!!

        voltage12_ = voltage12Apply
        voltage5_ = voltage5Apply
        if (voltage5_ < 10 || voltage12_ < 10){
            Log.d("lowVoltaging", voltage5_.toString())
            Log.d("lowVoltaging", voltage12_.toString())

        }

        if (charging5Str == "false") {
            charging5_ = false
        } else if (charging5Str == "true") {
            charging5_ = true
        } else {
            charging5_ = false
            if (charging5Str != null) {
                Log.d("Failure in applyBattery", charging5Str)
            }
        }
        Log.d("charging5inAPply", charging5_.toString())


    }
    private fun applyBackupPumpWarn(backupRunWarnStr: String) {
        Log.d("applybackuppumpwarn", "starting apply backup pump warn")
        if (backupPumpWarnSilence) {
            val now = Clock.System.now()
            if ((now - backupPumpSilenceTime) > 10.minutes) {
                backupPumpWarnSilence = false
            }
        }
        Log.d("applyMainPumpWarn", backupPumpWarnSilence.toString())
        if (backupRunWarnStr == "true") {
            backupRunWarnVis = true
        } else {
            backupRunWarnVis = false
        }
    }

    private fun checkBackupRunNotify(context: Context){
        if (backupRunning_ == true){
            callDeployNotification(
                context,
                "backupRun",
                "Check Sump Pump",
                "Backup Pump has run!\n Please Check Main Pump",
                "high", context.getString(R.string.fullScreenChannel),
                context.getString(R.string.BackupRunningNotification)
            )

        }
    }

    private fun checkMainRunning(context: Context){
        if (mainRunning_ == true){
            Log.d("checkMainRunning", "main RUnning callind deployNotification")
            callDeployNotification(
                context,
                "mainRunning",
                "Main Pump Has Run",
                "The main pump is running or has run.",
                "high",
                context.getString(R.string.mostUrgentWarningsChannelID),
                context.getString(R.string.mainRunningNotification),
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyWaterLevel(context: Context, highFloodingStr: String?, midFloodingStr: String?,
                                lowFloodingStr: String?, activity: Activity?
    ) {

        Log.d("applyingwaterlevel", "waterlevelapplying")


        //val waterLevelWarningBox = findViewById<TextView>(R.id.waterLevelWarning)
        //val triangleWarningImageView = findViewById<ImageView>(R.id.waterLevelWarningTriangle)
        //val xToClose = findViewById<ImageView>(R.id.xToCloseWaterLevelPumpErrorImageView)
        if (highFloodingStr != null) {
            Log.d("highFlood@", highFloodingStr)
        }
        if (highFloodingStr == "false") {
            Log.d("highFloodingStrApplyWater1", highFloodingStr)
            highFlooding_ = false
        } else if (highFloodingStr == "true") {
            Log.d("highFloodingStrApplyWater2", highFloodingStr)
            highFlooding_ = true
            if (midFloodingStr == "false" || lowFloodingStr == "false") {
                sensorError_ = true
                warningVisibilities["sensorErrorWarning"] = Pair(1, Clock.System.now())

                callDeployNotification(
                    context,
                    "sensorError",
                    "SumpPump WaterLevel Sensor Error",
                    "Error In water level sensor\nhigh=true mid/low = false",
                    "default",
                    context.getString(R.string.sensorErrorChannelID),
                    context.getString(R.string.waterLevelSensorErrorNotificationIDA),
                    )
            }
        }
        Log.d("MIDFLOODING STRING", midFloodingStr.toString())

        if (midFloodingStr == "false") {
            midFlooding_ = false
        } else if (midFloodingStr == "true") {
            midFlooding_ = true
            if (lowFloodingStr == "false") {
                sensorError_ = true


                Log.d(
                    "ERROR: Raise notification",
                    "Water Level Sensor Error:Mid Sensor is true, but Low  is false"
                )
                //val intent = Intent(this, MainActivity::class.java)
               callDeployNotification(
                    context,
                    "sensorError",
                    "SumpPump WaterLevel Sensor Error",
                    "Error In water level sensor\nmid=true low = false",
                    "default",
                    context.getString(R.string.sensorErrorChannelID),
                   context.getString(R.string.waterLevelSensorErrorNotificationIDB),
                )}
            }


        else {
            Log.d("Failure in applyWaterlevel", "failure in apply waterlevel mid")

        }




        if (lowFloodingStr == "false") {
            lowFlooding_ = false
        } else if (lowFloodingStr == "true") {
            lowFlooding_ = true
        } else {
            Log.d("Failure in applyWaterlevel", "failure in apply waterlevel low")
        }

        Log.d("applyWaterLevelData", "done with apply waterLEvel data")
        Log.d("highFLooding", highFlooding_.toString())
        Log.d("midFlooding", midFlooding_.toString())
        Log.d("lowFlooding", lowFlooding_.toString())

    }



    private fun checkWaterLevelForNotify(context: Context){ //make sure this gets called
        if (sensorError_ == true){
            callDeployNotification(
                context,
                "sensorError",
                "Sensor Error",
                "Error detected in water level sensors!",
                "default",
                context.getString(R.string.sensorErrorChannelID),
                context.getString(R.string.waterLevelSensorErrorNotificationIDC)
            )


        }
        if (highFlooding_ == true) {


            callDeployNotification(
                context,
                "highWater",
                "WARNING: HIGH Water in Sump Well",
                "The water has reached the top of the well.\nBasement flooding is imminent.",
                "high",
                context.getString(R.string.fullScreenChannel),
                context.getString(R.string.highWaterNotificationID)
            )
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun checkServerError(context: Context){
        Log.d("checkServerError", "initializing")
        Log.d("serverError", serverError.first.toString())

        if (serverError.first){
            Log.d("checkServerError","serverError is true")
            Log.d("now", Clock.System.now().toString())
            Log.d("compared to...time", serverError.second.toJavaInstant().toString())
            Log.d("notificationServerErrorMuteDuration", notificationServerErrorMuteDuration.toString())
            if (java.time.Duration.between(Clock.System.now().toJavaInstant(), serverError.second.toJavaInstant())  > notificationServerErrorMuteDuration.toJavaDuration()){
                Log.d("generalWarnSilence", generalWarnSilence.toString())
                callDeployNotification(
                    context,
                    "serverError",
                    "Server Error",
                    "Error In Server\nNo Data is being received",
                    "low",
                    context.getString(R.string.serverErrorChannelID),
                    context.getString(R.string.serverErrorNotificationID))
            }
        }
    }



    private fun checkVoltagesNotify(context: Context){
        if (!charging5_!!) {
            callDeployNotification(
                context,
                "noPower",
                "SumpPump RPi: No AC Power",
                "Usb is disconnected\nOr there is no power going to RPi",
                "high",
                context.getString(R.string.mostUrgentWarningsChannelID),
                context.getString(R.string.noACPowerNotificationID),
            )
            }
        if (voltage12_ < 95){
            if (!voltage12Low.first){
                voltage12Low = Pair(true, Clock.System.now())
            }
            else{
                if (Clock.System.now() - voltage12Low.second > 10.minutes){
                    callDeployNotification(
                        context,
                        "lowBattery12",
                        "Sump Pump Battery is LOW",
                        "12V battery is low.",
                        "low",
                        context.getString(R.string.GeneralInfoChannelID),
                        context.getString(R.string.lowBattery12vNotificationID),
                    )
                }
            }

        }
        else{
            voltage12Low = Pair(false, Clock.System.now())
        }
    }

    private fun checkMainPumpRuntime(context: Context){
        if (mainPumpRuntimeOver10) {
            callDeployNotification(
                context,
                "mainRunTime",
                "Check Sump Pump",
                "The pump has run\nfor 10 minutes\nwithout stopping.\n\nIf possible check\nif it is actually\npumping.",
                "high",
                 context.getString(R.string.fullScreenChannel),
                 context.getString(R.string.mainRunTimeNotificationID))
        }
    }

    private fun checkNoWaterPumpRunningNotify(context: Context) {
        if (mainRunning_ || backupRunning_ == true) {
            if (lowFlooding_ == false) {
                callDeployNotification(
                    context,
                    "noWater",
                    "URGENT: Check Sump Pump",
                    "Pump may be running dry! Reporting no water, but pump is running.",
                    "high",
                    context.getString(R.string.fullScreenChannel),
                    context.getString(R.string.noWaterNotificationID)                )
            }
        }
    }


    private fun callDeployNotification(context: Context, notificationString: String, title: String, message: String, priority: String, channelID: String, notifid: String){
        Log.d("callingDeployNot", message)
        // Create input data for the WorkRequest
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putString("channelID", channelID)
            .putString("notificationString", notificationString)
            .putString("priority", priority)
            .putString("notifid", notifid)
            .build()

        // Create a OneTimeWorkRequest
        val workRequest: WorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(inputData)
            .build()

        // Enqueue the work request
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}



