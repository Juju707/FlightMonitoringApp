package com.jujulad.skynetapp.airportFlights

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.airportFlights.ui.main.SectionsPagerAdapter
import com.jujulad.skynetapp.dataclasses.FlightData


class FlightsByAirport : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flights_by_airport)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val bundle = intent.extras
        val a = bundle?.getSerializable("arrivals") as List<FlightData>
        a.forEach { println(it.aircraft) }
    }
}