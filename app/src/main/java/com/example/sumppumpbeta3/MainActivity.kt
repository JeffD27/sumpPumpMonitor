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
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest

import com.example.sumppump3.R
import com.example.sumppump3.databinding.ActivityMainBinding
import com.squareup.moshi.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
val notificationStrings = listOf("serverError", "sensorError", "noPower", "highWater", "mainRunTime", "backupRun", "noWater", "lowBattery12", "noPumpControl", "mainRunning" )
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
var notificationMainRunningMuteDuration: kotlin.time.Duration = 30.minutes


var mainPumpRuntimeOver10: Boolean = true
var backupRunWarnVis: Boolean = true

var voltage12Low: Pair<Boolean, Instant> = Pair(false, Clock.System.now())

var serverError: Pair<Boolean, Instant> = Pair(false, Clock.System.now())
var responseStringReceived = false

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

private lateinit var job: Job
private val coroutineScope = CoroutineScope(Dispatchers.Main)


var bootRun: Boolean = true
var mainRunning_: Boolean = false
var mainRunTime_ : String = "Loading..."
var backupRunTime_ : String = ""
var mainTimeStartedStr : String = "Loading..."
var backupTimeStartedStr: String = "Loading..."
var backupRunning_: Boolean? = true
var runningDry: Boolean? = false

lateinit var settingsCounterFlowView: Flow<Int>

var voltage12_: Int = 250
var voltage5_: Int = 250
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

var fullScreenDeployedTime: Instant = Clock.System.now()



