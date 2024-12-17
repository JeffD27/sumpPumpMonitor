package com.example.sumppumpbeta3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.os.PowerManager

object BatteryOptimizationHelper {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_BATTERY_OPTIMIZATION_PROMPTED = "battery_optimization_prompted"

    fun requestBatteryOptimizationExemption(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = context.packageName
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            // Check if the app is already exempted or the user has been prompted before
            if (powerManager.isIgnoringBatteryOptimizations(packageName) ||
                sharedPreferences.getBoolean(KEY_BATTERY_OPTIMIZATION_PROMPTED, false)
            ) {
                return // Do nothing if already exempt or user was prompted
            }

            // Prompt the user
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            context.startActivity(intent)

            // Save the flag that user has been prompted
            sharedPreferences.edit().putBoolean(KEY_BATTERY_OPTIMIZATION_PROMPTED, true).apply()
        }
    }
}