package com.jujulad.skynetapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jujulad.skynetapp.login.LoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        grantPermission(Manifest.permission.INTERNET)
        val previouslyStarted = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)
        if (previouslyStarted) {
            showLogin()
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply()
        }

        val byAirport = findViewById<Button>(R.id.btn_airports)
        val nearby = findViewById<Button>(R.id.btn_nearby)

        byAirport.setOnClickListener {
            val intent = Intent(this, PickairportActivity::class.java)
            startActivity(intent)
        }
        nearby.setOnClickListener {
            val intent = Intent(this, FlightsnearbyActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun grantPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                1
            )
        }
    }

}
