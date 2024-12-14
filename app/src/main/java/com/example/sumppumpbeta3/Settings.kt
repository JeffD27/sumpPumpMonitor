package com.example.sumppumpbeta3


import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.sumppump3.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit


val mainScope = MainScope()


class Settings : ComponentActivity() {
    private val context = this
    private val durationConvertDict =  LinkedHashMap<String, kotlin.time.Duration>()
    override fun onCreate(savedInstanceState: Bundle?) {


        runBlocking {
                launch {

                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.settings)


                    durationConvertDict["5 minutes"] = 5.minutes
                    durationConvertDict["10 minutes"] = 10.minutes
                    durationConvertDict["15 minutes"] = 15.minutes
                    durationConvertDict["30 minutes"] = 30.minutes
                    durationConvertDict["1 hour"] = 1.hours
                    durationConvertDict["2 hours"] = 2.hours
                    durationConvertDict["4 hours"] = 4.hours
                    durationConvertDict["8 hours"] = 8.hours
                    durationConvertDict["24 hours"] = 24.hours
                    durationConvertDict["48 hours"] = 48.hours

                    durationPositionInt[10.minutes] = 0
                    durationPositionInt[15.minutes] = 1
                    durationPositionInt[30.minutes] = 2
                    durationPositionInt[1.hours] = 3
                    durationPositionInt[2.hours] = 4
                    durationPositionInt[4.hours] = 5
                    durationPositionInt[8.hours] = 6
                    durationPositionInt[24.hours] = 7
                    durationPositionInt[48.hours] = 8


                    val buttonOK = findViewById<Button>(R.id.buttonOK)

                    buttonOK.setOnClickListener(){
                        finish()
                    }
                    val buttonRestore = findViewById<Button>(R.id.buttonRestoreDefault)

                    buttonRestore.setOnClickListener(){
                        restoreDefaults()

                    }


                    //add values to dictionary
                    val spinnerServerError = findViewById<Spinner>(R.id.spinnerServerError)
                    val string = notificationServerErrorMuteDuration.toString() //just for the print statemtent
                    Log.i("ServerError in spinners", string)
                    spinnerDurationDict[spinnerServerError] = notificationServerErrorMuteDuration

                    spinnerStringDict[spinnerServerError] = "serverError"
                    val spinnerNoPower = findViewById<Spinner>(R.id.spinnerNoPowerSilenceTime)
                    spinnerDurationDict[spinnerNoPower] = notificationACPowerMuteDuration
                    spinnerStringDict[spinnerNoPower] = "noPower"
                    val spinnerHighWater = findViewById<Spinner>(R.id.spinnerHighWater)
                    spinnerDurationDict[spinnerHighWater] = notificationHighWaterMuteDuration
                    spinnerStringDict[spinnerHighWater] = "highWater"
                    val spinnerMainRunWarn = findViewById<Spinner>(R.id.spinnerMainRun)
                    spinnerDurationDict[spinnerMainRunWarn] = notificationMainRunWarnMuteDuration
                    spinnerStringDict[spinnerMainRunWarn] = "mainRunTime"
                    val spinnerBackupPump = findViewById<Spinner>(R.id.spinnerBackupRun)
                    spinnerDurationDict[spinnerBackupPump] = notificationBackupRanMuteDuration
                    spinnerStringDict[spinnerBackupPump] = "backupRun"
                    val spinnerNoWater = findViewById<Spinner>(R.id.spinnerPumpRunNoWater)
                    spinnerDurationDict[spinnerNoWater] = notificationWaterTooLowMuteDuration
                    spinnerStringDict[spinnerNoWater] = "noWater"
                    val spinnerSensorError = findViewById<Spinner>(R.id.spinnerWaterLevelSensorError)
                    spinnerDurationDict[spinnerSensorError] = notificationWaterLevelSensorErrorMuteDuration
                    spinnerStringDict[spinnerSensorError] = "sensorError"
                    val spinnerLowPower12v = findViewById<Spinner>(R.id.spinnerLow12v)
                    spinnerDurationDict[spinnerLowPower12v] = notificationBattery12LowMuteDuration
                    spinnerStringDict[spinnerLowPower12v] = "lowPower12"
                    Log.i("settingsDataEnd", "settings Data Ran")

                    val array = resources.getStringArray(R.array.spinnerTimes)
                    // access the spinner

                    for (spinner in spinnerDurationDict.keys){
                        val duration = spinnerDurationDict[spinner]
                        Log.i("spinner", spinnerStringDict[spinner].toString())
                        Log.i("durtaionInSettings", duration.toString())
                        val position = durationPositionInt[duration]!!

                        createSpinner(spinner, context, array, position)
                    }
                    LoopHandler().setNotificationMuteTimes(context)




                }
            }
    }




     fun restoreDefaults(): View.OnClickListener? {
        Log.i("restoreDefaults", "restore Defualts starting")
        val notificationStrings = listOf("serverError", "sensorError", "noPower", "highWater", "mainRunTime", "backupRun", "noWater", "lowBattery12" )

        for (string in notificationStrings){
            Log.i("String in Restore", string)
            Log.i("spinnerStringDict_restore", spinnerStringDict.keys.toString())
            val stringSpinnerDict = spinnerStringDict.entries.associate{ (k,v)-> v to k} //reverses stringSpinnerDict
            Log.i("stringSpinner", stringSpinnerDict.keys.toString())

            val spinner = stringSpinnerDict[string]
            Log.i("spinnner in Restore", spinner.toString())

            if (spinner != null) {
                if (defaultMuteTimes.isEmpty()){
                    defaultMuteTimes["serverError"] = 2.hours //change this back to 1 day or something
                    defaultMuteTimes["sensorError"] = 1.days
                    defaultMuteTimes["noPower"] = 1.hours
                    defaultMuteTimes["highWater"] = 2.hours
                    defaultMuteTimes["mainRunTime"] = 30.minutes
                    defaultMuteTimes["backupRun"] = 30.minutes
                    defaultMuteTimes["noWater"] = 10.minutes
                    defaultMuteTimes["lowBattery12"] = 1.days
                    defaultMuteTimes["noPumpControl"] = 12.hours
                    defaultMuteTimes["mainRunning"] = 30.minutes

                }
                val duration = defaultMuteTimes[string]
                val position = durationPositionInt[duration]!!
                //runBlocking {writeData(string, duration!!)}
                spinner.post(Runnable() {
                    run() {
                        spinner.setSelection(position);
                    }
                })
                spinner.setSelection(position, true)
                Log.i("positionW", position.toString())
            }
            else{Log.i("elseINRestoreDefaults", "problem in restore defualts")}


        }
         return null
     }
    private fun createSpinner(spinner: Spinner, context: Context, array: Array<String>, positionInt: Int) {
        if (spinner != null) {

            val adapter = ArrayAdapter(
                context,
                R.layout.spinner_style,
                array
            )
            adapter.setDropDownViewResource(R.layout.spinner_style)
            spinner.adapter = adapter




            spinner.setPopupBackgroundDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.darkbackground
                )
            );
            spinner.setSelection(positionInt, )

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    Log.i("spinner", spinnerStringDict[spinner].toString())
                    val choice = spinner.selectedItem.toString()
                    Log.i("choice", choice)
                    val durationFromChoice = durationConvertDict[choice]!!
                    if (durationFromChoice != null) {
                        spinnerDurationDict[spinner] = durationFromChoice

                        Log.i("spinner choice", durationFromChoice.toString())
                        runBlocking{
                            //spinnerStringDict[spinner] returns notifystring
                            launch {  writeData(spinnerStringDict[spinner]!!, durationFromChoice)} }
                    }


                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        }
    }

    suspend fun writeData(notification: String, data:kotlin.time.Duration) {
        val key = intPreferencesKey(notification)
        /*
        if (notification == "serverError") {
            Log.i("writeData", "setting serverError to 2")
            notificationServerErrorMuteDuration = 2.hours
            this.dataStore.edit { settings ->
                settings[key] = 2 //for testing delet all this if statement
            }
            val read: Flow<Int> = this.dataStore.data //read data in saved data store
                .map { settings: Preferences ->
                    // No type safety.
                    settings[key] ?: 10
                    //this sets the value (in exampleCounterFlow not datastore) if null
                }
            Log.i("read@!", read.first().toString())


            return
        }
        */
        Log.i("notification string", notification)
        Log.i("data duration", data.toString())
        val minutes = listOf(5.minutes, 10.minutes, 15.minutes, 30.minutes)
        val hours = listOf(1.hours, 2.hours, 4.hours, 8.hours, 12.hours, 24.hours, 48.hours)
        Log.i("WriteData", "initializing")
        this.dataStore.edit { settings -> //write data to saved data store
            if (data in minutes) {
                settings[key] = data.toInt(DurationUnit.MINUTES)
                Log.i("minutes", settings[key].toString())
            } else if (data in hours) {
                settings[key] = data.toInt(DurationUnit.HOURS)
                Log.i("Hours", settings[key].toString())
            } else {
                Log.i("elseWriteData", "here's your problem")
            }
        }
    }








}

