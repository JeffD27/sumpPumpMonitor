package com.example.sumppumpbeta3;
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.sumppump3.R
import com.example.sumppump3.databinding.ActivityMainBinding
//import com.example.sumppumpbeta3.R
//import com.example.sumppumpbeta3.databinding.ActivityMainBinding
import com.squareup.moshi.Json
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
//import kotlinx.datetime.ZoneOffset
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.notify
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.Thread.sleep
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

//import kotlin.time.Duration


/*import com.example.sumppumpmonitor.ui.theme.SumpPumpMonitorTheme*/
/*import androidx.navigation.compose.rememberNavController*/
/*http://127.0.0.1:5000*/






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

class MainActivity : ComponentActivity() {
    private var mainRunning_: Boolean = false
    private var backupRunning_: Boolean? = true

    private var voltage12_: Float? = 0.00f
    private var voltage5_: Float? = 0.00f
    private var charging5_: Boolean? = false

    private var highFlooding_: Boolean? = false
    private var midFlooding_: Boolean? = false
    private var lowFlooding_: Boolean? = false
    private var mainPumpWarnSilence = false
    private var backupPumpWarnSilence = false
    private var generalWarnSilence = false
    private lateinit var mainPumpSilenceTime: Instant
    private lateinit var backupPumpSilenceTime:Instant
    private  var notificationManager: NotificationManager? = null
//the following are for resetting notifications
    private lateinit var notificationServerErrorDeployed: Pair<Boolean, Instant> //to calculate if notification needs to be reset <if deployed, time deployed>
    private lateinit var notifactionWaterLevelSensorErrorDeployed: Pair<Boolean, Instant>
    private lateinit var notifactionWaterLevelSensorErrorBDeployed: Pair<Boolean, Instant>
    private lateinit var notificationACPowerDeployed: Pair<Boolean, Instant>
    private lateinit var notificationHighWaterDeployed: Pair<Boolean, Instant>
    private lateinit var notificationMainRunWarnDeployed: Pair<Boolean, Instant>
    private lateinit var notificationBackupRan: Pair<Boolean, Instant>
    private lateinit var notificationWaterTooLow: Pair<Boolean, Instant>
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //val binding: DataBindingUtil.inflate(layoutInflater, R.layout.list_item, viewGroup, false)
        val activity: Activity = this
        if (!this::notificationServerErrorDeployed.isInitialized) {notificationServerErrorDeployed = Pair(false, Clock.System.now())}
        if (!this::notifactionWaterLevelSensorErrorDeployed.isInitialized) {notifactionWaterLevelSensorErrorDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationACPowerDeployed.isInitialized) {notificationACPowerDeployed = Pair(false, Clock.System.now())}
        if (!this::notifactionWaterLevelSensorErrorBDeployed.isInitialized) {notifactionWaterLevelSensorErrorBDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationHighWaterDeployed.isInitialized) {notificationHighWaterDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationMainRunWarnDeployed.isInitialized) {notificationMainRunWarnDeployed = Pair(false, Clock.System.now())}
        if (!this::notificationBackupRan.isInitialized) {notificationBackupRan = Pair(false, Clock.System.now())}
        if (!this::notificationWaterTooLow.isInitialized) {notificationWaterTooLow = Pair(false, Clock.System.now())}

        var firstRun: Boolean = true


        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainRunningBoxColor = ContextCompat.getColor(this, R.color.green)
        binding.backupRunningBoxColor = ContextCompat.getColor(this, R.color.green)
        binding.waterLevelImage = ContextCompat.getDrawable(this, R.drawable.water_low)
        binding.mainRunWarnView = true
        binding.backupRunWarnView = true
        binding.generalErrorView = true

        binding.acPowerLargeBatteryImage = ContextCompat.getDrawable(this, R.drawable.acplug)
        binding.acPowerSmallBatteryImage = ContextCompat.getDrawable(this, R.drawable.noacplug)
        var acPowerLargeBatteryBoolean = false
        var acPowerSmallBatteryBoolean = true

        notificationManager = createNotifications()

