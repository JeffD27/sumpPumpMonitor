package com.example.sumppumpbeta3;
//import com.example.sumppumpbeta3.R
//import com.example.sumppumpbeta3.databinding.ActivityMainBinding
//import kotlinx.datetime.ZoneOffset
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import com.example.sumppump3.R
import com.example.sumppump3.databinding.ActivityMainBinding
import com.squareup.moshi.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.chrono.ChronoLocalDateTime
import java.time.temporal.Temporal
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

var preServerError: Pair<Boolean, LocalDateTime> = Pair(false, LocalDateTime.now())
val durationConvertDict = LinkedHashMap<String, kotlin.time.Duration>()
val spinnerStringDict = LinkedHashMap<Spinner, String>()
val spinnerDurationDict = LinkedHashMap<Spinner, kotlin.time.Duration>()

val durationPositionInt = LinkedHashMap<kotlin.time.Duration, Int>()

var showSettingsWindow: Boolean = false
val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
var notificationServerErrorMuteDuration: kotlin.time.Duration = 1.days
var notificationWaterLevelSensorErrorMuteDuration: kotlin.time.Duration = 1.days
var notificationACPowerMuteDuration: kotlin.time.Duration = 1.hours
var notificationHighWaterMuteDuration: kotlin.time.Duration = 15.minutes
var notificationMainRunWarnMuteDuration: kotlin.time.Duration = 30.minutes
var notificationBackupRanMuteDuration: kotlin.time.Duration = 1.hours
var notificationWaterTooLowMuteDuration:  kotlin.time.Duration = 10.minutes
var notificationBattery12LowMuteDuration: kotlin.time.Duration = 1.days
var notificationNoPumpControlMuteDuration: kotlin.time.Duration = 12.hours

@RequiresApi(Build.VERSION_CODES.O)
var serverError: Pair<Boolean, LocalDateTime> = Pair(false, LocalDateTime.now())

//@JsonClass(generateAdapter = true)
data class PyDataLayout (
    val mainRunningLyt: String?,
    val backupRunningLyt: String?,

    val highFloodingLyt: String?,
    val midFloodingLyt: String?,
    val lowFloodingLyt: String?,

    val voltage5Lyt: String?,
    val voltage12Lyt: String?,
    val charging5: String?
){}
data class PyData (
    @Json(name = "main_pump")
    val mainRunning: Boolean?,
    @Json(name = "backup_pump")
    val backupRunning: Boolean?,
    @Json(name = "waterLevel")
    val highFlooding: Boolean?,
    val midFlooding: Boolean?,
    val lowFlooding: Boolean?,
    @Json(name="batteries")
    val voltage5: Float?,
    val charging5: Boolean?,
    val voltage12: Float?){}
    val defaultMuteTimes = LinkedHashMap<String, Duration>()

    val warningVisibilities = LinkedHashMap<String, Pair<Int, LocalDateTime>>()


class MainActivity : ComponentActivity() {


    private var bootRun: Boolean = true
    private var mainRunning_: Boolean = false
    private  var mainRunTime_ : String = "Loading..."
    private var backupRunTime_ : String = ""
    private var mainTimeStartedStr : String = "Loading..."
    private var backupTimeStartedStr: String = "Loading..."
    private var backupRunning_: Boolean? = true



