package com.jujulad.skynetapp.AirportFlights

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.TabAdapter


class FlightsByAirportActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickairport)
        val mViewPager = findViewById<ViewPager>(R.id.container)
        setupViewPager(mViewPager)

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)
    }
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = TabAdapter(supportFragmentManager)
        adapter.addFragment(DeparturesFragment(), "Departures")
        adapter.addFragment(ArrivalsFragment(), "Arrivals")
        viewPager.adapter = adapter
    }
    private fun sendData(){
        var bundle=Bundle()
    }
}