        runBlocking {
            launch {
                println("World!")
                Log.i("mainRunning onCreate", mainRunning_.toString())

                val threadServer = Thread {
                    while (true) {

                        if (!charging5_!! && !generalWarnSilence){
                            binding.generalErrorView = true
                            binding.generalErrorText = "USB disconnected\n / no power!"
                            val (deployed, timeDeployed) = notificationACPowerDeployed
                            Log.i("time and deploy", timeDeployed.toString())
                            Log.i("time and deploy", deployed.toString())
                            if (!deployed){
                                Log.i("charging5_", "starting notification for charging 5")
                                notificationBuilder("SumpPump RPi: No AC Power", "Usb is disconnected\nOr there is no power going to RPi","high", "22222", "11111", notificationManager!!)
                                notificationACPowerDeployed = Pair(true, Clock.System.now())
                            }
                        }
                        else{binding.generalErrorView = false
                            binding.generalErrorText = "Message shouldn't be here!"}

                        try {
                            val parameters = mapOf<String, String>("firstRun" to firstRun.toString())
                            Log.i("mainactivity oncreate", "calling get on jeffs-handyman")
                            get( "jeffs-handyman.net/sumppump",  parameters, null)

                        } catch (e: java.lang.Exception) {
                            //e.printStackTrace()
                            binding.generalErrorText = "Error in Server.\n NO Data"
                            //Log.i("mainActivity after get", e.toString())

                            val (deployed, timeDeployed) = notificationServerErrorDeployed
                            Log.i("server error deployed", deployed.toString())
                            if (!deployed){
                                notificationBuilder("Server Error", "Error In Server\nNo Data is being received","high", "11111","44444", notificationManager!!)
                                notificationServerErrorDeployed = Pair(true, Clock.System.now())
                            }


                        }
                        firstRun = false
                        Log.i("onCreate MainRunning", mainRunning_.toString())
                        if (mainRunning_ == true) {
                            binding.mainRunning = "Running"
                            binding.mainRunningBoxColor =
                                ContextCompat.getColor(activity, R.color.green)
                            Log.i("mainRunningColor", binding.mainRunningBoxColor.toString())
                        }
                        if (mainRunning_ == false) {
                            binding.mainRunning = "Pump is Off"
                            binding.mainRunningBoxColor =
                                ContextCompat.getColor(activity, R.color.red)

                            Log.i("mainRunningColor", binding.mainRunningBoxColor.toString())
                        }



                        if (backupRunning_ == true) {

                            val (deployed, timeDeployed) = notificationBackupRan

                            if (!deployed){
                                notificationBuilder("Check Sump Pump", "Backup Pump has run!","high", "22222", "88888", notificationManager!!)
                                notificationBackupRan = Pair(true, Clock.System.now())
                            }
                            binding.backupRunning = "Running"
                            binding.backupRunningBoxColor =
                                ContextCompat.getColor(activity, R.color.green)
                            Log.i("backupRunningColor", binding.backupRunningBoxColor.toString())
                        }
                        if (backupRunning_ == false) {
                            binding.backupRunning = "Pump is Off"
                            binding.backupRunningBoxColor =
                                ContextCompat.getColor(activity, R.color.red)

                            Log.i("mainRunningColor", binding.mainRunningBoxColor.toString())
                        }

                        highFlooding_ = true
                        lowFlooding_ = false
                        if (highFlooding_ == true) {
                            binding.waterLevelImage =
                                ContextCompat.getDrawable(activity, R.drawable.water_high)
                            binding.waterLevelText = "High\n(100%)"


                            val (deployed, timeDeployed) = notificationHighWaterDeployed
                            if (!deployed){
                                notificationBuilder(
                                    "WARNING: HIGH Water in Sump Well",
                                    "The water has reached the top of the well.\nBasement flooding is imminent.",
                                    "high",
                                    "00000",
                                    "55555",
                                    notificationManager!!
                                )
                                notificationHighWaterDeployed = Pair(true, Clock.System.now())
                            }
                        }
                        else if (midFlooding_ == true) {
                            binding.waterLevelImage =
                                ContextCompat.getDrawable(activity, R.drawable.water_50)
                            binding.waterLevelText = "50%"
                        } else if (lowFlooding_ == true) {
                            binding.waterLevelImage =
                                ContextCompat.getDrawable(activity, R.drawable.water_10)
                            binding.waterLevelText = "10%"
                        } else {
                            binding.waterLevelImage =
                                ContextCompat.getDrawable(activity, R.drawable.water_low)
                            binding.waterLevelText = "Empty"
                        }

                        binding.battery12vText = voltage12_.toString() + "V"
                        binding.battery5vText = voltage5_.toString() + "V"




                        val (deployed, timeDeployed) = notificationMainRunWarnDeployed

                        if (mainRunWarnVis and !deployed){
                            notificationBuilder("Check Sump Pump", "The pump has run for 10 minutes without stopping","high", "11111", "77777", notificationManager!!)
                            notificationMainRunWarnDeployed = Pair(true, Clock.System.now())
                        }
                        if (mainRunWarnVis && !mainPumpWarnSilence) {
                            binding.mainRunWarnView = true}
                        else {binding.mainRunWarnView = false}
                        if (backupRunWarnVis && !backupPumpWarnSilence) {binding.backupRunWarnView = true}
                        else {binding.backupRunWarnView = false}


                        if (acPowerLargeBatteryBoolean){
                            binding.acPowerLargeBatteryImage = ContextCompat.getDrawable(activity, R.drawable.acplug)
                        }
                        else{ binding.acPowerLargeBatteryImage = ContextCompat.getDrawable(activity, R.drawable.noacplug)}

                        if (acPowerSmallBatteryBoolean){
                            binding.acPowerSmallBatteryImage = ContextCompat.getDrawable(activity, R.drawable.acplug)
                        }
                        else{ binding.acPowerSmallBatteryImage = ContextCompat.getDrawable(activity, R.drawable.noacplug)}

                        if (mainRunning_|| backupRunning_ == true){
                            if (lowFlooding_ == false){
                                val (deployed, timeDeployed) = notificationWaterTooLow
                                if (!deployed){
                                    notificationBuilder(
                                        "URGENT: Check Pump",
                                        "The water level seems empty, but the pump is running",
                                        "high",
                                        "00000",
                                        "99999",
                                        notificationManager!!)

                                    notificationWaterTooLow = Pair(true, Clock.System.now())
                                }


                            }

                        }

                        resetNotifications()
                        sleep(1500)
                    }
                }
                threadServer.start()

            }

            println("Hello")
        }

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


