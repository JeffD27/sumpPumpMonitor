package com.example.sumppumpbeta3


import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sumppump3.R

class RunningService: Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null

    }
    //this gets triggered whenever an android component sends an intent to the running service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("RunningService","onStartCommand in Running Service")

        when(intent?.action){
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }
    //this space is an activity area...just without viewing. you can override oncreate and other functions
    private fun start(){
        Log.i("RunningService","instart in Running Service")

        val notification = NotificationCompat.Builder(this, getString(R.string.generalInfoChannel))
            .setSmallIcon(R.drawable.flood_house_svg)
            .setContentTitle("Sump Pump Monitor is Running")
            .setContentText("Sump Pump Monitor is running in background")
            .build()
        startForeground(11113, notification)
    }

    enum class Actions{
        START, STOP
    }


}