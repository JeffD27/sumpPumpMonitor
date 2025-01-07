package com.example.sumppumpbeta3

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.sumppump3.R
import com.example.sumppump3.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
private val client = OkHttpClient().newBuilder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .build()
class CallServerWorker(context: Context, workerParameter: WorkerParameters): CoroutineWorker(context, workerParameter) {
    //val mContext = context
    @RequiresApi(Build.VERSION_CODES.O)

    override suspend fun doWork(): Result {
        Log.d("doWork", "starting")

        val context = applicationContext
        LoopHandler().run(context)//doesn't call server, but this preps the variables for the loop

        //initiateDeployedVariables() handled in loopHandler
        //setup variables to determine if notifications should run...
        Log.d("callServer", "calling Server")



        val responseString = CallServer().runWithDebounce(client)

        if (responseString == null){
            Log.d("callserverworker", "ResponseString is null!!!")
            if (!serverError.first) {
                serverError = Pair( true, Clock.System.now())}
            EvaluateResponse().checkServerError(context)
        }
        else{
            Log.d("responseString(())", responseString!!)
            EvaluateResponse().onCreate(context, responseString!!,null )
        }

        return Result.success()


    }

}