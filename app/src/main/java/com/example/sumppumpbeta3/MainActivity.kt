package com.example.sumppumpbeta3
//import com.example.sumppumpbeta3.R
//import com.example.sumppumpbeta3.databinding.ActivityMainBinding
//import kotlinx.datetime.ZoneOffset
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
import android.app.Activity
import android.app.NotificationManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
import kotlinx.datetime.toJavaInstant

import java.time.temporal.Temporal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@RequiresApi(Build.VERSION_CODES.O)
var preServerError: Pair<Boolean, Instant> = Pair(false, Clock.System.now())
val durationConvertDict = LinkedHashMap<String, kotlin.time.Duration>()
val spinnerStringDict = LinkedHashMap<Spinner, String>()
val spinnerDurationDict = LinkedHashMap<Spinner, kotlin.time.Duration>()

val durationPositionInt = LinkedHashMap<kotlin.time.Duration, Int>()
val notificationStrings = listOf("serverError", "sensorError", "noPower", "highWater", "mainRunTime", "backupRun", "noWater", "lowBattery12", "noPumpControl" )
var persistentServerError: Boolean = false
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


var mainPumpRuntimeOver10: Boolean = true
var backupRunWarnVis: Boolean = true



var serverError: Pair<Boolean, Instant> = Pair(false, Clock.System.now())

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

    val warningVisibilities = LinkedHashMap<String, Pair<Int, Instant>>()


var bootRun: Boolean = true
var mainRunning_: Boolean = false
var mainRunTime_ : String = "Loading..."
var backupRunTime_ : String = ""
var mainTimeStartedStr : String = "Loading..."
var backupTimeStartedStr: String = "Loading..."
var backupRunning_: Boolean? = true


lateinit var settingsCounterFlowView: Flow<Int>

var voltage12_: Int = 0
var voltage5_: Int = 0
var charging5_: Boolean? = true

var checkBatteryVoltsTime: Temporal? = null
var pumpControlActive = false // false means we do no have pump control
var highFlooding_: Boolean? = false
var midFlooding_: Boolean? = false
var lowFlooding_: Boolean? = false
var sensorError_: Boolean? = false
var mainPumpWarnSilence = false
var backupPumpWarnSilence = false
var generalWarnSilence = false
var waterLevelWarnSilence = false
lateinit var mainPumpSilenceTime: Instant
lateinit var backupPumpSilenceTime:Instant




