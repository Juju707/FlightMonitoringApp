package com.jujulad.skynetapp.flightnearby

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.httpRequest.HttpGetRequest


class FlightsNearbyActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var currentLocation: TextView
    private val location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flightsnearby)
        val searchBtn = findViewById<Button>(R.id.btn_search)
        currentLocation = findViewById(R.id.txt_current_coordinates)
        searchBtn.setOnClickListener {
            val l = getCurrentLocation()
            if (l != null) currentLocation.text = "Current location: ${l.latitude}; ${l.longitude}"
            else currentLocation.text = "Cannot get your location."

            val url =
                "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&flight_status=active"
            val thread = HttpGetRequest { printFlight(it) }
            thread.execute(url)

        }
    }

    private fun printFlight(r: HttpGetRequest) {

        println(
            "AAA ${r.flights
                .filter { it.lon != null }}"
        )
        Toast.makeText(applicationContext, "response = ${r.flights.size}", Toast.LENGTH_LONG).show()
    }

    private fun getCurrentLocation(): Location? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            currentLocation.text = "Grant permissions and click search again!"
            grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!isLocationEnabled()) {
            currentLocation.text = "Enable location and click search again!"
            return null
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        return null
    }

    private fun grantPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

}