    private fun resetNotifications(){
        if (this::notificationServerErrorDeployed.isInitialized) {
            val (deployed, timeDeployed) = notificationServerErrorDeployed
            Log.i("resetNotifications() server", timeDeployed.toString())
            val timeDif = (Clock.System.now() - timeDeployed)
            Log.i("timeDIFf_ResetNotifications", timeDif.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > 1.days) {
                notificationServerErrorDeployed = Pair(false, Clock.System.now())
            }
        }
        if (this::notifactionWaterLevelSensorErrorDeployed.isInitialized){ //this notification was never tested
            val (deployed, timeDeployed) = notifactionWaterLevelSensorErrorDeployed
            Log.i("resetNotifications() wl sensor", timeDeployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > 1.hours) {
                notifactionWaterLevelSensorErrorDeployed = Pair(false, Clock.System.now())
            }
        }
        if (this::notifactionWaterLevelSensorErrorBDeployed.isInitialized){ //this notification was never tested
            val (deployed, timeDeployed) = notifactionWaterLevelSensorErrorDeployed
            Log.i("resetNotifications() wl sensor", timeDeployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > 1.hours) {
                notifactionWaterLevelSensorErrorBDeployed = Pair(false, Clock.System.now())
            }
        }
        if (this::notificationACPowerDeployed.isInitialized){
            val (deployed, timeDeployed) = notificationACPowerDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > 12.hours) {
                notificationACPowerDeployed = Pair(false, Clock.System.now())
            }
        }
        if (this::notificationHighWaterDeployed.isInitialized){
            val (deployed, timeDeployed) = notificationHighWaterDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > 20.minutes) {
                notificationHighWaterDeployed = Pair(false, Clock.System.now())
            }
        }

        if (this::notificationMainRunWarnDeployed.isInitialized){
            val (deployed, timeDeployed) = notificationMainRunWarnDeployed
            Log.i("resetNotifications() ac power", timeDeployed.toString())
            Log.i("deployed", deployed.toString())
            if (deployed && (Clock.System.now() - timeDeployed) > 30.minutes) {
                notificationMainRunWarnDeployed = Pair(false, Clock.System.now())
            }
        }

        if (this::notificationBackupRan.isInitialized){
            val (deployed, timeDeployed) = notificationBackupRan
            if (deployed && (Clock.System.now() - timeDeployed) > 30.minutes) {
                notificationBackupRan = Pair(false, Clock.System.now())
            }
        }

        if (this::notificationWaterTooLow.isInitialized){
            val (deployed, timeDeployed) = notificationWaterTooLow
            if (deployed && (Clock.System.now() - timeDeployed) > 30.seconds) {
                notificationWaterTooLow = Pair(false, Clock.System.now())
            }
        }
    }

    fun closeMainPumpNotification(view: View){
        Log.i("closeMainPumpNotification", "starting close main pump notification")
        Log.i("closeMainPumpNotification", mainPumpWarnSilence.toString())
        if (!mainPumpWarnSilence){
            mainPumpSilenceTime = Clock.System.now()
            mainPumpWarnSilence = true}


    }

    fun closeBackupPumpNotification(view: View){
        Log.i("closebackupPumpNotification", "starting close backup pump notification")
        Log.i("closebackupPumpNotification", backupPumpWarnSilence.toString())
        if (!backupPumpWarnSilence){
            backupPumpSilenceTime = Clock.System.now()
            backupPumpWarnSilence = true}


    }

    fun closeGeneralWarn(view: View){
        Log.i("closeGeneralPumpNotification", "starting close general warn notification")
        Log.i("closegeneralNotification", generalWarnSilence.toString())
        if (!generalWarnSilence){

            generalWarnSilence = true}
    }
    private fun getRunTime(timeStarted: String): Int {
        val monthConvert = mapOf("Jan" to "1", "Feb" to "2", "Mar" to "3", "Apr" to "4", "May" to "5", "Jun" to "6", "Jul" to "7", "Aug" to "8", "Sep" to "9", "Oct" to "10", "Nov" to "11", "Dec" to "12")
        val regex = "\\w{3,9},\\s*(\\d*)\\s(\\w+)\\s*(\\d{4})\\s*(\\d{2}):(\\d{2}):(\\d{2})".toRegex()
        val match = regex.find(timeStarted)!!
        val day = match.groupValues[1]
        val month = monthConvert[match.groupValues[2]]
        if (month != null) {
            Log.i("MONTH", month)
        }
        val year = match.groupValues[3]
        val hour = match.groupValues[4]
        val minute = match.groupValues[5]
        val second = match.groupValues[6]

        //var formatter = DateTimeFormatter. ("yyyy-MMM-dd")
        //formatter =
        //   formatter.withLocale(Locale.US) // Locale specifies human language for translating, and cultural norms for lowercase/uppercase and abbreviations and such. Example: Locale.US or Locale.CANADA_FRENCH

        //val date = LocalDate.parse("3/24/2022", formatter)

        val dateStr = "$year-$month-$day"
        val timeStr = "$hour:$minute:$second"
        //val time = ("$year-$month-$day" + "T$hour:$minute:$second").toInstant()
        //val ldt = LocalDateTime.parse(time).toInstant()

        //val now = Clock.System.now()
        //val difference = now.minus(time)
        return 0

    }




    private fun postFirstRun(firstRun:Boolean){ //this is a post request but i didn't neeed it after all
        var payload: String
        Log.i("firstrunvariable in postfirstrun", firstRun.toString())

        if (firstRun) {payload = "fristRun: True"}
        else{payload = "firstRun: False"}


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



    fun get(url: String?, params: Map<String, String>, responseCallback: Unit?) {
        val httpBuilder: HttpUrl.Builder = url!!.toHttpUrlOrNull()!!.newBuilder()
        Log.i("in Get", params.toString())
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
        val mainTimeStartedReg: Regex = "timeStartedMain.:\\s*.(.+)".toRegex()
        val backupTimeStartedReg: Regex = "timeStartedBackup.:\\s*.(.+)".toRegex()

        val highFloodingReg: Regex = "highFlooding.:\\s*(\\w+)".toRegex()
        val midFloodingReg: Regex = "midFlooding.:\\s*(\\w+)".toRegex()
        val lowFloodingReg: Regex = "lowFlooding.:\\s*(\\w+)".toRegex()

        val voltage5Reg: Regex = "'voltage5.:\\s*(\\d*\\.\\d{0,2})".toRegex()
        val voltage12Reg: Regex = "voltage12.:\\s*(\\d*\\.\\d{0,2})".toRegex()
        val charging5Reg: Regex = "charging5.:\\s*(\\w+)".toRegex()


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
        match = responseString?.let {backupRunWarnReg.find(it)}
        val backupRunningWarnStr = match?.groupValues?.get(1)
        if (backupRunningWarnStr != null) {
            applyBackupPumpWarn(backupRunningWarnStr)
        }
        match = responseString?.let {mainTimeStartedReg.find(it)}
        val mainTimeStartedStr = match?.groupValues?.get(1)
        if (mainTimeStartedStr != null) {
            //val mainRunTime = getRunTime(mainTimeStartedStr)
            Log.i("mainRunningStr", mainTimeStartedStr)
        }
        match = responseString?.let {backupTimeStartedReg.find(it)}
        val backupTimeStartedStr = match?.groupValues?.get(1)
        if (backupTimeStartedStr != null) {
            Log.i("backupTimeStartedStr", backupTimeStartedStr)
            //getRunTime(backupTimeStartedStr)
        }
        match = responseString?.let { highFloodingReg.find(it) }
        val highFloodingStr = match?.groupValues?.get(1)
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

        applyRelayData(mainRunningStr, backupRunningStr)
        applyBatteryData(
            voltage12Str,
            voltage5Str,
            charging5Str
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
    private fun applyMainPumpWarn(mainRunWarnStr: String){
        Log.i("applymainpumpwarn", "starting apply main pump warn")
        if (mainPumpWarnSilence){
            val now = Clock.System.now()
            if ((now - mainPumpSilenceTime )>  30.minutes ){
                mainPumpWarnSilence = false
            }
        }
        Log.i("applyMainPumpWarn", mainPumpWarnSilence.toString())
        if (mainRunWarnStr == "true"){mainRunWarnVis = true}
        else {mainRunWarnVis = false}
    }

    private fun applyBackupPumpWarn(backupRunWarnStr: String){
        Log.i("applybackuppumpwarn", "starting apply backup pump warn")
        if (backupPumpWarnSilence){
            val now = Clock.System.now()
            if ((now - backupPumpSilenceTime )>  10.minutes ){
                backupPumpWarnSilence = false
            }
        }
        Log.i("applyMainPumpWarn", backupPumpWarnSilence.toString())
        if (backupRunWarnStr == "true"){backupRunWarnVis = true}
        else {backupRunWarnVis = false}
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
        var voltage5Apply: Float? = 0.0F
        var voltage12Apply: Float? = 0.0F
        var charging5Apply: Boolean = false


        voltage5Apply = voltage5Str?.toFloat()
        voltage12Apply = voltage12Str?.toFloat()

        if (charging5Str == "False") {
            charging5Apply = false
        } else if (charging5Str == "True") {
            charging5Apply = true
        } else {
            Log.i("Failure in applyBattery", charging5Apply.toString())
        }
        Log.i("applyBatteryData", "done with apply battery data")
        Log.i("voltage5", voltage5Apply.toString())
        Log.i("voltage12", voltage12Apply.toString())
        Log.i("charging", charging5Apply.toString())

        voltage12_ = voltage12Apply
        voltage5_ = voltage5Apply
        charging5_ = charging5Apply

    }

    private fun applyWaterLevel(
        highFloodingStr: String?,
        midFloodingStr: String?,
        lowFloodingStr: String?
    ) {
        //delete this later:
        val highFloodingStrTest = "true"
        val midFloodingStrTest = "true"
        val lowFloodingStrTest ="false"
        var highFloodingApply: Boolean = false
        var midFloodingApply: Boolean = false
        var lowFloodingApply: Boolean = false
        val waterLevelWarningBox = findViewById<TextView>(R.id.waterLevelWarning)
        val triangleWarningImageView = findViewById<ImageView>(R.id.waterLevelWarningTriangle)
        val xToClose = findViewById<ImageView>(R.id.xToCloseWaterLevelPumpErrorImageView)

        if (highFloodingStr == "false") {
            highFloodingApply = false
        }
        else if (highFloodingStr == "true") {
            highFloodingApply = true
            if (midFloodingStr == "false" || lowFloodingStr == "false") {
                waterLevelWarningBox.visibility = View.VISIBLE
                triangleWarningImageView.visibility = View.VISIBLE
                xToClose.visibility = View.VISIBLE
                Log.i(
                    "ERROR: Raise notification",
                    " Water Level Sensor Error:High Flooding is true, but others are false"
                )
                val (deployed, timeDeployed) = notifactionWaterLevelSensorErrorDeployed
                if (!deployed) {
                    notificationBuilder(
                        "SumpPump WaterLevel Sensor Error",
                        "Error In water level sensor\nhigh=true mid/low = false",
                        "high",
                        "22222",
                        "11111",
                        notificationManager!!)
                    notifactionWaterLevelSensorErrorDeployed = Pair(true, Clock.System.now())
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
                triangleWarningImageView.visibility = View.VISIBLE

                Log.i(
                    "ERROR: Raise notification",
                    "Water Level Sensor Error:Mid Sensor is true, but Low  is false"
                )
                val (deployed, timeDeployed) = notifactionWaterLevelSensorErrorBDeployed
                if (!deployed) {
                    notificationBuilder(
                        "SumpPump WaterLevel Sensor Error",
                        "Error In water level sensor\nmid=true low = false",
                        "high",
                        "22222",
                        "22222",
                        notificationManager!!)
                    notifactionWaterLevelSensorErrorBDeployed = Pair(true, Clock.System.now())
                }
            } }
        else {
            Log.i("Failure in applyWaterlevel", "failure in apply waterlevel mid")

        }




        if (lowFloodingStr == "false") {
            lowFloodingApply = false
        }
        else if (lowFloodingStr == "true") {
            lowFloodingApply = true
        }
        else {
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

    private fun notificationBuilder(title:String, content:String, priority: String, channelid: String, notifid:String, notificationManager: NotificationManager  ){ //priority: high default low
        Log.i("notificationBuilder()", "starting notification builder")
        val builder = NotificationCompat.Builder(this, channelid)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setContentIntent(pendingIntent)

        Log.i("notificationBuilder", title )
        Log.i("notificationBuilder", content )
        builder.setSmallIcon(R.drawable.flood_house_svg)
        if (priority == "high"){
            builder.priority = NotificationCompat.PRIORITY_HIGH}
        else if(priority=="default"){
            builder.priority = NotificationCompat.PRIORITY_DEFAULT}
        else if(priority == "low"){
            builder.priority = NotificationCompat.PRIORITY_LOW
        }

        notificationManager.notify(notifid.toInt(), builder.build())




    }

    private fun createNotifications (): NotificationManager? {
        Log.i("createNotifications", Build.VERSION.SDK_INT.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            Log.i("createNotifications", "software requirements met!")
            val mChannelAA = NotificationChannel("00000", "Most Urgent Warnings", NotificationManager.IMPORTANCE_HIGH)
            mChannelAA.description = "e.g. \"Pump is running on no water\" or \"flooding in basement\""

            val mChannelA = NotificationChannel("11111", getString(R.string.pumpProblemsChannel), NotificationManager.IMPORTANCE_HIGH)

            mChannelA.description =  getString(R.string.pumpProblemsChannelDescription)
            val mChannelB = NotificationChannel("22222", getString(R.string.systemProblemsChannel), NotificationManager.IMPORTANCE_HIGH)
            mChannelB.description =  getString(R.string.systemProblemsChannelDescription)
            val mChannelC = NotificationChannel("33333", getString(R.string.generalInfoChannel), NotificationManager.IMPORTANCE_DEFAULT)
            mChannelC.description =  getString(R.string.generalInfoChannelDescription)


            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannelAA)
            notificationManager.createNotificationChannel(mChannelA)
            notificationManager.createNotificationChannel(mChannelB)
            notificationManager.createNotificationChannel(mChannelC)
            return notificationManager
        }
        return null
    }


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}