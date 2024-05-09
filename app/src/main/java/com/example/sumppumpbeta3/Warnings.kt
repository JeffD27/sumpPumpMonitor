package com.example.sumppumpbeta3

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
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
        setContentView(com.example.sumppump3.R.layout.warnings_page)

        val binding: WarningsPageBinding =
            DataBindingUtil.setContentView(this, com.example.sumppump3.R.layout.warnings_page)

        warningStringToCard["serverErrorWarning"] = findViewById(com.example.sumppump3.R.id.serverErrorCard)
        warningStringToCard["sensorErrorWarning"] = findViewById(com.example.sumppump3.R.id.sensorErrorCard)
        warningStringToCard["noPowerWarning"] = findViewById(com.example.sumppump3.R.id.noPowerCardinWarnings)
        warningStringToCard["highWaterWarning"] = findViewById(com.example.sumppump3.R.id.highWaterCard)
        warningStringToCard["mainRunTimeWarning"] = findViewById(com.example.sumppump3.R.id.mainRunTimeCard)
        warningStringToCard["backupRunWarning"] = findViewById(com.example.sumppump3.R.id.constraintLayoutBackupRun)
        warningStringToCard["noWaterWarning"] = findViewById(com.example.sumppump3.R.id.constraintNoWater)
        warningStringToCard["lowBattery12Warning"] = findViewById(com.example.sumppump3.R.id.constraintLowBattery)
        warningStringToCard["noPumpControlWarning"] = findViewById(com.example.sumppump3.R.id.noPumpControlCard)





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

        reOrderCardsByTime()
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
            Log.i("warning2", warning)
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
            if (hour == 0){hour = 12}

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

           // Log.i("inGetTime", (java.time.Duration.between(timeFromString,nowTime) > java.time.Duration.ofMinutes(1)).toString())
            Log.i("durationBetween", java.time.Duration.between(timeFromString,nowTime).toString())
            Log.i("booleanDurationBetween", (java.time.Duration.between(timeFromString,nowTime) > java.time.Duration.ofMinutes(1)).toString())
            if (java.time.Duration.between(timeFromString,nowTime) > java.time.Duration.ofDays(1)){ //how long should a warning stay in the warnings page
                Log.i("warningStringToCard", warningStringToCard[warning].toString())
                warningStringToCard[warning]!!.visibility= VISIBLE //for testing switch back to GONE
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

        val cardsToTimes = LinkedHashMap<ConstraintLayout, LocalDateTime>()

        Log.i("warning size",warningStringToCard.size.toString())
        var n = 1
        for(warning in warningStringToCard.keys) {
            Log.i("n$", n.toString())
            Log.i("warning#", warning)
            val card = warningStringToCard[warning]!!
            Log.i("cardVis", card.toString())
            val timestamp = warningStringToTimeStamp[warning]!!
            Log.i("timestamp@!", timestamp.toString())
            cardsToTimes[card] = timestamp
            n +=1
        }
        val cardsToTimesSorted = LinkedHashMap<ConstraintLayout, LocalDateTime>()
        val timesSorted = cardsToTimes.values.sorted()
        for (sortedTime in timesSorted){
            for ((card, time) in cardsToTimes.entries){
                if (time == sortedTime){cardsToTimesSorted[card] = time }                }
            }
        Log.i("cardsToTimeSorted", cardsToTimesSorted.values.toString())
        Log.i("timesToCardsSize", cardsToTimes.size.toString())
        Log.i("timesToCards", cardsToTimes.keys.toString())


        //val timeToCard = timesToCards.entries.associate{ (k,v)-> v to k} //uno reverse
       // for (time in timeToCard.keys){Log.i("(unsorted) time", time.toString())}

        //val mapTimeToCards = timeToCard.toSortedMap()
        //val sortedTimes = mapTimeToCards.keys
        //Log.i("sortedTimes", sortedTimes.toString())
        //Log.i("mapCardToTimes", mapTimeToCards.entries.toString())


        var i= 1

        for ((card, time) in cardsToTimesSorted) {
            card.visibility = VISIBLE //for testing
            if (card.visibility == GONE){
                continue
            }
            Log.i("i in Warnings sortedCards", i.toString())
            Log.i("PleasebeSorted", time.toString())
            Log.i("@visibility", card.visibility.toString())

            Log.i("@visibility2", card.visibility.toString())
            when(i) {

                //arrange cards by time  //also yes this should be cleaned up by adding a function.
                9 ->{
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.highWaterCard)?.apply {
                        if (this.visibility == VISIBLE){


                            if (parent != null) {
                                Log.i("*parent", parent.toString())
                                Log.i("parentNotNull", i.toString())
                                Log.i("*BeforeParent", parent.toString())
                                val parent_ = parent
                                val child_ = findViewById<ConstraintLayout>(com.example.sumppump3.R.id.highWaterCard)
                                Log.i("highwaterCardBefore",findViewById<ConstraintLayout>(com.example.sumppump3.R.id.highWaterCard).toString() )

                                if ((parent as ViewGroup).childCount > 0){(parent as ViewGroup).removeView(child_)}
                                Log.i("AfterParent", parent_.toString())

                            }
                        }


                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.cardHighWater))!! as ViewGroup)
                }
                8 -> {
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.noPowerCardinWarnings)?.apply {
                    if (this.visibility == VISIBLE){

                        if (parent != null) {
                            Log.i("*parent", parent.toString())
                            Log.i("parentNotNull", i.toString())
                            val parent_ = parent
                            if ((parent as ViewGroup).childCount > 0) {
                                (parent as ViewGroup).removeView(this)
                            }


                        }
                    }

                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.secondCard))!! as ViewGroup)

                }
                7 -> {
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.sensorErrorCard)?.apply {
                        if (this.visibility == VISIBLE) {
                            if (parent != null) {
                                Log.i("*parent", parent.toString())
                                Log.i("parentNotNull", i.toString())
                                val parent_ = parent
                                if ((parent as ViewGroup).childCount > 0) {
                                    (parent as ViewGroup).removeView(this)
                                }

                            }
                        }
                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.thirdCard))!! as ViewGroup)

                }
                6 -> {
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.mainRunTimeCard)?.apply {
                        if (this.visibility == VISIBLE){
                            if (parent != null) {
                                val parent_ = parent
                                Log.i("*parent", parent.toString())
                                if ((parent as ViewGroup).childCount > 0) {
                                    (parent as ViewGroup).removeView(this)
                                }
                            }

                        }
                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.fourthCard))!! as ViewGroup)

                }
                5 -> {
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.constraintLayoutBackupRun)?.apply {
                        if (this.visibility == VISIBLE) {
                            if (parent != null) {
                                val parent_ = parent
                                Log.i("*parent", parent.toString())
                                if ((parent as ViewGroup).childCount > 0) {
                                    (parent as ViewGroup).removeView(this)
                                }
                            }

                        }
                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.fifthCard))!! as ViewGroup)

                }
                4 -> {
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.constraintNoWater)?.apply {
                        if (this.visibility == VISIBLE){
                            if (parent != null) {
                                val parent_ = parent
                                Log.i("*i", i.toString())
                                Log.i("(cheated)parent", findViewById<ConstraintLayout>(com.example.sumppump3.R.id.constraintNoWater).parent.toString())
                                Log.i("*parent", parent_.toString())
                                if ((parent as ViewGroup).childCount > 0) {
                                    (parent as ViewGroup).removeView(this)
                                }
                            }


                        }
                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.sixthCard))!! as ViewGroup)

                }
                3 -> {
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.constraintLowBattery)?.apply {
                        if (this.visibility == VISIBLE) {
                            if (parent != null) {
                                val parent_ = parent
                                Log.i("*parent", parent.toString())
                                if ((parent as ViewGroup).childCount > 0) {
                                    (parent as ViewGroup).removeView(this)
                                }
                            }

                        }
                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.seventhCard))!! as ViewGroup)

                }
                2 -> {
                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.noPumpControlCard)?.apply {
                        if (this.visibility == VISIBLE) {
                            if (parent != null) {
                                val parent_ = parent
                                Log.i("*parent", parent.toString())
                                if ((parent as ViewGroup).childCount > 0) {
                                    (parent as ViewGroup).removeView(this)
                                }
                            }

                        }
                    }
                    card.addTo(findViewById<ConstraintLayout>((R.id.eighthCard))!! as ViewGroup)

                }


                1 -> {

                    findViewById<ConstraintLayout>(com.example.sumppump3.R.id.serverErrorCard)?.apply {
                        if (this.visibility == VISIBLE) {
                            if (parent != null) {
                                val parent_ = parent
                                Log.i("*parent", parent.toString())

                                if ((parent as ViewGroup).childCount > 0) {
                                    (parent as ViewGroup).removeView(this)
                                }
                            }
                        }
                    }

                    card.addTo(findViewById<ConstraintLayout>(R.id.ninthCard)!! as ViewGroup)


                }
            }

            i += 1
        }
    }

    fun destroyItem(container: ViewGroup, `object`: Any?) {
        container.removeView(`object` as View?)
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

        val view = findViewById<ConstraintLayout>(R.id.noPowerCardinWarnings)
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
    private fun View?.addTo(parent: ViewGroup? ) {

        Log.i("attemptAddView", "trying to addview")



        this ?: return //this is where everything is getting fucked
        Log.i("thisExists", "congrats")
        Log.i("@parent", parent.toString())
        parent ?: return
        val parent_ = this.parent
        if (parent_ != null){(parent_ as ViewGroup).removeView(this)}
        Log.i("!addview", "addingView!!")

        parent.addView(this)
    }

}