open class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.i("mainActivity", "onCreate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)




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

        initiateWarningVisibilities()//also applies saved data
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
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        checkNoWaterPumpRunning(activity, binding)
        checkServerError(activity, binding)
        checkPumpRuntimeBackupRun(activity, binding)
        checkGeneralErrors(activity, binding)
        checkVoltages(activity, binding)
        checkPumpsRunning(activity, binding)
        checkWaterLevel(activity, binding)
        //callServer(activity, binding, notificationManager)


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
    private fun getVisibility(timeStamp: Instant): Int {
        if (java.time.Duration.between(
                timeStamp.toJavaInstant(),
                Clock.System.now().toJavaInstant()
            ) < java.time.Duration.ofDays(5)
        ) {
            return 1

        } else {
            return 0

        }

    }


    //private fun readFromData()


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun readWarningData(warning: String): Pair<Int, String> {
        val prefKey = intPreferencesKey(warning)
        val string = warning + "Time"
        val prefKeyTime = stringPreferencesKey(string)
        settingsCounterFlowView = dataStore.data //read data in saved data store
            .map { settings ->
                // No type safety.
                settings[prefKey]
                    ?: 0     //this sets the value to zero (in exampleCounterFlow not datastore) if null
            }


        val settingsCounterFlowTime: Flow<String> = dataStore.data
            .map { settings ->
                settings[prefKeyTime] ?: Instant.parse("2000-01-01T12:35:24.00Z").toString()
            }
        val stringToParse = settingsCounterFlowTime.first()
        Log.i("stringToParse", stringToParse)

        return Pair(settingsCounterFlowView.first(), settingsCounterFlowTime.first())


    }

/*
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
*/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initiateWarningVisibilities(){ //this also will apply time stamps based on saved settings data
        for (warning in warningStrings) { //intiates warningVisibilities
            runBlocking {  //com.example.sumppumpbeta3.warningVisibilities[warning]
                val data = readWarningData(warning) //get saved settings data
                val timeString = data.second
                val regex = Regex("(\\d{4}).(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})")
                val match = regex.find(timeString)!!
                val year = match.groupValues[1]
                val month = match.groupValues[2]
                val day = match.groupValues[3]
                val hour = match.groupValues[4]
                val minute = match.groupValues[5]
                Log.i("timeString!", timeString)
                Log.i("timeString!", year)
                Log.i("timeString!", month)
                Log.i("timeString!", day)
                Log.i("timeString!", hour)
                Log.i("timeString!", minute)


                val restOfTimeString = ":00.99Z"
                val timeStringParsed = String.format(
                    "%s-%s-%sT%s:%s" + "%s",
                    year,
                    month,
                    day,
                    hour,
                    minute,
                    restOfTimeString
                )
                Log.i("timeStringParsed", timeStringParsed)
                val timeStamp = Instant.parse(timeStringParsed)
                val visibility = getVisibility(timeStamp)

                warningVisibilities[warning] = Pair(visibility, timeStamp)
                Log.i("timeString", timeString)
            }

        }
    }


    private fun checkWaterLevel(activity: Activity, binding: ActivityMainBinding){
        val waterLevelWarningBox = findViewById<TextView>(R.id.waterLevelWarning)
        if (sensorError_ == true){
            waterLevelWarningBox.visibility = View.VISIBLE
            warningVisibilities["sensorErrorWarning"] = Pair(1, Clock.System.now())
        }
        if (highFlooding_ == true) {
            warningVisibilities["highWaterWarning"] = Pair(1, Clock.System.now())
            if (binding != null && activity != null) {
                binding.waterLevelImage =
                    ContextCompat.getDrawable(activity, R.drawable.water_high)
                binding.waterLevelText = "High\n(100%)"
            }
            if( midFlooding_ == false || lowFlooding_ == false  ) {
                    binding.waterLevelWarnView = !waterLevelWarnSilence}
        else{
            binding.waterLevelWarnView=false}
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
    }

    private fun checkPumpsRunning(activity: Activity, binding: ActivityMainBinding){
        if (mainRunning_ == true) { //main pump is running
            if (binding != null && activity != null) {
                binding.mainRunning = "Pump is Running"

                binding.mainRunTime = mainRunTime_
                binding.mainRunningBoxColor =
                    ContextCompat.getColor(activity, R.color.green)
            }
            Log.i("mainRunningColor", binding.mainRunningBoxColor.toString())


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

            Log.i("evaluateResp", "backup Running#")

            warningVisibilities["backupRunWarning"] = Pair(1, Clock.System.now())


        }
        if (binding != null && activity != null){
            binding.backupRunTime = backupRunTime_
            binding.backupRunning = "Pump is Running"

            binding.backupRunningBoxColor =
                ContextCompat.getColor(activity, R.color.green)
            Log.i("backupRunningColor", binding.backupRunningBoxColor.toString())
            backupRunTime_.let{binding.backupRunTime}
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


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkVoltages(activity: Activity, binding: ActivityMainBinding){
        if(!charging5_!!){
            Log.i("generalWarnCharging5", generalWarnSilence.toString())

            warningVisibilities["noPowerWarning"] = Pair(1, Clock.System.now())
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
            }
            if (binding != null) { binding.battery12vText = "$voltage12_%"}

        }

        if (voltage12_ < 95){
        warningVisibilities["lowBattery12Warning"] = Pair(1, Clock.System.now())


        }

        else{
            if (binding != null && activity != null){
                binding.battery12TextBGColor = ContextCompat.getColor(activity, R.color.green)
            }
        }
        binding.acPowerSmallBatteryImage =
            activity?.let { ContextCompat.getDrawable(it, R.drawable.acplug) }

        Log.i("voltage5_", voltage5_.toString())
        val sleepTime = java.time.Duration.ofMinutes(1)

        val currentTime = Clock.System.now().toJavaInstant()
        checkBatteryVoltsTime?.let{}?: run{ checkBatteryVoltsTime = currentTime - java.time.Duration.ofMinutes(40)} // if checkBatteryVoltsTime doesn't exist, create it and make it equal to 40 minutes ago so that way it is almost certiantly greater than sleeptime and we check the battery voltage
        Log.i("checkBatteryVoltsTime", checkBatteryVoltsTime.toString())
        val duration = java.time.Duration.between(checkBatteryVoltsTime!!, currentTime) // how long has it been since we check the battery voltage. we only want to check every sleeptime
        Log.i("durationBatteryVolts", duration.toString())
        Log.i("sleeptime", sleepTime.toString())
        Log.i("sleeptimeBool", (duration > sleepTime).toString())
        if (binding != null && activity != null){
            if (duration > sleepTime || binding.battery5TextView.text == "0%" ){
                Log.i("durationBatteryVolts2", sleepTime.toString())
                if (binding != null){binding.battery5vText = "$voltage5_%"}
                checkBatteryVoltsTime = Clock.System.now().toJavaInstant()

                if(voltage5_ < 70){

                    binding.battery5TextBGColor = ContextCompat.getColor(activity, R.color.red)

                }
                else {

                    binding.battery5TextBGColor = ContextCompat.getColor(activity, R.color.green)

                }
            }
        }
    }

    private fun checkGeneralErrors(activity: Activity, binding: ActivityMainBinding){
        if (!pumpControlActive){ //gets set in evaluateResponse
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
                    else{
                        if (warningVisibilities["serverErrorWarning"]!!.first == 0 && pumpControlActive) {
                            if (warningVisibilities["noWaterWarning"]!!.first == 0) {
                                binding.generalErrorView = false
                            }
                        }
                    }
                }
            }


        }
    }

    private fun checkPumpRuntimeBackupRun(activity: Activity, binding: ActivityMainBinding){

        if (mainPumpRuntimeOver10) { //this gets updated in applyMainPumpWarn...if pump has run > 10 min gets eval in python server side as boolean. boolean is applied in applyMainPumpWarn
            if (!mainPumpWarnSilence) {
                binding.mainRunWarnView = true
            } else {
                binding.mainRunWarnView = false
            }
            if (backupRunWarnVis && !backupPumpWarnSilence) {
                binding.backupRunWarnView = true
            } else {
                binding.backupRunWarnView = false
            }
        }

    }
    private fun checkServerError(activity: Activity, binding: ActivityMainBinding){

        if (persistentServerError){
            if(!generalWarnSilence){
                if (binding != null) {
                    binding.generalErrorView = true
                    binding.generalErrorText = "Error in Server.\n NO Data"
                }
            }
        }

    }
    private fun checkNoWaterPumpRunning(activity: Activity, binding: ActivityMainBinding){
        if (mainRunning_|| backupRunning_ == true){
            if (lowFlooding_ == false){
                warningVisibilities["noWaterWarning"] = Pair(1, Clock.System.now())
                if (binding != null) {
                    if( mainRunning_){
                        binding.generalErrorView= true
                        binding.generalErrorText = "Pump running dry,\n Please Check!"
                    }
                    if (!mainRunning_ && binding.generalErrorText == "Pump running dry,\n Please Check!"){
                        binding.generalErrorView = false
                    }
                }
            }
        }
    }
}











