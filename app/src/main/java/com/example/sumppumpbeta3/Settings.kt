package com.example.sumppumpbeta3


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.sumppump3.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit


class Settings : ComponentActivity() {

    private val durationConvertDict =  LinkedHashMap<String, kotlin.time.Duration>()
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this

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

                    val spinnerServerErrorString = "spinnerServerError"

                        //add values to dictionary
                        val spinnerServerError = findViewById<Spinner>(R.id.spinnerServerError)
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
                        spinnerDurationDict[spinnerSensorError] =
                            notificationWaterLevelSensorErrorMuteDuration
                        spinnerStringDict[spinnerSensorError] = "sensorError"
                        Log.i("settingsDataEnd", "settings Data Ran")






                        val array = resources.getStringArray(R.array.spinnerTimes)
                        // access the spinner
                        for (spinner in spinnerDurationDict.keys){
                            val duration = spinnerDurationDict[spinner]
                            val position = durationPositionInt[duration]!!
                            createSpinner(spinner, context, array, position)
                        }




                        }
                    }

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
            spinner.setSelection(positionInt)
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    Toast.makeText(
                        this@Settings,
                        getString(R.string.selected_item) + " " +
                                "" + array[position], Toast.LENGTH_SHORT
                    ).show()
                    Log.i("spinner", spinner.selectedItem.toString())
                    val choice = spinner.selectedItem.toString()
                    Log.i("choice", choice)
                    val durationFromChoice = durationConvertDict[choice]!!
                    if (durationFromChoice != null) {
                        spinnerDurationDict[spinner] = durationFromChoice
                    }
                    Log.i("Spinner_noPowerSilenceTime", durationFromChoice.toString())
                    runBlocking{
                        launch {  writeData(spinnerStringDict[spinner]!!, durationFromChoice)} }


                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        }
    }

    suspend fun writeData(notification: String, data:kotlin.time.Duration) {
        val key = intPreferencesKey(notification)
        val minutes = listOf(5.minutes, 10.minutes, 15.minutes, 30.minutes)
        val hours = listOf(1.hours, 2.hours, 4.hours, 8.hours, 12.hours, 24.hours, 48.hours)
        Log.i("WriteData", "initializing")
        this.dataStore.edit { settings ->
            if (data in minutes ){
                settings[key] = data.toInt(DurationUnit.MINUTES)
                Log.i("minutes", settings[key].toString())
            }
            else if(data in hours){
                settings[key] = data.toInt(DurationUnit.HOURS)
                Log.i("Hours", settings[key].toString())
            }
            else{
                Log.i("elseWriteData", "here's your problem")
            }
        }
    }




    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {

    }
}