    private var voltage12_: Int = 0
    private var voltage5_: Int = 0
    private var charging5_: Boolean? = true
    @RequiresApi(Build.VERSION_CODES.O)
    private var checkBatteryVoltsTime: Temporal? = null
    private var pumpControlActive = false // false means we do no have pump control
    private var highFlooding_: Boolean? = false
    private var midFlooding_: Boolean? = false
    private var lowFlooding_: Boolean? = false
    private var mainPumpWarnSilence = false
    private var backupPumpWarnSilence = false
    private var generalWarnSilence = false
    private var waterLevelWarnSilence = false
    private lateinit var mainPumpSilenceTime: Instant
    private lateinit var backupPumpSilenceTime:Instant


//the following are for resetting notifications
    lateinit var notificationServerErrorDeployed: Pair<Boolean, Instant> //to calculate if notification needs to be reset <if deployed, time deployed>
    private lateinit var notificationWaterLevelSensorErrorDeployed: Pair<Boolean, Instant>
    private lateinit var notificationWaterLevelSensorErrorBDeployed: Pair<Boolean, Instant>
    private lateinit var notificationACPowerDeployed: Pair<Boolean, Instant>
    private lateinit var notificationHighWaterDeployed: Pair<Boolean, Instant>
    private lateinit var notificationMainRunWarnDeployed: Pair<Boolean, Instant>
    private lateinit var notificationBackupRan: Pair<Boolean, Instant>
    private lateinit var notificationWaterTooLow: Pair<Boolean, Instant>
    private lateinit var notificationBattery12Low: Pair<Boolean, Instant>
    private lateinit var notificationNoPumpCotnrol: Pair<Boolean, Instant>





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    0
            )
        }

        Intent(applicationContext, RunningService::class.java).also {
            it.action = RunningService.Actions.START.toString()
            startService(it)
        }



        //val binding: DataBindingUtil.inflate(layoutInflater, R.layout.list_item, viewGroup, false)
        val activity: Activity = this


        try {
            Class.forName("dalvik.system.CloseGuard")
                .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
                .invoke(null, true)
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }



        Log.i("durationConvertDictKeys", durationConvertDict.keys.toString())
        //this is just initializing the ...deployed variables for use in reset notifications
        if (!this::notificationServerErrorDeployed.isInitialized) {notificationServerErrorDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationWaterLevelSensorErrorDeployed.isInitialized) {notificationWaterLevelSensorErrorDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationACPowerDeployed.isInitialized) {notificationACPowerDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationWaterLevelSensorErrorBDeployed.isInitialized) {notificationWaterLevelSensorErrorBDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationHighWaterDeployed.isInitialized) {notificationHighWaterDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationMainRunWarnDeployed.isInitialized) {notificationMainRunWarnDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationBackupRan.isInitialized) {notificationBackupRan = Pair(false, Clock.System.now())}
        if (!this::notificationWaterTooLow.isInitialized) {notificationWaterTooLow = Pair(false, Clock.System.now())}
        if (!this::notificationBattery12Low.isInitialized) {notificationBattery12Low = Pair(false, Clock.System.now())}
        if (!this::notificationNoPumpCotnrol.isInitialized) {notificationNoPumpCotnrol = Pair(false, Clock.System.now())}




        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainRunningBoxColor = ContextCompat.getColor(this, R.color.green)
        binding.backupRunningBoxColor = ContextCompat.getColor(this, R.color.green)
        binding.waterLevelImage = ContextCompat.getDrawable(this, R.drawable.water_low)
        binding.mainRunWarnView = true
        binding.backupRunWarnView = true
        binding.generalErrorView = false
        binding.waterLevelWarnView = true
        binding.battery5TextBGColor = ContextCompat.getColor(this, R.color.green)
        binding.battery12TextBGColor = ContextCompat.getColor(this, R.color.green)

        binding.acPowerLargeBatteryImage = ContextCompat.getDrawable(this, R.drawable.acplug)




        val buttonSettings = findViewById<Button>(R.id.buttonSettings)






        defaultMuteTimes["serverError"] = 1.days
        defaultMuteTimes["sensorError"] = 1.days
        defaultMuteTimes["noPower"] = 1.hours
        defaultMuteTimes["highWater"] = 15.minutes
        defaultMuteTimes["mainRunTime"] = 10.minutes
        defaultMuteTimes["backupRun"] = 10.minutes
        defaultMuteTimes["noWater"] = 10.minutes
        defaultMuteTimes["lowBattery12"] = 1.days
        defaultMuteTimes["noPumpControl"] = 12.hours


        //set an initial datetime way in the past
        /*
        warningVisibilities["sensorErrorWarning"] = LocalDateTime.of(2000, 1,1, 12,0)
        warningVisibilities["noPowerWarning"] = LocalDateTime.of(2000, 1,1, 12,0)
        warningVisibilities["highWaterWarning"] = LocalDateTime.of(2000, 1,1, 12,0)
        warningVisibilities["mainRunTimeWarning"] = LocalDateTime.of(2000, 1,1, 12,0)
        warningVisibilities["backupRunWarning"] = LocalDateTime.of(2000, 1,1, 12,0)
        warningVisibilities["noWaterWarning"] = LocalDateTime.of(2000, 1,1, 12,0)
        warningVisibilities["lowBattery12Warning"] = LocalDateTime.of(2000, 1,1, 12,0)
        warningVisibilities["noPumpControlWarning"] = LocalDateTime.of(2000, 1,1, 12,0)
        */
        for(warning in warningStrings){
            runBlocking {  //com.example.sumppumpbeta3.warningVisibilities[warning]
                val data = readWarningData(warning)
                val timeString = data.second
                val regex = Regex("(\\d{4}).(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})")
                val match = regex.find(timeString)!!
                val year = match.groupValues[1].toInt()
                val month = match.groupValues[2].toInt()
                val day = match.groupValues[3].toInt()
                val hour = match.groupValues[4].toInt()
                val minute  = match.groupValues[5].toInt()


                val timeStamp = LocalDateTime.of(year, month, day, hour, minute)
                val visibility = getVisibility(timeStamp)
                warningVisibilities[warning] = Pair(visibility,timeStamp)
                Log.i("timeString", timeString)
            }

        }



