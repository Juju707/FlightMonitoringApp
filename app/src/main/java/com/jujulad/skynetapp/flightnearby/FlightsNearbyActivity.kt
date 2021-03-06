@file:Suppress("UNCHECKED_CAST")

package com.jujulad.skynetapp.flightnearby

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.MapsActivity
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.dataclasses.AirportData
import com.jujulad.skynetapp.dataclasses.Flight
import com.jujulad.skynetapp.httpRequest.HttpGetRequest
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

//Aktywność pozwalająca na znalezienie lotów w promieniu 100 km od aktualnej lokalizacji uzytkownika.
// Pozwala na wyświetlenie maksymalnie 10 lotów.
//Nowo spotkane samoloty zastają zapisane do bazy danych,
// w przypadku ponownego spotkania tego samego samolotu zostaje zwiększony licznik spotkań.
class FlightsNearbyActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var currentLocation: TextView
    private var location: Location? = null
    private var offset: Double = 100.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flightsnearby)

        //Inicjalizacja elementów GUI
        val searchBtn = findViewById<Button>(R.id.btn_search)
        val showCurrent = findViewById<Button>(R.id.btn_show_map)
        val nearby = findViewById<TextView>(R.id.txt_radius)
        val pb = findViewById<ProgressBar>(R.id.pb_progressbar)
        currentLocation = findViewById(R.id.txt_current_coordinates)
        showCurrent.visibility = View.INVISIBLE
        nearby.text = getString(R.string.displaying_list)
        pb.visibility = View.INVISIBLE
        //Metoda wykonywana po kliknięciu przyciku szukaj.
        searchBtn.setOnClickListener {
            try {
                //Wyświetlenie prograss baru.

                //Pobranie obrecnej lokalizacji.
                location = getCurrentLocation()
                if (location != null) currentLocation.text =
                    getString(R.string.current_loc, location!!.latitude, location!!.longitude)
                else currentLocation.text = getString(R.string.no_loc)

                //Pobranie danych na temat aktualnych lotów.
                val url2 =
                    "https://aviation-edge.com/v2/public/flights?key=a32eb8-8cdda7&lat=${location!!.latitude}&lng=${location!!.longitude}&distance=$offset"
                val thread = HttpGetRequest { printFlight(it) }
                thread.execute(url2)
                pb.visibility = View.VISIBLE
            } catch (e: Exception) {
                currentLocation.text = getString(R.string.no_loc_again)
            }
        }

    }

    //Metoda wykonywana po tym, jak zostaną pobrane dane o lotach.
    @SuppressLint("SimpleDateFormat")
    private fun printFlight(r: HttpGetRequest) {
        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        //Pobrane zostaje 10 ze znalezionych lotów (lub mniej, jeśli tyle znaleziono)
        val flights = if (r.flights.size > 10) r.flights.subList(0, 10) else r.flights
        //Przefiltrowanie lotów na takie, które posiadają nazwę samolotu. (konieczne w przypadku drugiego API)
        //Zmapowanie listy uzyskanych lotów na obiekty typu Flight odpowiednie do bazy danych.
        val filteredFlight =
            flights
                .filter { it.aircraft != null }
                .map {
                    val ar = airportJSON()
                    val da = ar.firstOrNull { a -> a.iata == it.dep_airport }
                    val aa = ar.firstOrNull { a -> a.iata == it.arr_airport }
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
        //Uaktualnienie bazy danych o aktualnie znalezione loty.
        updateDatabase(filteredFlight)
        //Schowanie progress baru oraz wyświetlenie przycisku pozwalającego otworzyć aktywność z mapą.
        val showCurrent = findViewById<Button>(R.id.btn_show_map)
        val pb = findViewById<ProgressBar>(R.id.pb_progressbar)
        pb.visibility = View.INVISIBLE
        showCurrent.visibility = View.VISIBLE

        showCurrent.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("loc", "${location?.latitude ?: 0.0};${location?.longitude ?: 0.0}")
            intent.putExtra("flights", filteredFlight as Serializable)
            startActivity(intent)
        }
        Toast.makeText(applicationContext, "I found ${filteredFlight.size} / 10 flights nearby", Toast.LENGTH_LONG)
            .show()
    }

    //Wczytanie lotnisk z pliku JSON.
    private fun airportJSON(): MutableList<AirportData> {
        val text = application.assets.open("airports.json").bufferedReader().use { it.readText() }
        val json = JSONObject(text)
        val airports = mutableListOf<AirportData>()
        json.keys().forEach {
            val row = JSONObject(json[it].toString())
            airports.add(
                AirportData(
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
            )
        }
        return airports
    }

    //Uaktualnienie danych w bazie danych dla aktualnego użytkownika.
    private fun updateDatabase(flights: List<Flight>) {
        val user = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", "") ?: ""
        db.collection("users").document(user).get()
            .addOnSuccessListener { doc ->
                val pass = doc["password"] as String
                val flightsInDB = parseMapToFlights((doc["flights"] as List<Map<String, Any>>))
                val fl = flights.toMutableList()
                //Jesli dany samolot występuje w bazie danych - dodanie danych na temat aktualnie spotkanego, w przeciwnym razie dodanie samolotu do bazy danych
                fl.forEach { f ->
                    if (flightsInDB.filter { it.aircraft == f.aircraft }.size == 1)
                        flightsInDB.first { it.aircraft == f.aircraft }.seen.add(f.seen.last())
                    else flightsInDB.add(f)
                }
                //Aktualizacjia listy lotów.
                updateList(flightsInDB)
                db.collection("users").document(user).set(mapOf("flights" to flightsInDB, "password" to pass))
                    .addOnSuccessListener { Log.d("Nearby", "Success!") }
                    .addOnFailureListener { e -> Log.w("Nearby", "Error adding document", e) }

            }.addOnFailureListener { e -> Log.w("XXX", "Error writing document", e) }
    }

    //Konwersja danych pobranych z bazy danych na odpowiedni obiekt.
    private fun parseMapToFlights(list: List<Map<String, Any>>): MutableList<Flight> {
        return list.map {
            Flight(
                it["aircraft"] as String,
                it["seen"] as MutableList<Map<String, Any>>
            )
        }.toMutableList()
    }

    //Zaaktualizowanie listy lotów.
    private fun updateList(flights: MutableList<Flight>) {
        val list = findViewById<ListView>(R.id.flights_list)
        val flightsList = flights.map { it.toString() }.toTypedArray()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, flightsList)
        list.adapter = adapter
        list.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, NearbyDetailsActivity::class.java)
            intent.putExtra("info", flights[i].fullInfo())
            startActivity(intent)
        }
    }

    //Pobranie aktualnej lokalizacji.
    private fun getCurrentLocation(): Location? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            currentLocation.text = getString(R.string.grant_perm)
            grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            currentLocation.text = getString(R.string.grant_perm)
            grantPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isLocationEnabled()) {
            currentLocation.text = getString(R.string.enable_loc)
            return null
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    //Sprawdzenie pozwoleń na lokalizację.
    private fun grantPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)

    }

    //Sprawdzenie pozwoleń na lokalizację.
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

}

