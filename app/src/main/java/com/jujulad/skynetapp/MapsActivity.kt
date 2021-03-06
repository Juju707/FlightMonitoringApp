package com.jujulad.skynetapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.jujulad.skynetapp.dataclasses.Flight

//Klasa wyświetlają mapy. Zawiera znacznik pokazujący obecną lokalizację oraz znaczniki oznaczające samoloty.
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val loc = intent.getStringExtra("loc")!!.split(";")
        val flights = intent.getSerializableExtra("flights") as List<Flight>
        val current = LatLng(loc[0].toDouble(), loc[1].toDouble())
        mMap.addMarker(MarkerOptions().position(current).title("Current location."))
        val circle = mMap.addCircle(
            CircleOptions().center(current).radius(100000.0)
        )
        val z = getZoomLevel(circle).toFloat()
        mMap.animateCamera(CameraUpdateFactory.zoomTo(z))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
        //dodanie znaczników dla samolotów
        flights.forEach {
            val l = LatLng(it.seen.last()["lat"] as Double, it.seen.last()["lon"] as Double)
            val marker = MarkerOptions().position(l).title(it.markerInfo())
            marker.flat(true)
            //marker
            mMap.addMarker(marker).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.plane))
        }
    }

    private fun getZoomLevel(circle: Circle): Int {
        val radius = circle.radius
        val scale = radius / 500
        return (16 - Math.log(scale) / Math.log(2.0)).toInt()
    }
}
