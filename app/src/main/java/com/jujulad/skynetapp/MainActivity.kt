package com.jujulad.skynetapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jujulad.skynetapp.airportFlights.PickAirportActivity
import com.jujulad.skynetapp.flightnearby.FlightsNearbyActivity
import com.jujulad.skynetapp.login.LoginActivity

//Głowna aktywność. Sprawdza pozwolenia na internet oraz lokalizację.
//Pozwala na przejście do wyszukiwania lotów po lotniskach bądź do przeglądania lotów w pobliżu.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Jeśli nie ma zalogowanego użytkownika aplikacja przenosi do ekranu logowania.
        val previouslyStarted = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", "")
        if (previouslyStarted == "") showLogin()

        grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        grantPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        grantPermission(Manifest.permission.INTERNET)

        val byAirport = findViewById<Button>(R.id.btn_search)
        val nearby = findViewById<Button>(R.id.btn_nearby)

        byAirport.setOnClickListener {
            val intent = Intent(this, PickAirportActivity::class.java)
            startActivity(intent)
        }
        nearby.setOnClickListener {
            val intent = Intent(this, FlightsNearbyActivity::class.java)
            startActivity(intent)

        }
    }

    //Otwiera aktywność z logowaniem.
    private fun showLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    //Zbiera pozwolenia.
    private fun grantPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
    }

}
