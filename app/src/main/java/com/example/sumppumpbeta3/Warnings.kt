package com.example.sumppumpbeta3

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.sumppump3.R
import com.example.sumppump3.databinding.WarningsPageBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime


val warningStrings = arrayOf(
    "serverErrorWarning",
    "sensorErrorWarning",
    "noPowerWarning",
    "highWaterWarning",
    "mainRunTimeWarning",
    "backupRunWarning",
    "noWaterWarning",
    "lowBattery12Warning",
    "noPumpControlWarning"
)
class Warnings: ComponentActivity() {


    private val warningStringToCard = LinkedHashMap<String, ConstraintLayout>()
    private val warningStringToTimeStampBinding = LinkedHashMap<String, ConstraintLayout>()
    private val warningStringToTimeStamp = LinkedHashMap<String, LocalDateTime>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.warnings_page)

        val binding: WarningsPageBinding =
            DataBindingUtil.setContentView(this, R.layout.warnings_page)

        warningStringToCard["serverErrorWarning"] = findViewById(R.id.serverErrorCard)
        warningStringToCard["sensorErrorWarning"] = findViewById(R.id.sensorErrorCard)
        warningStringToCard["noPowerWarning"] = findViewById(R.id.noPowerCard)
        warningStringToCard["highWaterWarning"] = findViewById(R.id.highWaterCard)
        warningStringToCard["mainRunTimeWarning"] = findViewById(R.id.mainRunTimeCard)
        warningStringToCard["backupRunWarning"] = findViewById(R.id.constraintLayoutBackupRun)
        warningStringToCard["noWaterWarning"] = findViewById(R.id.constraintNoWater)
        warningStringToCard["lowBattery12Warning"] = findViewById(R.id.constraintLowBattery)
        warningStringToCard["noPumpControlWarning"] = findViewById(R.id.noPumpControlCard)





        var sum = 0
        for (warning in warningStrings){
            Log.i("warning^%", warning)
            val timeData = getTimeData(warning)
            val visData = getVisData(warning, timeData) //read from datastore
            Log.i("visdata", visData.toString())
            sum += visData
            val card = warningStringToCard[warning]!! //getXMLCard

            /*
            if (visData == 0){

                card.visibility = GONE
            }
            else{

                card.visibility = VISIBLE

            }
            */
            when (warning){
                "serverErrorWarning" ->{

                    binding.serverErrorTimeStamp = timeData.toString()

                }
                "sensorErrorWarning" ->{
                    binding.sensorErrorTimeStamp = timeData.toString()
                }
                "noPowerWarning" ->{
                    binding.noPowerTimeStamp = timeData.toString()
                }
                "highWaterWarning" ->{
                    binding.highWaterTimeStamp = timeData.toString()
                }
                "mainRunTimeWarning" ->{
                    binding.mainRunningTimeStamp = timeData.toString()
                }
                "backupRunWarning" ->{
                    binding.backupRunTimeStamp = timeData.toString()
                }
                "noWaterWarning" ->{
                    binding.noWaterTimeStamp = timeData.toString()
                }
                "lowBattery12Warning" ->{
                    binding.lowBattery12TimeStamp = timeData.toString()
                }

                "noPumpControlWarning" ->{
                    binding.noPumpControlTimeStamp = timeData.toString()
                }


            }




        }
        var sumOfVisCards = 0
        for (card in warningStringToCard.values){
            if (card.visibility != GONE){sumOfVisCards += 1}
        }
        if (sumOfVisCards > 0){
            val allGoodCard:ConstraintLayout = findViewById(R.id.cardAllGood)
            allGoodCard.visibility = GONE
        }

    }

    private var newTimeString: String = ""
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimeData(warning: String): String {
        runBlocking {

            val prefKey = stringPreferencesKey(warning + "Time")

            val exampleCounterFlow: Flow<String> =
                dataStore.data //read data in saved data store
                    .map { settings ->
                        // No type safety.
                        settings[prefKey]
                            ?: LocalDateTime.of(2000, 11, 1, 1, 1)
                                .toString()    //this sets the value to zero (in exampleCounterFlow not datastore) if null
                    }
            val timeString = exampleCounterFlow.first()
            val regex = Regex("(\\d{4}).(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})")
            val match = regex.find(timeString)!!
            year = match.groupValues[1].toInt()
            month = match.groupValues[2].toInt()
            day = match.groupValues[3].toInt()
            hour = match.groupValues[4].toInt()
            minute = match.groupValues[5].toInt()


            var amPM = "am"
            if (hour > 12){
                hour = hour - 12
                amPM = "pm"
            }
            val dayFromString = LocalDate.of(year, month, day)
            val timeFromString = LocalDateTime.of(year, month, day, hour, minute, 0)
            warningStringToTimeStamp[warning] = timeFromString
            val todayDate = LocalDate.now()
            val nowTime = LocalDateTime.now()
            var setZero = false
           // Log.i("inGetTime", (java.time.Duration.between(timeFromString,nowTime) > java.time.Duration.ofMinutes(1)).toString())
            Log.i("durationBetween", java.time.Duration.between(timeFromString,nowTime).toString())
            Log.i("booleanDurationBetween", (java.time.Duration.between(timeFromString,nowTime) > java.time.Duration.ofMinutes(1)).toString())
            if (java.time.Duration.between(timeFromString,nowTime) > java.time.Duration.ofDays(1)){ //how long should a warning stay in the warnings page
                Log.i("warningStringToCard", warningStringToCard[warning].toString())
                warningStringToCard[warning]!!.visibility= GONE
            }

            var today_bool = false
            if(todayDate.equals(dayFromString)){
                today_bool = true

            }

            Log.i("Today", today_bool.toString())
            if (minute > 10) {
                if (today_bool){
                    newTimeString = "Today at $hour:$minute $amPM"
                }
                else{
                    newTimeString = "$month/$day/$year at $hour:$minute $amPM"
                }
            }
            else{
                if (today_bool){
                    newTimeString = "Today at $hour:0$minute $amPM"
                }
                else{
                    newTimeString = "$month/$day/$year at $hour:0$minute $amPM" //add a zero to minute
                }
            }


        }
        return newTimeString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun reOrderCardsByTime(){
        var mostRecentTimes = ArrayList<LocalDateTime>()
        val cardsToTimes = LinkedHashMap<ConstraintLayout, LocalDateTime>()
        for(warning in warningStringToCard.keys){
            val card = warningStringToCard[warning]!!
            val timestamp = warningStringToTimeStamp[warning]!!
            cardsToTimes[card] = timestamp
        val cardsToOrderNumber = LinkedHashMap<ConstraintLayout, Int>()
        val numberOfCards = warningStringToCard.size
        var numberSortedSoFar = 0
        val sortedCardsWithTime = LinkedHashMap<ConstraintLayout, LocalDateTime>()
        val timeToCard = cardsToTimes.entries.associate{ (k,v)-> v to k} //uno reverse
        val timesSorted = timeToCard.keys.sortedBy { it }
        for (time in timesSorted) {
            val card = timeToCard[time]!!
            sortedCardsWithTime[card] = time
        }
        var i= 0
        for ((card, time) in sortedCardsWithTime.entries) {
            when(i) {
            1 ->{
                val card = findViewById<CardView>(R.id.highWaterCard)
                card.removeView(findViewById(R.id.cardHighWater))
                card.addView(card)

            }
            2 -> {
                val card

            }
            i += 1


        }






            }
        }


        for( warning in warningStringToTimeStamp.keys){
            val time = warningStringToTimeStamp[warning]

        }

    }

    private var visData:Int = 0
    private fun getVisData(warning: String, timeData: String):Int {

        runBlocking {

                val prefKey = intPreferencesKey(warning)

                val exampleCounterFlow: Flow<Int> =
                    dataStore.data //read data in saved data store
                        .map { settings ->
                            // No type safety.
                            settings[prefKey]
                                ?: 0   //this sets the value to zero (in exampleCounterFlow not datastore) if null
                        }
                visData = exampleCounterFlow.first()

                Log.i("dataInWarnings", visData.toString())



            }
        return visData
    }


    fun closeHighWater(view: View){

            val view = findViewById<ConstraintLayout>(R.id.highWaterCard)
            view.visibility = GONE
    }
    fun closeServerError(view: View){
        val view = findViewById<ConstraintLayout>(R.id.serverErrorCard)
        view.visibility = GONE
    }
    fun closeNoACPower(view: View){

        val view = findViewById<ConstraintLayout>(R.id.noPowerCard)
        view.visibility = GONE
    }

    fun closeSensorError(view: View){
        val view = findViewById<ConstraintLayout>(R.id.sensorErrorCard)
        view.visibility = GONE
    }
    fun closeMainRunTime(view: View){
        val view = findViewById<ConstraintLayout>(R.id.mainRunTimeCard)
        view.visibility = GONE
    }

    fun closeBackupRun(view: View){
        val view = findViewById<ConstraintLayout>(R.id.constraintLayoutBackupRun)
        view.visibility = GONE
    }

    fun closeNoWater(view: View){
        val view = findViewById<ConstraintLayout>(R.id.constraintNoWater)
        view.visibility = GONE
    }

    fun closeLowBattery12(view: View) {
        val view = findViewById<ConstraintLayout>(R.id.constraintLowBattery)
        view.visibility = GONE
    }

    fun closeNoPumpControl(view: View) {
        val view = findViewById<ConstraintLayout>(R.id.noPumpControlCard)
        view.visibility = GONE
    }
}