// Set an OnClickListener for the button
        buttonSettings.setOnClickListener {

            // Create an Intent to start the new activity
            val intent = Intent(this, Settings()::class.java)


            // Start the new activity
            startActivity(intent)
        }

        val buttonWarnings = findViewById<Button>(R.id.buttonWarnings)

        buttonWarnings.setOnClickListener {

            // Create an Intent to start the new activity
            val intent = Intent(this, Warnings()::class.java)


            // Start the new activity
            startActivity(intent)
        }

        callServer(activity, binding)



    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun callServer(activity: Activity?, binding: ActivityMainBinding?){


                    var firstRun: Boolean = true

                    val notificationStrings = listOf("serverError", "sensorError", "noPower", "highWater", "mainRunTime", "backupRun", "noWater", "lowBattery12", "noPumpControl" )


                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val intDurationDict = LinkedHashMap<Int, Duration>()
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
                    val threadServer = Thread {
                        while (true) {

                            for(string in notificationStrings){
                                runBlocking {
                                        val durationInt = updateNotificationMuteTimes(string)!! //initiated in settings

                                        //Log.i("notifyString", string)

                                       // Log.i("durationInt", durationInt.toString())

                                        when(string){
                                            "serverError" ->{
                                                notificationServerErrorMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "sensorError" ->{
                                                notificationWaterLevelSensorErrorMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "noPower" ->{
                                                notificationACPowerMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "highWater" ->{
                                                notificationHighWaterMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "mainRunTime" ->{
                                                notificationMainRunWarnMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "backupRun" ->{
                                                notificationBackupRanMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "noWater" ->{
                                                notificationWaterTooLowMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "lowBattery12" ->{
                                                notificationBattery12LowMuteDuration = intDurationDict[durationInt]!!
                                            }
                                            "noPumpControl" ->{
                                                notificationNoPumpControlMuteDuration = intDurationDict[durationInt]!!

                                            }
                                        }
                                    }
                                }

                            Log.i("charging5", charging5_.toString())
                            if(!charging5_!!){
                                Log.i("generalWarnCharging5", generalWarnSilence.toString())

                                warningVisibilities["noPowerWarning"] = Pair(1, LocalDateTime.now())
                                if (binding != null){
                                    if (binding != null && activity != null) {
                                        binding.acPowerSmallBatteryImage = ContextCompat.getDrawable(activity, R.drawable.noacplug)
                                    }
                                }
                                if (!generalWarnSilence){
                                    if (binding != null) {
                                        if (binding != null) {
                                            binding.generalErrorView = true
                                        }

                                        if (binding != null) {
                                            binding.generalErrorText = "USB disconnected\n / no power!"
                                        }
                                    }
                                    val (deployed, timeDeployed) = notificationACPowerDeployed
                                    Log.i("time and deploy", timeDeployed.toString())
                                    Log.i("time and deploy", deployed.toString())
                                    if (!deployed){
                                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                        Log.i("charging5", "starting notification for charging 5")
                                        notificationBuilder(
                                            "SumpPump RPi: No AC Power",
                                            "Usb is disconnected\nOr there is no power going to RPi",
                                            "high",
                                            getString(R.string.systemIssuesChannelID),
                                            getString(R.string.noACPowerNotificationID),
                                            notificationManager)
                                        notificationACPowerDeployed = Pair(true, Clock.System.now())



                                    }
                                }
                            }

                            else{
                                //warningVisibilities["noPowerWarning"] = false   you don't want to do this. make it false after x amount of time so it continues to display on warnings page
                                if (binding != null){
                                    if (warningVisibilities["serverErrorWarning"]!!.first == 0 && pumpControlActive){
                                        if( warningVisibilities["noWaterWarning"]!!.first == 0){
                                            binding.generalErrorView = false
                                        }
                                    }
                                    binding.acPowerSmallBatteryImage =
                                        activity?.let { ContextCompat.getDrawable(it, R.drawable.acplug) }
                                }
                            }




                            try {

                                val parameters = mapOf<String, String>("firstRun" to firstRun.toString())
                                Log.i("mainactivity oncreate", "calling get on sumppump.jeffs-handyman.net")
                                getFromServer( "https://sumppump.jeffs-handyman.net/",  parameters, null, binding)
                                //serverError = Pair(false, LocalDateTime.now())
                                preServerError = Pair(false, LocalDateTime.now())

                            } catch (e: java.lang.Exception) {
                                Log.i("serverError@#$*", LocalDateTime.now().toString())
                                warningVisibilities["serverErrorWarning"] = Pair(1, LocalDateTime.now())
                                Log.i("serverError", "yep that's a server error.")
                                e.printStackTrace()
                                if (!serverError.first){
                                    serverError = Pair(true, LocalDateTime.now())
                                }


                                //Log.i("mainActivity after get", e.toString())




                            }
                            firstRun = true //this was false, but I don't want this variable anymore
                            Log.i("onCreate MainRunning", mainRunning_.toString())


                            if (mainRunning_ == true) {

                                if (binding != null){
                                    if (binding != null && activity != null) {
                                        binding.mainRunning = "Pump is Running"

                                        binding.mainRunTime = mainRunTime_
                                        binding.mainRunningBoxColor =
                                            ContextCompat.getColor(activity, R.color.green)
                                    }
                                    Log.i("mainRunningColor", binding.mainRunningBoxColor.toString())
                                }

                            }
                            if (mainRunning_ == false) {
                                if (binding != null && activity != null) {
                                    binding.mainRunning = "Pump Is Not Running"
                                    binding.mainRunningBoxColor =
                                        ContextCompat.getColor(activity, R.color.red)
                                    Log.i("mainTimeStartedStr", mainTimeStartedStr)
                                    binding.mainRunTime = "Last Ran On:\n$mainTimeStartedStr"
                                    Log.i(
                                        "mainRunningColor",
                                        binding.mainRunningBoxColor.toString()
                                    )
                                }
                            }
                            Log.i("mainRunTime_", mainRunTime_)



                            if (backupRunning_ == true) {

                                warningVisibilities["backupRunWarning"] = Pair(1, LocalDateTime.now())
                                val (deployed, timeDeployed) = notificationBackupRan

                                if (!deployed){


                                    notificationBuilder("Check Sump Pump", "Backup Pump has run!","high", getString(R.string.pumpProblemsChannel), getString(R.string.backupRunNotificationID), notificationManager)
                                    notificationBackupRan = Pair(true, Clock.System.now())
                            }
                                if (binding != null && activity != null){
                                    binding.backupRunTime = backupRunTime_
                                    binding.backupRunning = "Pump is Running"

                                    binding.backupRunningBoxColor =
                                        ContextCompat.getColor(activity, R.color.green)
                                    Log.i("backupRunningColor", binding.backupRunningBoxColor.toString())
                                    backupRunTime_.let{binding.backupRunTime}}
                                backupPumpWarnSilence = false
                            }
                            if (backupRunning_ == false) {
                                if (binding != null && activity != null){
                                    binding.backupRunning = "Pump Is Not Running"
                                    binding.backupRunTime = "Last Ran on:\n $backupTimeStartedStr"
                                    binding.backupRunningBoxColor =
                                        ContextCompat.getColor(activity, R.color.red)

                                    Log.i("mainRunningColor", binding.mainRunningBoxColor.toString())
                                }
                            }


                            if (highFlooding_ == true) {
                                warningVisibilities["highWaterWarning"] = Pair(1, LocalDateTime.now())
                                if (binding != null && activity != null) {
                                    binding.waterLevelImage =
                                        ContextCompat.getDrawable(activity, R.drawable.water_high)
                                    binding.waterLevelText = "High\n(100%)"
                                }
                                 //set in applyWaterLevel


                                val (deployed, timeDeployed) = notificationHighWaterDeployed
                                if (!deployed){
                                    notificationBuilder(
                                        "WARNING: HIGH Water in Sump Well",
                                        "The water has reached the top of the well.\nBasement flooding is imminent.",
                                        "high",
                                        "Pump Errors/Warnings",
                                        getString(R.string.highWaterNotificationID),
                                        notificationManager
                                    )
                                    notificationHighWaterDeployed = Pair(true, Clock.System.now())
                                }
                                if( midFlooding_ == false || lowFlooding_ == false  ) {
                                    if (binding != null){
                                        binding.waterLevelWarnView = !waterLevelWarnSilence
                                    }
                                }
                                else{
                                    if (binding != null){binding.waterLevelWarnView=false}
                                }
                            }
                            else if (midFlooding_ == true) {
                                    if (binding != null && activity != null){
                                        binding.waterLevelImage =
                                            ContextCompat.getDrawable(activity, R.drawable.water_50)
                                        binding.waterLevelText = "50%"
                                    }

                                if( lowFlooding_ == false ) {
                                    if (binding != null){
                                        binding.waterLevelWarnView = !waterLevelWarnSilence
                                    }
                                }

                                else{
                                    if (binding != null){binding.waterLevelWarnView=false}}
                                }
                                else if (lowFlooding_ == true) {
                                    if (binding != null && activity != null){
                                        binding.waterLevelImage =
                                            ContextCompat.getDrawable(activity, R.drawable.water_10)
                                        binding.waterLevelText = "10%"
                                        binding.waterLevelWarnView=false
                                    }
                            } else {
                                if (binding != null && activity != null) {
                                    binding.waterLevelImage =
                                        ContextCompat.getDrawable(activity, R.drawable.water_low)
                                    binding.waterLevelText = "Empty"
                                    binding.waterLevelWarnView = false
                                }
                            }

                            if (binding != null) { binding.battery12vText = "$voltage12_%"}
                            if (voltage12_ < 95){
                                warningVisibilities["lowBattery12Warning"] = Pair(1, LocalDateTime.now())
                                val (deployed, timeDeployed) = notificationBattery12Low
                                if (binding != null && activity != null){binding.battery12TextBGColor = ContextCompat.getColor(activity, R.color.red)}
                                if(!deployed){
                                    notificationBuilder(
                                        "Sump Pump Battery is LOW",
                                        "12 Volt battery is low. Check AC power.",
                                        "high",
                                        getString(R.string.pumpProblemsChannel),
                                        getString(R.string.lowBattery12vNotificationID),
                                        notificationManager
                                    )
                                }
                                notificationBattery12Low = Pair(true, Clock.System.now())
                            }
                            else{
                                if (binding != null && activity != null){
                                    binding.battery12TextBGColor = ContextCompat.getColor(activity, R.color.green)
                                }
                            }
                            Log.i("voltage5_", voltage5_.toString())
                            val sleepTime = java.time.Duration.ofMinutes(1)

                            val currentTime = LocalDateTime.now()
                            checkBatteryVoltsTime?.let{}?: run{ checkBatteryVoltsTime = LocalDateTime.now() - java.time.Duration.ofMinutes(40)}
                            Log.i("checkBatteryVoltsTime", checkBatteryVoltsTime.toString())
                            val duration = java.time.Duration.between(checkBatteryVoltsTime!!, currentTime)
                            Log.i("durationBatteryVolts", duration.toString())
                            Log.i("sleeptime", sleepTime.toString())
                            Log.i("sleeptimeBool", (duration > sleepTime).toString())
                            if (binding != null && activity != null){
                                if (duration > sleepTime || binding.battery5TextView.text == "0%" ){
                                    Log.i("durationBatteryVolts2", sleepTime.toString())
                                    if (binding != null){binding.battery5vText = "$voltage5_%"}
                                    checkBatteryVoltsTime = LocalDateTime.now()

                                    if(voltage5_ < 70){

                                            binding.battery5TextBGColor = ContextCompat.getColor(activity, R.color.red)

                                    }
                                    else {

                                            binding.battery5TextBGColor = ContextCompat.getColor(activity, R.color.green)

                                    }
                                }
                            }

                            val (deployed, timeDeployed) = notificationMainRunWarnDeployed

                            if (mainRunWarnVis and !deployed){
                                notificationBuilder("Check Sump Pump", "The pump has run for 10 minutes without stopping","high", getString(R.string.pumpProblemsChannel), getString(R.string.mainRunTimeNotificationID), notificationManager)
                                notificationMainRunWarnDeployed = Pair(true, Clock.System.now())
                            }
                            if (binding != null){
                                if (mainRunWarnVis && !mainPumpWarnSilence) {
                                    binding.mainRunWarnView = true}
                                else {binding.mainRunWarnView = false}
                                if (backupRunWarnVis && !backupPumpWarnSilence) {binding.backupRunWarnView = true}
                                else {binding.backupRunWarnView = false}
                            }




                            if (mainRunning_|| backupRunning_ == true){
                                if (lowFlooding_ == false){
                                    warningVisibilities["noWaterWarning"] = Pair(1, LocalDateTime.now())
                                    if (binding != null) {
                                        if( mainRunning_){
                                            binding.generalErrorView= true
                                            binding.generalErrorText = "Pump running dry,\n Please Check!"
                                        }
                                    }
                                        val (deployed, timeDeployed) = notificationWaterTooLow
                                    if (!deployed){
                                        notificationBuilder(
                                            "URGENT: Check Pump",
                                            "The water level seems empty, but the pump is running",
                                            "high",
                                            "Pump Errors/Warnings",
                                            getString(R.string.waterTooLowNotificationID),
                                            notificationManager)

                                        notificationWaterTooLow = Pair(true, Clock.System.now())
                                    }


                                }

                            }
                            if (binding != null) {
                                if (!mainRunning_ && binding.generalErrorText == "Pump running dry,\n Please Check!"){
                                    binding.generalErrorView = false
                                }
                            }
                            Log.i("noPowerMuteTime", notificationACPowerMuteDuration.toString() )
                            checkServerError(binding, notificationManager)
                            if (activity != null) {
                                resetNotifications(activity)
                            }


                            runBlocking {
                                //set warnings page data

                                dataStore.edit { settings -> //write data to saved data store
                                    for(warnVis in warningVisibilities) {
                                        val warnStart = warnVis.value.second
                                        Log.i("warnvis", warnVis.key)
                                        val timeStampKey = warnVis.key + "Time"
                                        settings[stringPreferencesKey(timeStampKey)] = warnStart.toString()
                                        Log.i("duration", java.time.Duration.between(LocalDateTime.now(), warnVis.value.second).toString())
                                        val visibility = getVisibility(warnVis.value.second)
                                        settings[intPreferencesKey(warnVis.key)] = visibility



                                        }
                                    }

                                }




                            sleep(2000)
                            bootRun = false
                        }
                    }
                    threadServer.start()




            println("Hello")

            }



    /*private fun sendFirstRun(firstRun:Boolean){
        val client = OkHttpClient()

        val body: RequestBody = MultipartBuilder()
        val requestBuilder: Request = Request.Builder()
            .url("https://www.example.com/index.php")
            .post(formBody)
            .build()

        try {
            val response = client.newCall(requestBuilder).execute()

            // Do something with the response.
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val request: Request = Request.Builder()
            .post(firstRun)
            .url("http://10.0.0.218:3000/")
            .header("Connection", "close")
            .build()



    }*/

    /*
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        // ...

        val notification: Notification = ExpeditedWorker()::class.java
        return ForegroundInfo(R.string.persistentWorkerNotificationID, ,
            FOREGROUND_SERVICE_TYPE_LOCATION or
                    FOREGROUND_SERVICE_TYPE_MICROPHONE) }
    */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getVisibility(timeStamp:LocalDateTime): Int {
        if (java.time.Duration.between(timeStamp, LocalDateTime.now()) < java.time.Duration.ofDays(5) ) {
           return 1

        } else {
            return 0

        }

    }
    private fun notificationBuilder(title:String, content:String, priority: String, channelid: String, notifid:String, notificationManager: NotificationManager  ){ //priority: high default low
        Log.i("notificationBuilder()", "starting notification builder")
        val builder = NotificationCompat.Builder(this, channelid)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setContentIntent(pendingIntent)

        Log.i("notificationBuilder", title )
        Log.i("notificationBuilder", content )
        builder.setSmallIcon(R.drawable.floodedhouse)
        if (priority == "high"){
            builder.priority = NotificationCompat.PRIORITY_HIGH}
        else if(priority=="default"){
            builder.priority = NotificationCompat.PRIORITY_DEFAULT}
        else if(priority == "low"){
            builder.priority = NotificationCompat.PRIORITY_LOW
        }

        notificationManager.notify(notifid.toInt(), builder.build())
    }



    suspend fun updateNotificationMuteTimes(notification:String): Int? {
        Log.i("updateNotifcationMuteTimes", notification)

        val durationIntDict = LinkedHashMap<Duration, Int>()
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

        val settingsCounterFlow: Flow<Int> = this.dataStore.data //read data in saved data store
              .map { settings ->
                  // No type safety.
                  settings[prefKey] ?: defaultMuteInt   //this sets the value (in exampleCounterFlow not datastore) if null
              }

        //Log.i("readDurationData", exampleCounterFlow.first().toString())
        Log.i("valueinRead", settingsCounterFlow.first().toString())
        return settingsCounterFlow.first()
    }


    private fun resetNotifications(activity: Activity) {
        if (this::notificationServerErrorDeployed.isInitialized) {
            val (deployed, timeDeployed) = notificationServerErrorDeployed
            Log.i("resetNotifications() server", timeDeployed.toString())
            val timeDif = (Clock.System.now() - timeDeployed)
            Log.i("timeDIFf_ResetNotifications", timeDif.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationServerErrorMuteDuration) {
                notificationServerErrorDeployed = Pair(false, Clock.System.now())
            }
        }



        if (this::notificationWaterLevelSensorErrorDeployed.isInitialized) { //this notification was never tested
            val (deployed, timeDeployed) = notificationWaterLevelSensorErrorDeployed
            Log.i("resetNotifications() wl sensor", timeDeployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationWaterLevelSensorErrorMuteDuration) {
                notificationWaterLevelSensorErrorDeployed = Pair(false, Clock.System.now())
            }
        }
        if (this::notificationWaterLevelSensorErrorBDeployed.isInitialized) { //this notification was never tested
            val (deployed, timeDeployed) = notificationWaterLevelSensorErrorDeployed
            Log.i("resetNotifications() wl sensor", timeDeployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationWaterLevelSensorErrorMuteDuration) {
                notificationWaterLevelSensorErrorBDeployed = Pair(false, Clock.System.now())
            }
        }
        if (this::notificationACPowerDeployed.isInitialized) {
            val (deployed, timeDeployed) = notificationACPowerDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationACPowerMuteDuration) {
                notificationACPowerDeployed = Pair(false, Clock.System.now())
            }
        }
        if (this::notificationHighWaterDeployed.isInitialized) {
            val (deployed, timeDeployed) = notificationHighWaterDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationHighWaterMuteDuration) {
                notificationHighWaterDeployed = Pair(false, Clock.System.now())
            }
        }

        if (this::notificationMainRunWarnDeployed.isInitialized) {
            val (deployed, timeDeployed) = notificationMainRunWarnDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > notificationMainRunWarnMuteDuration) {
                notificationMainRunWarnDeployed = Pair(false, Clock.System.now())
            }
        }

        if (this::notificationBackupRan.isInitialized) {
            val (deployed, timeDeployed) = notificationBackupRan
            if (deployed && (Clock.System.now() - timeDeployed) > notificationBackupRanMuteDuration) {
                notificationBackupRan = Pair(false, Clock.System.now())
            }
        }

        if (this::notificationWaterTooLow.isInitialized) {
            val (deployed, timeDeployed) = notificationWaterTooLow
            if (deployed && (Clock.System.now() - timeDeployed) > notificationWaterTooLowMuteDuration) {
                notificationWaterTooLow = Pair(false, Clock.System.now())
            }
        }
        if (this::notificationBattery12Low.isInitialized) {
            val (deployed, timeDeployed) = notificationBattery12Low
            if (deployed && (Clock.System.now() - timeDeployed) > notificationBattery12LowMuteDuration) {
                notificationBattery12Low = Pair(false, Clock.System.now())
            }
        }

        if (this::notificationNoPumpCotnrol.isInitialized) {
            val (deployed, timeDeployed) = notificationNoPumpCotnrol
            if (deployed && (Clock.System.now() - timeDeployed) > notificationNoPumpControlMuteDuration) {
                notificationNoPumpCotnrol = Pair(false, Clock.System.now())
            }
        }
    }



        fun closeMainPumpNotification(view: View) {
            Log.i("closeMainPumpNotification", "starting close main pump notification")
            Log.i("closeMainPumpNotification", mainPumpWarnSilence.toString())
            if (!mainPumpWarnSilence) {
                mainPumpSilenceTime = Clock.System.now()
                mainPumpWarnSilence = true
            }


        }

        fun closeWaterLevelNotification(view: View) {
            Log.i("closeWaterLevelNot", waterLevelWarnSilence.toString())
            if (!waterLevelWarnSilence) {

                waterLevelWarnSilence = true
            }
        }

        fun closeBackupPumpNotification(view: View) {
            Log.i("closebackupPumpNotification", "starting close backup pump notification")
            Log.i("closebackupPumpNotification", backupPumpWarnSilence.toString())
            if (!backupPumpWarnSilence) {
                backupPumpSilenceTime = Clock.System.now()
                backupPumpWarnSilence = true
            }


        }

        fun closeGeneralWarn(view: View) {
            Log.i("closeGeneralPumpNotification", "starting close general warn notification")
            Log.i("closegeneralNotification", generalWarnSilence.toString())
            if (!generalWarnSilence) {

                generalWarnSilence = true
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        private suspend fun readWarningData(warning: String): Pair<Int, String> {
            val prefKey = intPreferencesKey(warning)
            val string = warning + "Time"
            val prefKeyTime = stringPreferencesKey(string)
            val settingsCounterFlowView: Flow<Int> = dataStore.data //read data in saved data store
                .map { settings ->
                    // No type safety.
                    settings[prefKey]
                        ?: 0     //this sets the value to zero (in exampleCounterFlow not datastore) if null
                }
            val settingsCounterFlowTime: Flow<String> = dataStore.data
                .map { settings ->
                    settings[prefKeyTime] ?: LocalDateTime.of(2000, 1, 1, 12, 0).toString()
                }
            val stringToParse = settingsCounterFlowTime.first()
            Log.i("stringToParse", stringToParse)

            return Pair(settingsCounterFlowView.first(), settingsCounterFlowTime.first())


        }

        private fun postFirstRun(firstRun: Boolean) { //this is a post request but i didn't neeed it after all
            var payload: String
            Log.i("firstrunvariable in postfirstrun", firstRun.toString())

            if (firstRun) {
                payload = "fristRun: True"
            } else {
                payload = "firstRun: False"
            }


            Log.i("firstrunvariable in postfirstrun", firstRun.toString())
            val okHttpClient = OkHttpClient()
            val requestBody = payload.toRequestBody()
            val request = Request.Builder()
                .post(requestBody)
                .url("www.jeffs-handyman.net/")
                .build()
            //this is a post request i hope...we will see
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.message?.let { Log.i("ERROR in POST", it) }
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.i("response in POST(((((()))))", response.message)
                }
            })

            Log.i("getDataTake999", "this is after POST")

        }


        private val client = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()


        @RequiresApi(Build.VERSION_CODES.O)
        fun getFromServer(
            url: String?,
            params: Map<String, String>? = null,
            responseCallback: Unit?,
            binding: ActivityMainBinding?
        ) {
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

            val voltage5Reg: Regex = "voltage5.:\\s*(\\d*)".toRegex()
            val voltage12Reg: Regex = "voltage12.:\\s*(\\d*)".toRegex()
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
            if (match?.let { checkPumpControlRunning(it) } == false) { //if pumpcontrol is not running
                pumpControlActive = false
                Log.i("generalWarnSilence", generalWarnSilence.toString())
                if(!generalWarnSilence){
                    if (binding != null) {
                        binding.generalErrorView = true
                        binding.generalErrorText = "No Pump Control Software!\n The pumps will not run!"
                    }

                else{
                    if(charging5_!! && warningVisibilities["serverErrorWarning"]!!.first == 0){
                        if (warningVisibilities["noWaterWarning"]?.first == 1){
                            if (binding != null && mainRunning_) {
                                binding.generalErrorView= true

                                 binding.generalErrorText = "Pump running dry,\n Please Check!"
                            }


                        }
                        else if (binding != null) {
                            binding.generalErrorView = false
                        }

                        }
                    }


            }
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
            //Log.i("voltage5Str", voltage5Str!!)
            match = responseString?.let { voltage12Reg.find(it) }
            val voltage12Str = match?.groupValues?.get(1)
            match = responseString?.let { charging5Reg.find(it) }
            val charging5Str = match?.groupValues?.get(1)

            //  match = re

            // val isPumpControlRunning = PumpControlRunningCheck()

            applyRelayData(mainRunningStr, backupRunningStr)
            applyBatteryData(
                voltage12Str = voltage12Str,
                voltage5Str = voltage5Str,
                charging5Str = charging5Str
            )
            if (voltage12Str != null) {
                Log.i("voltage12", voltage12Str)
            }

            applyWaterLevel(
                highFloodingStr,
                midFloodingStr,
                lowFloodingStr
            )
        }


        private var mainRunWarnVis: Boolean = true
        private var backupRunWarnVis: Boolean = true

        @RequiresApi(Build.VERSION_CODES.O)
        private fun applyMainPumpWarn(mainRunWarnStr: String) {
            Log.i("applymainpumpwarn", "starting apply main pump warn")
            if (mainPumpWarnSilence) {
                val now = Clock.System.now()
                if ((now - mainPumpSilenceTime) > 30.minutes) {
                    mainPumpWarnSilence = false
                }
            }
            Log.i("applyMainPumpWarn", mainPumpWarnSilence.toString())
            if (mainRunWarnStr == "true") {
                mainRunWarnVis = true
                warningVisibilities["mainRunTimeWarning"] = Pair(1, LocalDateTime.now())
            } else {
                mainRunWarnVis = false
            }
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
            } else if (backupRunningStr == "true") {
                backupRunningApply = true
            } else {
                Log.i("Failure in applyRelayData", "failure in applyRelayData")
            }
            Log.i("applyRelayData", "done with apply relay data")
            Log.i("mainRunning", mainRunningApply.toString())
            Log.i("backupRunning", backupRunningApply.toString())
            mainRunning_ = mainRunningApply
            backupRunning_ = backupRunningApply

        }

        private fun applyBatteryData(
            voltage5Str: String?,
            voltage12Str: String?,
            charging5Str: String?

        ) {
            var voltage5Apply: Int
            var voltage12Apply: Int
            var charging5Apply: Boolean


            voltage5Apply = voltage5Str?.toInt()!!
            voltage12Apply = voltage12Str?.toInt()!!

            voltage12_ = voltage12Apply
            voltage5_ = voltage5Apply


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

        @RequiresApi(Build.VERSION_CODES.O)
        private fun checkPumpControlRunning(dateTimeMatch: MatchResult): Boolean {
            Log.i("checkingPumpControl", dateTimeMatch.toString())
            val year = dateTimeMatch?.groupValues?.get(1)!!.toInt()
            val month = dateTimeMatch?.groupValues?.get(2)!!.toInt()
            val day = dateTimeMatch?.groupValues?.get(3)!!.toInt()
            val hour = dateTimeMatch?.groupValues?.get(4)!!.toInt()
            val minute = dateTimeMatch?.groupValues?.get(5)!!.toInt()
            val second = dateTimeMatch?.groupValues?.get(6)!!.toInt()

            val timeStamp = LocalDateTime.of(year, month, day, hour, minute, second)
            Log.i("timestampCheckPump", timeStamp.toString())
            Log.i(
                "checkPUmpControl",
                java.time.Duration.between(timeStamp, LocalDateTime.now()).toString()
            )

            if (java.time.Duration.between(
                    timeStamp,
                    LocalDateTime.now()
                ) > java.time.Duration.ofMinutes(1)
            ) {
                Log.i("noPumpControl!", "Pump Control is not running")
                warningVisibilities["noPumpControlWarning"] = Pair(1, LocalDateTime.now())

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val (deployed, timeDeployed) = notificationNoPumpCotnrol
                if(!deployed) {
                    notificationBuilder(
                        getString(R.string.noPumpControlNotificationTitle),
                        getString(R.string.noPumpControlNotificationContent),
                        "max",
                        getString(R.string.pumpProblemsChannel),
                        getString(R.string.noACPowerNotificationID),
                        notificationManager
                    )
                    notificationNoPumpCotnrol = Pair(true, Clock.System.now())

                }
            return false


            }
            else    {return  true}
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun applyWaterLevel(
            highFloodingStr: String?,
            midFloodingStr: String?,
            lowFloodingStr: String?
        ) {

            Log.i("applyingwaterlevel", "waterlevelapplying")
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            var highFloodingApply: Boolean = false
            var midFloodingApply: Boolean = false
            var lowFloodingApply: Boolean = false
            val waterLevelWarningBox = findViewById<TextView>(R.id.waterLevelWarning)
            //val triangleWarningImageView = findViewById<ImageView>(R.id.waterLevelWarningTriangle)
            //val xToClose = findViewById<ImageView>(R.id.xToCloseWaterLevelPumpErrorImageView)
            if (highFloodingStr != null) {
                Log.i("highFlood@", highFloodingStr)
            }
            if (highFloodingStr == "false") {
                Log.i("highFloodingStrApplyWater1", highFloodingStr)
                highFloodingApply = false
            } else if (highFloodingStr == "true") {
                Log.i("highFloodingStrApplyWater2", highFloodingStr)
                highFloodingApply = true
                if (midFloodingStr == "false" || lowFloodingStr == "false") {

                    warningVisibilities["sensorErrorWarning"] = Pair(1, LocalDateTime.now())
                    Log.i(
                        "ERROR: Raise notification",
                        " Water Level Sensor Error:High Flooding is true, but others are false"
                    )
                    val (deployed, timeDeployed) = notificationWaterLevelSensorErrorDeployed
                    if (!deployed) {
                        notificationBuilder(
                            "SumpPump WaterLevel Sensor Error",
                            "Error In water level sensor\nhigh=true mid/low = false",
                            "high",
                            getString(R.string.systemIssuesChannelID),
                            getString(R.string.waterLevelSensorErrorNotificationID),
                            notificationManager
                        )
                        notificationWaterLevelSensorErrorDeployed = Pair(true, Clock.System.now())
                    }
                }

            }



            Log.i("MIDFLOODING STRING", midFloodingStr.toString())

            if (midFloodingStr == "false") {
                midFloodingApply = false
            } else if (midFloodingStr == "true") {
                midFloodingApply = true
                if (lowFloodingStr == "false") {
                    waterLevelWarningBox.visibility = View.VISIBLE
                    warningVisibilities["sensorErrorWarning"] = Pair(1, LocalDateTime.now())


                    Log.i(
                        "ERROR: Raise notification",
                        "Water Level Sensor Error:Mid Sensor is true, but Low  is false"
                    )
                    val intent = Intent(this, MainActivity::class.java)
                    val (deployed, timeDeployed) = notificationWaterLevelSensorErrorBDeployed
                    if (!deployed) {
                        notificationBuilder(
                            "SumpPump WaterLevel Sensor Error",
                            "Error In water level sensor\nmid=true low = false",
                            "high",
                            getString(R.string.systemIssuesChannelID),
                            getString(R.string.waterLevelSensorErrorNotificationID),
                            notificationManager
                        )
                        notificationWaterLevelSensorErrorBDeployed = Pair(true, Clock.System.now())
                    }
                }
            } else {
                Log.i("Failure in applyWaterlevel", "failure in apply waterlevel mid")

            }




            if (lowFloodingStr == "false") {
                lowFloodingApply = false
            } else if (lowFloodingStr == "true") {
                lowFloodingApply = true
            } else {
                Log.i("Failure in applyWaterlevel", "failure in apply waterlevel low")
            }

            Log.i("applyWaterLevelData", "done with apply waterLEvel data")
            Log.i("highFLooding", highFloodingApply.toString())
            Log.i("midFlooding", midFloodingApply.toString())
            Log.i("lowFlooding", lowFloodingApply.toString())

            highFlooding_ = highFloodingApply
            midFlooding_ = midFloodingApply
            lowFlooding_ = lowFloodingApply
        }


        fun readFile(file: String) {

            var reader: BufferedReader? = null

            try {
                reader = BufferedReader(FileReader(file))
                var line: String?
                var lines: Array<String?> = arrayOf("")
                while (reader.readLine().also { line = it } != null) {
                    // Process each line
                    println(line)
                    lines += line
                }
            } catch (e: Exception) {
                println("An error occurred: ${e.message}")
            } finally {
                try {
                    reader?.close()
                } catch (e: Exception) {
                    println("An error occurred while closing the file: ${e.message}")
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun checkServerError(binding: ActivityMainBinding?, notificationManager: NotificationManager){
            if (!bootRun){
                if (serverError.first){

                    if (java.time.Duration.between(serverError.second, LocalDateTime.now()) > java.time.Duration.ofMinutes(5)){
                        Log.i("generalWarnSilence", generalWarnSilence.toString())
                        val (deployed, timeDeployed) = notificationServerErrorDeployed
                        Log.i("server error deployed", deployed.toString())
                        if (!deployed){

                            notificationBuilder("Server Error", "Error In Server\nNo Data is being received","high", getString(R.string.systemIssuesChannelID),getString(R.string.serverErrorNotificationID), notificationManager)
                            notificationServerErrorDeployed = Pair(true, Clock.System.now())
                        }
                        if(!generalWarnSilence){
                            if (binding != null) {
                                binding.generalErrorView = true
                                binding.generalErrorText = "Error in Server.\n NO Data"
                            }
                        }

                    }
                }
    }
}
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}