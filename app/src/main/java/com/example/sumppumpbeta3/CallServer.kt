package com.example.sumppumpbeta3

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class CallServer {

    suspend fun run(): String? {
        Log.i("CallServer", "Initializing Call server in CallServer.kt")


        var firstRun: Boolean = true
        var responseString:String? = null


        //THIS USED TO BE IN A WHILE LOOP...MAYBE IT SHOULD BE?


        //val notifications = NotificationsSettings()

        try {
            Log.i("callServer", "trying in callSever")
            val parameters = mapOf<String, String>("firstRun" to firstRun.toString())
            Log.i("mainactivity oncreate", "calling get on sumppump.jeffs-handyman.net")
            //important note that getFromServer also applies data. So mainRunning_ will be be altered (if req) for example
            responseString = getFromServer("https://sumppump.jeffs-handyman.net/", parameters)

            if (responseString != null && responseString.startsWith("{\"", 0)){
                Log.i("responseString", responseString)
                Log.i("callServer", "radio tower view = true")
                responseStringReceived = true
                serverError = Pair(false, Clock.System.now())
                return responseString
            }
            else {
                responseStringReceived = false
                Log.i("callServer", "radio tower view = false")
                Log.i("hiddenServerError!", "responseString is null or erroring")
                if (responseString != null) {
                    Log.i("responsString", responseString)
                }
                if (!serverError.first) {
                    Log.i("CallServer","setting ServerError to true")
                    serverError = Pair(true, Clock.System.now())
                }
                return null
            }


        } catch (e: java.lang.Exception) {
            Log.i("serverError@#$*", Clock.System.now().toString())
            warningVisibilities["serverErrorWarning"] = Pair(1, Clock.System.now())
            Log.i("serverError", "yep that's a server error.")
            e.printStackTrace()
            if (!serverError.first) {
                serverError = Pair(true, Clock.System.now())
            }
            return null

            //now in evaluateResp. we need to kick back data to here in order to run a notificatin

        }

    }
    private val client = OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private suspend fun getFromServer(url: String?, params: Map<String, String>?): String? {
        url?.let {
            val httpBuilder: HttpUrl.Builder = it.toHttpUrlOrNull()!!.newBuilder()
            Log.i("in Get", "starting get")

            // Add parameters to the URL
            params?.forEach { (key, value) ->
                Log.i("in Get: key", key)
                Log.i("in Get: value", value)
                httpBuilder.addQueryParameter(key, value)
            }

            val request: Request = Request.Builder().url(httpBuilder.build()).build()
            Log.i("request", request.toString())

            return try {
                // Ensure this network operation happens on the IO thread
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()  // Executes on a background thread
                }

                // Handle the response
                if (response.isSuccessful) {
                    val responseString = response.body?.string()
                    Log.i("response string", responseString ?: "No response")
                    responseString
                } else {
                    Log.e("Error", "Request failed with code: ${response.code}")
                    "ERROR: ${response.message}"
                }
            } catch (e: IOException) {
                Log.e("Error", "Network request failed", e)
                null
            }
        } ?: run {
            Log.e("Error", "URL is null")
            return null
        }
    }

}