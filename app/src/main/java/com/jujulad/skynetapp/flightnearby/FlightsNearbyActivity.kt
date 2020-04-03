package com.jujulad.skynetapp.flightnearby

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.dataclasses.Flight
import com.jujulad.skynetapp.httpRequest.HttpGetRequest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


class FlightsNearbyActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var currentLocation: TextView
    private var location: Location? = null
    private var offset: Double = Double.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flightsnearby)
        val searchBtn = findViewById<Button>(R.id.btn_search)
        currentLocation = findViewById(R.id.txt_current_coordinates)
        val nearby = findViewById<TextView>(R.id.txt_bbcoordinates)
        nearby.text = "Radius : ${if (offset == Double.MAX_VALUE) "whole word!" else offset}"
        searchBtn.setOnClickListener {
            location = getCurrentLocation()
            if (location != null) currentLocation.text =
                "Current location: ${location!!.latitude}; ${location!!.longitude}"
            else currentLocation.text = "Cannot get your location."

            val url =
                "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&flight_status=active"
            val thread = HttpGetRequest { printFlight(it) }
            thread.execute(url)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun printFlight(r: HttpGetRequest) {
        val l = location!!
        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val filteredFlight =
            r.flights
                .filter { it.is_ground == false }
                .filter {
                    (it.lat!! - l.latitude).pow(2) + (it.lon!! - l.longitude).pow(2) < offset.pow(2)
                }
                .map {
                    Flight(
                        it.aircraft!!,
                        it.dep_airport,
                        it.arr_airport,
                        it.lat!!,
                        it.lon!!,
                        mutableListOf(df.format(Calendar.getInstance().time))
                    )
                }
        updateDatabase(filteredFlight)
        println("AAA $filteredFlight")
        Toast.makeText(applicationContext, "I found ${filteredFlight.size} flights nearby", Toast.LENGTH_LONG).show()
    }

    private fun updateDatabase(flights: List<Flight>) {
        val user = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", "") ?: ""
        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        db.collection("users").document(user).get()
            .addOnSuccessListener { doc ->
                val pass = doc["password"] as String
                val flightsInDB = parseMapToflights((doc["flights"] as List<Map<String, Any>>))
                val fl = flights.toMutableList()
                fl.add(
                    Flight("x", "x", "x", 0.0, 0.0, mutableListOf(df.format(Calendar.getInstance().time)))
                )
                fl.forEach { f ->
                    if (flightsInDB.filter { it.aircraft == f.aircraft }.size == 1)
                        flightsInDB.first { it.aircraft == f.aircraft }.seen.add(f.seen.last())
                    else flightsInDB.add(f)
                }
                updateList(flightsInDB)

                db.collection("users").document(user).set(mapOf("flights" to flightsInDB, "password" to pass))
                    .addOnSuccessListener { Log.d("Nearby", "Success!") }
                    .addOnFailureListener { e -> Log.w("Nearby", "Error adding document", e) }


            }.addOnFailureListener { e -> Log.w("XXX", "Error writing document", e) }
    }


    private fun parseMapToflights(list: List<Map<String, Any>>): MutableList<Flight> {
        return list.map {
            Flight(
                it["aircraft"] as String,
                it["dep_airport"] as String,
                it["arr_airport"] as String,
                it["lat"] as Double,
                it["lon"] as Double,
                (it["seen"] as List<String>).toMutableList()
            )
        }.toMutableList()
    }


    private fun updateList(flights: MutableList<Flight>) {
        val list = findViewById<ListView>(R.id.flights_list)
        val flightsList = flights.map { it.toString() }.toTypedArray()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, flightsList)
        list.adapter = adapter
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
            try {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

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

