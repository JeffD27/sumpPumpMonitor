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
import kotlin.time.Duration.Companion.seconds

class CallServer {

    suspend fun run(client: OkHttpClient): String? {


        var firstRun: Boolean = true
        var responseString:String? = null


        //THIS USED TO BE IN A WHILE LOOP...MAYBE IT SHOULD BE?


        //val notifications = NotificationsSettings()

        try {
            val parameters = mapOf<String, String>("firstRun" to firstRun.toString())
            Log.d("mainactivity oncreate", "calling get on sumppump.jeffs-handyman.net")
            //important note that getFromServer also applies data. So mainRunning_ will be be altered (if req) for example
            responseString = getFromServer(client,"https://sumppump.jeffs-handyman.net/", parameters)

            if (responseString != null && responseString.startsWith("{\"", 0)){
                Log.d("responseString", responseString)
                responseStringReceived = true
                serverError = Pair(false, Clock.System.now())
                return responseString
            }
            else {
                responseStringReceived = false
                if (responseString != null) {
                    Log.d("responsString", responseString)
                }
                if (!serverError.first) {
                    serverError = Pair(true, Clock.System.now())
                }
                return null
            }


        } catch (e: java.lang.Exception) {
            Log.d("serverError@#$*", Clock.System.now().toString())
            warningVisibilities["serverErrorWarning"] = Pair(1, Clock.System.now())
            e.printStackTrace()
            if (!serverError.first) {
                serverError = Pair(true, Clock.System.now())
            }
            return null

            //now in evaluateResp. we need to kick back data to here in order to run a notificatin

        }

    }

    private suspend fun getFromServer(client: OkHttpClient, url: String?, params: Map<String, String>?): String? {
        url?.let {
            val httpBuilder: HttpUrl.Builder = it.toHttpUrlOrNull()!!.newBuilder()

            // Add parameters to the URL
            params?.forEach { (key, value) ->

                httpBuilder.addQueryParameter(key, value)
            }

            val request: Request = Request.Builder().url(httpBuilder.build()).build()

            return try {
                // Ensure this network operation happens on the IO thread
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()  // Executes on a background thread
                }

                // Handle the response
                if (response.isSuccessful) {
                    val responseString = response.body?.string()
                    Log.d("response string", responseString ?: "No response")
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

    private val debounceThreshold = 45L
    private var lastCallTime = 0L
    suspend fun runWithDebounce(client: OkHttpClient): String? {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCallTime > debounceThreshold) {
            lastCallTime = currentTime
            return run(client)
        }
        return null
    }

}