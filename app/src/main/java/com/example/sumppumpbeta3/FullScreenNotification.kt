package com.example.sumppumpbeta3


import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.sumppump3.R
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.widget.Button

class FullScreenNotificationActivity : AppCompatActivity() {
    private lateinit var wakeLock: PowerManager.WakeLock

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_notification)

        // Wake up the screen


        // Disable keyguard (lock screen) with requestDismissKeyguard
        //val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
       // keyguardManager.requestDismissKeyguard(this, null)

        // Keep the screen on
       // window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Make sure we’re in full-screen mode (hide status/navigation bars)


        // Set up the message from Intent
        val message = intent.getStringExtra("MESSAGE") ?: "No message available"

        findViewById<TextView>(R.id.messageTextView).text = message


    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the wake lock when the activity is destroyed
        if (this::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
        FullScreenNotificationService().stopSelf()
    }
}