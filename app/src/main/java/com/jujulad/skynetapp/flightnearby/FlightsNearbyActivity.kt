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
import com.jujulad.skynetapp.dataclasses.airportData
import com.jujulad.skynetapp.httpRequest.HttpGetRequest
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class FlightsNearbyActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var currentLocation: TextView
    private var location: Location? = null
    private var offset: Double = 100.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flightsnearby)
        val searchBtn = findViewById<Button>(R.id.btn_search)
        currentLocation = findViewById(R.id.txt_current_coordinates)
        val nearby = findViewById<TextView>(R.id.txt_bbcoordinates)
        nearby.text = "Radius : ${if (offset == Double.MAX_VALUE) "whole word!" else offset} km"
        searchBtn.setOnClickListener {
            location = getCurrentLocation()
            if (location != null) currentLocation.text =
                "Current location: ${location!!.latitude}; ${location!!.longitude}"
            else currentLocation.text = "Cannot get your location."
            val url2 =
                "https://aviation-edge.com/v2/public/flights?key=a32eb8-8cdda7&lat=${location!!.latitude}&lng=${location!!.longitude}&distance=$offset"

//            val url =
//                "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&flight_status=active"
            val thread = HttpGetRequest { printFlight(it) }
            thread.execute(url2)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun printFlight(r: HttpGetRequest) {
        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val filteredFlight =
            r.flights
                .filter { it.aircraft != null }
                .map {
                    val da = airportJSON().firstOrNull { a -> a.iata == it.dep_airport }
                    val aa = airportJSON().firstOrNull { a -> a.iata == it.arr_airport }
                    Flight(
                        it.aircraft!!,
                        mutableListOf(
                            mapOf(
                                "dep_airport" to "${da?.name ?: it.dep_airport} , ${da?.city ?: ""}",
                                "arr_airport" to "${aa?.name ?: it.arr_airport}, ${aa?.city ?: ""}",
                                "time" to df.format(Calendar.getInstance().time),
                                "lat" to it.lat!!,
                                "lon" to it.lon!!
                            )
                        )
                    )
                }
        updateDatabase(filteredFlight)
        Toast.makeText(applicationContext, "I found ${filteredFlight.size} flights nearby", Toast.LENGTH_LONG).show()
    }

    private fun airportJSON(): MutableList<airportData> {

        var text = application.assets.open("airports.json").bufferedReader().use { it.readText() }
        var json = JSONObject(text)
        var airports = mutableListOf<airportData>()
        for (key in json.keys()) {
            val row = JSONObject(json.get(key).toString())
            var airport =
                airportData(
                    row.getString("icao"),
                    row.getString("iata"),
                    row.getString("name"),
                    row.getString("city"),
                    row.getString("state"),
                    row.getString("country"),
                    row.getString("elevation"),
                    row.getString("lat"),
                    row.getString("lon")
                )
            airports.add(airport)
        }
        return airports
    }

    private fun updateDatabase(flights: List<Flight>) {
        val user = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", "") ?: ""
        db.collection("users").document(user).get()
            .addOnSuccessListener { doc ->
                val pass = doc["password"] as String
                val flightsInDB = parseMapToFlights((doc["flights"] as List<Map<String, Any>>))
                val fl = flights.toMutableList()

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


    private fun parseMapToFlights(list: List<Map<String, Any>>): MutableList<Flight> {
        return list.map {
            Flight(
                it["aircraft"] as String,
                it["seen"] as MutableList<Map<String, Any>>
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

