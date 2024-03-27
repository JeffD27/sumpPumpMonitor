package com.example.sumppumpbeta3;
import android.app.Activity
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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.sumppump3.R
import com.example.sumppump3.databinding.ActivityMainBinding
//import com.example.sumppumpbeta3.R
//import com.example.sumppumpbeta3.databinding.ActivityMainBinding
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.Thread.sleep
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
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
    var mainRunning_: Boolean = false
    var backupRunning_: Boolean? = false

    var voltage12_: Float? = 0.00f
    var voltage5_: Float? = 0.00f
    var charging5_: Boolean? = false

    var highFlooding_: Boolean? = false
    var midFlooding_: Boolean? = false
    var lowFlooding_: Boolean? = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //val binding: DataBindingUtil.inflate(layoutInflater, R.layout.list_item, viewGroup, false)
        val activity: Activity = this
        var firstRun: Boolean = true

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainRunningBoxColor = ContextCompat.getColor(this, R.color.green)
        binding.backupRunningBoxColor = ContextCompat.getColor(this, R.color.green)
        binding.waterLevelImage = ContextCompat.getDrawable(this, R.drawable.water_low)
        binding.mainRunWarnView = true
        //binding.pyDataVar = PyDataLayout(mainRunning_, backupRunning_, highFlooding_, midFlooding_, lowFlooding_, voltage5_, charging5_, voltage12_)
        //setContentView(R.layout.activity_main)
        runBlocking {
            launch {
                println("World!")
                Log.i("mainRunning onCreate", mainRunning_.toString())



                val threadServer = Thread {
                    while (true) {


                        try {
                            val parameters = mapOf<String, String>("firstRun" to firstRun.toString())
                            Log.i("mainactivity oncreate", "calling get on jeffs-handyman")
                            get( "jeffs-handyman.net/sumppump",  parameters, null)

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            Log.i("mainActivity after get", e.toString())

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

                        if (highFlooding_ == true) {
                            binding.waterLevelImage =
                                ContextCompat.getDrawable(activity, R.drawable.water_high)
                            binding.waterLevelText = "High\n(100%)"
                        } else if (midFlooding_ == true) {
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

                        if (mainRunWarnVis == true) {binding.mainRunWarnView = true}
                        else {binding.mainRunWarnView = false}

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


    private val moshi = Moshi.Builder().build()
    //private val adapter: JsonAdapter<PyData> = moshi.adapter(PyData::class.java)

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





    //private fun getDataTake999(): Callback {}



    /*
    val request: Request = Request.Builder()
        .url("http://10.0.0.218:3000/")

        .header("Connection", "close")
        .build()


    runBlocking {


        client.newCall(request).execute().use { response ->
            //println("here")
            if (!response.isSuccessful) println("not succesful i think")//throw IOException("Unexpected code $response")
            // println("&&&&&$$$%%%%^^^#^^$^&@#**#&^^%")
    */
    var mainRunWarnVis: Boolean = false
    private fun applyMainPumpWarn(mainRunWarnStr: String){
        if (mainRunWarnStr == "true"){mainRunWarnVis = true}
        else {mainRunWarnVis = false}
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


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}