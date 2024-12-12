package com.example.sumppumpbeta3

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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


class EvaluateResponse() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun onCreate(context: Context, responseString: String, activity: Activity?) {
        Log.i("evalResp", "is this running???")
        Log.i("responseString",responseString)
        if (responseString == "null"){
                checkServerError(context)
                return}

        val activityNull = activity == null //this is just for the log below...
        Log.i("ActivityNull?", activityNull.toString() )

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
            Log.i("mainrunningstring", mainRunningStr) //already wrong value here
        }
        match = responseString?.let { backupRunningReg.find(it) }
        val backupRunningStr = match?.groupValues?.get(1)
        match = responseString?.let { mainRunWarnReg.find(it) }
        val mainRunningWarnStr = match?.groupValues?.get(1)
        Log.i("mainRunningWarnStr", mainRunningWarnStr.toString())
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
            Log.i("mainRunningStr", mainTimeStartedStr)
        }
        match = responseString?.let { backupTimeStartedReg.find(it) }
        backupTimeStartedStr = match?.groupValues?.get(1).toString()
        if (backupTimeStartedStr != null) {
            Log.i("backupTimeStartedStr", backupTimeStartedStr)
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

        Log.i("mainRunTimeMatch", mainRunTime_)

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
        Log.i("timePumpControl", match.toString())
        if (match?.let { checkPumpControlRunning(context, it) } == false) {
            //if pumpcontrol is not running
            pumpControlActive = false
            Log.i("generalWarnSilence", generalWarnSilence.toString())

        }
        else{pumpControlActive = true}




        match = responseString?.let { highFloodingReg.find(it) }
        val highFloodingStr = match?.groupValues?.get(1)
        if (highFloodingStr != null) {
            Log.i("highFloodingString", highFloodingStr)
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
        Log.i("backupRun_777", backupRunning_.toString())
        checkBackupRunNotify(context)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun checkPumpControlRunning(context: Context, dateTimeMatch: MatchResult): Boolean { //this takes a time stamp from the server that gets updated every 200ms. if that timestamp is more than 1 minute ago...we know pumpcontrol.py is not running
        Log.i("checkingPumpControl", dateTimeMatch.toString())
        var year = dateTimeMatch?.groupValues?.get(1)!!.toString()
        var month = dateTimeMatch?.groupValues?.get(2)!!.toString()
        if (month.length == 1){month = "0" + month}
        var day = dateTimeMatch?.groupValues?.get(3)!!.toString()
        if (day.length == 1){day = "0" + day}
        var hour = dateTimeMatch?.groupValues?.get(4)!!.toString()
        if (hour.length == 1){hour = "0" + hour}
        var minute = dateTimeMatch?.groupValues?.get(5)!!.toString()
        if (minute.length == 1){minute = "0" + minute}
        var second = dateTimeMatch?.groupValues?.get(6)!!.toString()
        if (second.length == 1){second = "0" + second}

        var timeToParse = String.format("%s-%s-%sT%s:%s:%s.000-05:00", year, month, day, hour, minute, second)
        Log.i("timeTOPARSE", timeToParse.toString())
        //          2016-09-18T12:17:21:000Z
        //          2024-12-012T11:23:52.000-05:00
        var timeStamp = Instant.parse(timeToParse).toJavaInstant()
        val zonedTimeStamp = ZonedDateTime.ofInstant(timeStamp, ZoneId.of("America/New_York"))
        Log.i("timestampCheckPump", timeStamp.toString())
        val zonedDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))
        val duration = java.time.Duration.between(
            zonedTimeStamp,
            zonedDateTime)
        Log.i("zonedDateTime", zonedDateTime.toString())
        Log.i("duration&^%", duration.toString())
        if (java.time.Duration.between(
                zonedTimeStamp,
                zonedDateTime
            ) > java.time.Duration.ofMinutes(1)
        ) {
            Log.i("noPumpControl!", "Pump Control is not running")
            warningVisibilities["noPumpControlWarning"] = Pair(1, Clock.System.now())
            pumpControlActive = false


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
        else    {
            Log.i("evaluateResp", "we have ELSE")
            val time = warningVisibilities["noPumpControlWarning"]?.second
            if (time != null){
                warningVisibilities["noPumpControlWarning"] = Pair(0, time)
            }

            pumpControlActive = true
            return true
            }
    }
    private fun applyMainPumpWarn(mainRunWarnStr: String) { //if pump has run > 10 min gets eval in python server side as boolean. boolean is applied here
        Log.i("applymainpumpwarn", "starting apply main pump warn")
        if (mainPumpWarnSilence) {
            val now = Clock.System.now()
            if ((now - mainPumpSilenceTime) > 30.minutes) {
                mainPumpWarnSilence = false
            }
        }
        Log.i("applyMainPumpWarn", mainPumpWarnSilence.toString())
        if (mainRunWarnStr == "true") {
            Log.i("applyMainPumpWarn", mainRunWarnStr)
            mainPumpRuntimeOver10 = true
            warningVisibilities["mainRunTimeWarning"] = Pair(1, Clock.System.now())
        } else {
            Log.i("applyMainPumpWarn", mainRunWarnStr)
            mainPumpRuntimeOver10 = false
        }
    }

    private fun applyRelayData(mainRunningStr: String?, backupRunningStr: String?) {
        Log.i("applyRelayData", "starting apply relay data")
        var mainRunningApply: Boolean = false
        var backupRunningApply: Boolean = false
        if (mainRunningStr != null) {
            Log.i("mainRunningStr applyRelayData", mainRunningStr)
        }
        if (mainRunningStr != null) {
            Log.i("mainRunningStr in applyRelayData", mainRunningStr)
        }
        if (mainRunningStr == "false") {
            mainRunningApply = false
        } else if (mainRunningStr == "true") {
            mainRunningApply = true
        } else {
            Log.i("Failure in applyRelayData", "failure in applyRelayData")
        }

        if (backupRunningStr == "false") {
            backupRunningApply = false
            Log.i("EvalResp","backupRunningApply is false")

        } else if (backupRunningStr == "true") {
            backupRunningApply = true
            Log.i("EvalResp","backupRunningApply is true")
        } else {
            Log.i("Failure in applyRelayData", "failure in applyRelayData")
        }
        Log.i("applyRelayData", "done with apply relay data")
        Log.i("mainRunning", mainRunningApply.toString())
        Log.i("backupRunning", backupRunningApply.toString())
        mainRunning_ = mainRunningApply
        backupRunning_ = backupRunningApply

    }

    private fun applyBatteryData( voltage5Str: String, voltage12Str: String, charging5Str: String) {
        if (voltage5Str != null) {
            Log.i("voltage5Eval", voltage5Str)
        }
        if (voltage12Str != null) {
            Log.i("voltage12Eval", voltage12Str)
        }


        var charging5Apply: Boolean


        var voltage5Apply: Int = voltage5Str?.toInt()!!
        var voltage12Apply: Int = voltage12Str?.toInt()!!

        voltage12_ = voltage12Apply
        voltage5_ = voltage5Apply
        if (voltage5_ < 10 || voltage12_ < 10){
            Log.i("lowVoltaging", voltage5_.toString())
            Log.i("lowVoltaging", voltage12_.toString())

        }

        if (charging5Str == "false") {
            charging5_ = false
        } else if (charging5Str == "true") {
            charging5_ = true
        } else {
            charging5_ = false
            if (charging5Str != null) {
                Log.i("Failure in applyBattery", charging5Str)
            }
        }
        Log.i("charging5inAPply", charging5_.toString())


    }
    private fun applyBackupPumpWarn(backupRunWarnStr: String) {
        Log.i("applybackuppumpwarn", "starting apply backup pump warn")
        if (backupPumpWarnSilence) {
            val now = Clock.System.now()
            if ((now - backupPumpSilenceTime) > 10.minutes) {
                backupPumpWarnSilence = false
            }
        }
        Log.i("applyMainPumpWarn", backupPumpWarnSilence.toString())
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
                "Backup Pump has run!",
                "high", context.getString(R.string.mostUrgentWarningsChannelID),
                context.getString(R.string.BackupRunningNotification)
            )
        }

    }

    private fun checkMainRunning(context: Context){
        if (mainRunning_ == true){
            Log.i("checkMainRunning", "main RUnning callind deployNotification")
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

        Log.i("applyingwaterlevel", "waterlevelapplying")


        //val waterLevelWarningBox = findViewById<TextView>(R.id.waterLevelWarning)
        //val triangleWarningImageView = findViewById<ImageView>(R.id.waterLevelWarningTriangle)
        //val xToClose = findViewById<ImageView>(R.id.xToCloseWaterLevelPumpErrorImageView)
        if (highFloodingStr != null) {
            Log.i("highFlood@", highFloodingStr)
        }
        if (highFloodingStr == "false") {
            Log.i("highFloodingStrApplyWater1", highFloodingStr)
            highFlooding_ = false
        } else if (highFloodingStr == "true") {
            Log.i("highFloodingStrApplyWater2", highFloodingStr)
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
        Log.i("MIDFLOODING STRING", midFloodingStr.toString())

        if (midFloodingStr == "false") {
            midFlooding_ = false
        } else if (midFloodingStr == "true") {
            midFlooding_ = true
            if (lowFloodingStr == "false") {
                sensorError_ = true


                Log.i(
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
            Log.i("Failure in applyWaterlevel", "failure in apply waterlevel mid")

        }




        if (lowFloodingStr == "false") {
            lowFlooding_ = false
        } else if (lowFloodingStr == "true") {
            lowFlooding_ = true
        } else {
            Log.i("Failure in applyWaterlevel", "failure in apply waterlevel low")
        }

        Log.i("applyWaterLevelData", "done with apply waterLEvel data")
        Log.i("highFLooding", highFlooding_.toString())
        Log.i("midFlooding", midFlooding_.toString())
        Log.i("lowFlooding", lowFlooding_.toString())

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
                context.getString(R.string.mostUrgentWarningsChannelID),
                context.getString(R.string.highWaterNotificationID)
            )
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun checkServerError(context: Context){
        Log.i("checkServerError", "initializing")
        Log.i("serverError", serverError.first.toString())

        if (serverError.first){
            Log.i("checkServerError","serverError is true")
            Log.i("now", Clock.System.now().toString())
            Log.i("compared to...time", serverError.second.toJavaInstant().toString())
            Log.i("notificationServerErrorMuteDuration", notificationServerErrorMuteDuration.toString())
            if (java.time.Duration.between(Clock.System.now().toJavaInstant(), serverError.second.toJavaInstant())  > notificationServerErrorMuteDuration.toJavaDuration()){
                Log.i("generalWarnSilence", generalWarnSilence.toString())
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

            callDeployNotification(
                context,
                "lowBattery12",
                "Sump Pump Battery is LOW",
                "12 Volt battery is low. Check AC power.",
                "low",
                context.getString(R.string.GeneralInfoChannelID),
                context.getString(R.string.lowBattery12vNotificationID),
            )
        }
    }

    private fun checkMainPumpRuntime(context: Context){
        if (mainPumpRuntimeOver10) {
            callDeployNotification(
                context,
                "mainRunTime",
                "Check Sump Pump",
                "The pump has run for 10 minutes without stopping",
                "high",
                 context.getString(R.string.mostUrgentWarningsChannelID),
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
                    context.getString(R.string.mostUrgentWarningsChannelID),
                    context.getString(R.string.noWaterNotificationID)                )
            }
        }
    }


    private fun callDeployNotification(context: Context, notificationString: String, title: String, message: String, priority: String, channelID: String, notifid: String){
        Log.i("callingDeployNot", message)
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



