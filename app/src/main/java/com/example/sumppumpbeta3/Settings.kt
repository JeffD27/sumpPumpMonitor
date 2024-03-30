package com.example.sumppumpbeta3


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

var noPowerSilenceTimeGlobal:kotlin.time.Duration = 1.days

class Settings : ComponentActivity() {

    private val durationConvertDict =  LinkedHashMap<String, kotlin.time.Duration>()
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this

        runBlocking {
            launch {

                super.onCreate(savedInstanceState)
                setContentView(R.layout.settings)
                durationConvertDict["5 min"]=5.minutes
                durationConvertDict["10 min"]=10.minutes
                durationConvertDict["30 min"]=30.minutes
                durationConvertDict["1 hour"] = 1.hours
                durationConvertDict["2 hours"] = 2.hours
                durationConvertDict["4 hours"] = 4.hours
                durationConvertDict["8 hours"] = 8.hours
                durationConvertDict["24 hours"] = 24.hours

                val noPowerSilenceTime = resources.getStringArray(R.array.noPowerNotificationSilenceTime)
                // access the spinner
                val spinner = findViewById<Spinner>(R.id.spinnerNoPowerSilenceTime)
                if (spinner != null) {

                    val adapter = ArrayAdapter(
                        context,
                        R.layout.spinner_style, noPowerSilenceTime
                    )
                    adapter.setDropDownViewResource(R.layout.spinner_style)
                    spinner.adapter = adapter



                    spinner.setPopupBackgroundDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.darkbackground
                        )
                    );
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
                                        "" + noPowerSilenceTime[position], Toast.LENGTH_SHORT
                            ).show()
                            Log.i("spinner", spinner.selectedItem.toString())
                            val choice = spinner.selectedItem.toString()
                            val durationFromChoice = durationConvertDict[choice]
                            if (durationFromChoice != null) {
                                notificationACPowerMuteDuration = durationFromChoice
                            }
                            Log.i("Spinner_noPowerSilenceTime", noPowerSilenceTime.toString())


                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // write code to perform some

                        }
                    }
                }

            }
        }

        }
    suspend fun writeData() {
        val NOPOWERSILENCETIME = intPreferencesKey(noPowerSilenceTimeGlobal.toString())
        this.dataStore.edit { settings ->
            val currentCounterValue = settings[NOPOWERSILENCETIME] ?: 0
            settings[NOPOWERSILENCETIME] = currentCounterValue + 1
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {

    }
}