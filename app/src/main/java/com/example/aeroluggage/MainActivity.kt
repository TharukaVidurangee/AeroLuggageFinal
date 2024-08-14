package com.example.aeroluggage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toolbar


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Delay of 3 seconds before switching to SecondActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