open class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        BatteryOptimizationHelper.requestBatteryOptimizationExemption(this)
        NotificationChannels()
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

        //this is for debugging..check for things that weren't closed properly
        try {
            Class.forName("dalvik.system.CloseGuard")
                .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
                .invoke(null, true)
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }

        adjustButtonSettings()
        //Log.i("FUllScreenTest", "starting full screen test" ) //it passed
        //val intent = Intent(this, FullScreenNotificationActivity::class.java)
        startActivity(intent)
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






        defaultMuteTimes["serverError"] = 2.hours
        defaultMuteTimes["sensorError"] = 1.days
        defaultMuteTimes["noPower"] = 1.hours
        defaultMuteTimes["highWater"] = 2.hours
        defaultMuteTimes["mainRunTime"] = 10.minutes
        defaultMuteTimes["backupRun"] = 10.minutes
        defaultMuteTimes["noWater"] = 10.minutes
        defaultMuteTimes["lowBattery12"] = 1.days
        defaultMuteTimes["noPumpControl"] = 12.hours
        defaultMuteTimes["mainRunning"] = 30.minutes

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
        Log.i("callStartRepeating", "calling startrepeatingServer...")
        startRepeatingServerCalls(activity, binding)


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
            ) < java.time.Duration.ofDays(2)
        ) {
            return 1

        } else {
            return 0

        }

    }
    private fun adjustButtonSettings(){
        val constraintLayout = findViewById<ConstraintLayout>(R.id.mainConstraint)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        when {
            findViewById<Group>(R.id.generalWarningGroup).visibility == View.VISIBLE -> {
                constraintSet.connect(R.id.buttonSettings, ConstraintSet.TOP, R.id.generalWarning, ConstraintSet.BOTTOM, 20)
            }
            findViewById<Group>(R.id.backupPumpRunWarningGroup).visibility == View.VISIBLE -> {
                constraintSet.connect(R.id.buttonSettings, ConstraintSet.TOP, R.id.backupPumpRunWarning, ConstraintSet.BOTTOM, 20)
            }
            else -> {
                Log.i("adjustButtonSettings", "else")
                constraintSet.connect(R.id.buttonSettings, ConstraintSet.TOP, R.id.divider2, ConstraintSet.BOTTOM, 80)
            }
        }

        constraintSet.applyTo(constraintLayout)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRepeatingServerCalls(activity: Activity, binding: ActivityMainBinding) {
        Log.i("startRepeatingServerCalls", "starting Repeated Server calls")
        val context = this
        job = coroutineScope.launch {


            while (isActive) {  // While the job is active
                Log.i("startingLoop", "loop is starting in mainActivity")

                Log.i("serverMuteInStartRepeating", notificationServerErrorMuteDuration.toString())
                // Enqueue the work request

                //WorkManager.getInstance(applicationContext).enqueue(callServerWorkerOnce) //this needs to a periodic work request...not in a loop
                LoopHandler().run(context)
                val response = CallServer().run()
                if (response != null) {
                    EvaluateResponse().onCreate(context, response, activity)
                }
                else{EvaluateResponse().onCreate(context, "null", activity)}
                applyWarningVisibilities()

                checkNoWaterPumpRunning(activity, binding)
                checkServerError(activity, binding)
                checkPumpRuntimeBackupRun(activity, binding)
                checkGeneralErrors(activity, binding)
                checkVoltages(activity, binding)
                checkPumpsRunning(activity, binding)
                checkWaterLevel(activity, binding)
                assessViewWarnings()
                adjustButtonSettings()
                Log.i("delaying", "delay coming")

                delay(1000) // Delay for 1 seconds before the next call
            }
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
        for (warning in warningStrings) {
            Log.i("warning initiateWarn", warning)//intiates warningVisibilities
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


                val restOfTimeString = ":00Z" //i removed the z at the end
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
                val timeStampKotlin = Instant.parse(timeStringParsed)


                val visibility = getVisibility(timeStampKotlin)

                warningVisibilities[warning] = Pair(visibility, timeStampKotlin)
                Log.i("visibility", visibility.toString())
                Log.i("timeString", timeString)
            }

        }
    }


    private fun checkWaterLevel(activity: Activity, binding: ActivityMainBinding){
        val waterLevelWarningBox = findViewById<TextView>(R.id.waterLevelWarning)
        Log.i("checkWaterLevel", "initiating check water level")
        if (sensorError_ == true){
            Log.i("checkWaterLevel", "sensor Error True")
            waterLevelWarningBox.visibility = View.VISIBLE
            warningVisibilities["sensorErrorWarning"] = Pair(1, Clock.System.now())
        }
        else{warningVisibilities["sensorErrorWarning"] = Pair(0, Clock.System.now())}
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
            binding.waterLevelWarnView=false
            warningVisibilities["highWaterWarning"] = Pair(0, Clock.System.now())}
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



            binding.backupRunTime = backupRunTime_
            binding.backupRunning = "Pump is Running"

            binding.backupRunningBoxColor =
                ContextCompat.getColor(activity, R.color.green)
            Log.i("backupRunningColor", binding.backupRunningBoxColor.toString())
            backupRunTime_.let{binding.backupRunTime}
            backupPumpWarnSilence = false}

        if (backupRunning_ == false) {
            binding.backupRunning = "Pump Is Not Running"
            binding.backupRunTime = "Last Ran on:\n $backupTimeStartedStr"
            binding.backupRunningBoxColor =
                ContextCompat.getColor(activity, R.color.red)

            Log.i("mainRunningColor", binding.mainRunningBoxColor.toString())
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkVoltages(activity: Activity, binding: ActivityMainBinding){
        if(!charging5_!!){
            Log.i("generalWarnCharging5", generalWarnSilence.toString())

            warningVisibilities["noPowerWarning"] = Pair(1, Clock.System.now())

            binding.acPowerSmallBatteryImage = ContextCompat.getDrawable(activity, R.drawable.noacplug) //draw no plug


            if (!generalWarnSilence){
                binding.generalErrorView = true

                binding.generalErrorText = "USB disconnected\n / no power!"
            }
        }
        else{binding.acPowerSmallBatteryImage =
            activity.let { ContextCompat.getDrawable(it, R.drawable.acplug) }//if charger is connect draw plug
            warningVisibilities["noPowerWarning"] = Pair(0, Clock.System.now())
        }
        Log.i("checkVoltages12", voltage12_.toString())
        if (voltage12_ != 250){
            binding.battery12vText = "$voltage12_%"}
        else{
            binding.battery12vText = "?%"
        }



        if (voltage12_ < 95){
            warningVisibilities["lowBattery12Warning"] = Pair(1, Clock.System.now())
            binding.battery12TextBGColor = ContextCompat.getColor(activity, R.color.red)


            Log.i("checkVoltages()", "low voltage12")
        }

        else{
            binding.battery12TextBGColor = ContextCompat.getColor(activity, R.color.green)
        }


        Log.i("voltage5_", voltage5_.toString())
        //val sleepTime = java.time.Duration.ofMinutes(1)

        val currentTime = Clock.System.now().toJavaInstant()
        Log.i("current_herewegoagain", currentTime.toString())
       // Log.i("sleeptime", sleepTime.toString() )
        checkBatteryVoltsTime?.let{}?: run{ checkBatteryVoltsTime = currentTime - java.time.Duration.ofMinutes(40)} // if checkBatteryVoltsTime doesn't exist, create it and make it equal to 40 minutes ago so that way it is almost certiantly greater than sleeptime and we check the battery voltage
        Log.i("checkBatteryVoltsTime", checkBatteryVoltsTime.toString())
        val duration = java.time.Duration.between(checkBatteryVoltsTime!!, currentTime) // how long has it been since we check the battery voltage. we only want to check every sleeptime or it jumps like crazy
        Log.i("durationBatteryVolts", duration.toString())
        Log.i("duration", duration.toString())
       // Log.i("sleeptimeBool", (duration > sleepTime).toString())
        //if (duration > sleepTime || binding.battery5TextView.text == "0%" || runThrough < 6){

            //Log.i("durationBatteryVolts2", sleepTime.toString())
        if (voltage5_ != 250){
            binding.battery5vText = "$voltage5_%"}
        else{ binding.battery5vText = "?%"}
        Log.i("votage5er", voltage5_.toString())
        checkBatteryVoltsTime = Clock.System.now().toJavaInstant()

        if(voltage5_ < 70){

            binding.battery5TextBGColor = ContextCompat.getColor(activity, R.color.red)

        }
        else {

            binding.battery5TextBGColor = ContextCompat.getColor(activity, R.color.green)

        }

    }

    private fun checkGeneralErrors(activity: Activity, binding: ActivityMainBinding){
        Log.i("pumpControlCheckGen", pumpControlActive.toString())
        Log.i("noPowerWarning.first", warningVisibilities["noPowerWarning"]?.first.toString() )
        Log.i("noPowerWarning.first", warningVisibilities["serverError"]?.first.toString() )
        Log.i("noPowerWarning.first", warningVisibilities["noWaterWarning"]?.first.toString() )
        if (!pumpControlActive){ //gets set in evaluateResponse
            if(!generalWarnSilence){
                binding.generalErrorView = true
                binding.generalErrorText = "No Pump Control Software!"
            }
        }

        else if (warningVisibilities["noPowerWarning"]?.first == 0 && warningVisibilities["serverError"]?.first == 0){
                if (warningVisibilities["noPumpControlWarning"]?.first==0 && warningVisibilities["noWaterWarning"]?.first == 0){
                        closeGeneralWarn(null)
                }
        }

    }




    private fun checkPumpRuntimeBackupRun(activity: Activity, binding: ActivityMainBinding) {
        Log.i("mainPumpRuntimeOver10", mainPumpRuntimeOver10.toString())

        if (mainPumpRuntimeOver10) { //this gets updated in applyMainPumpWarn...if pump has run > 10 min gets eval in python server side as boolean. boolean is applied in applyMainPumpWarn
            if (!mainPumpWarnSilence) {
                binding.mainRunWarnView = true
                warningVisibilities["mainRunTimeWarning"] = Pair(1, Clock.System.now())
            }
        }
        else {
            binding.mainRunWarnView = false
            warningVisibilities["mainRunTimeWarning"] = Pair(0, Clock.System.now())
        }
        if (backupRunWarnVis && !backupPumpWarnSilence) {
            binding.backupRunWarnView = true
        } else {
            binding.backupRunWarnView = false
        }
    }


    private fun checkServerError(activity: Activity, binding: ActivityMainBinding){
        val radioTowerSymbol = findViewById<ImageView>(R.id.radioTowerImageView)
        if (!responseStringReceived){
            Log.i("checkServerError", "removing radio tower")
            radioTowerSymbol.visibility = INVISIBLE
        }
        else{
            Log.i("checkServerError", "radio towerView=true")
            radioTowerSymbol.visibility = VISIBLE
        }
        if (serverError.first){

            if(!generalWarnSilence){
                binding.generalErrorView = true
                binding.generalErrorText = "Error in Server.\n NO Data"
                warningVisibilities["serverError"] = Pair(1, Clock.System.now()

                )
            }

        }
        else{
            warningVisibilities["serverError"] = Pair(0, Clock.System.now())
            radioTowerSymbol.visibility = VISIBLE
        }


    }
    private fun checkNoWaterPumpRunning(activity: Activity, binding: ActivityMainBinding){
        if (mainRunning_|| backupRunning_ == true){
            Log.i("mainRunning nowater", mainRunning_.toString())
            Log.i("backupRunning nowater", backupRunning_.toString())
            if (lowFlooding_ == false){
                warningVisibilities["noWaterWarning"] = Pair(1, Clock.System.now())
                binding.generalErrorView= true
                binding.generalErrorText = "Pump running dry,\n Please Check!"

            }
        }
        else{
            binding.generalErrorView = false
            warningVisibilities["noWaterWarning"] = Pair(0, Clock.System.now())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun applyWarningVisibilities(){
        runBlocking {
            //set warnings page data

            dataStore.edit { settings -> //write data to saved data store
                for(warnVis in warningVisibilities) {
                    val warnStart = warnVis.value.second
                    Log.i("warnvis", warnVis.key)
                    val timeStampKey = warnVis.key + "Time"
                    settings[stringPreferencesKey(timeStampKey)] = warnStart.toString()
                    Log.i("duration", java.time.Duration.between(Clock.System.now().toJavaInstant(), warnVis.value.second.toJavaInstant()).toString())
                    val visibility = getVisibility(warnVis.value.second)
                    settings[intPreferencesKey(warnVis.key)] = visibility



                }
            }

        }
    }
    private fun assessViewWarnings(){
        if ( warningVisibilities["mainRunTimeWarning"]?.first == 0){
            closeMainPumpWarn(null)
        }

        if (warningVisibilities["highWaterWarning"]?.first  == 0){
            if (warningVisibilities["sensorErrorWarning"]?.first == 0){
                closeWaterLevelWarn(null)
            }
        }

        if (warningVisibilities["backupRunWarning"]?.first == 0){
            closeBackupPumpWarn(null)
        }
    }
    fun closeGeneralWarn(view: View?) {
        val view = findViewById<Group>(R.id.generalWarningGroup)
        view.visibility = GONE
        adjustButtonSettings()
    }

    fun closeBackupPumpWarn(view: View?){
        val view = findViewById<Group>(R.id.backupPumpRunWarningGroup)
        view.visibility = GONE
    }

    fun closeMainPumpWarn(view: View?){
        val view = findViewById<Group>(R.id.mainPumpRunWarningGroup)
        view.visibility = GONE
    }
    fun closeWaterLevelWarn(view: View?){
        val view = findViewById<Group>(R.id.WaterLevelWarningGroup)
        view.visibility = GONE
    }
    fun closeMainPumpNotification(view: View?) {
        val view = findViewById<Group>(R.id.mainPumpRunWarningGroup)
        view.visibility = GONE
    }
}











