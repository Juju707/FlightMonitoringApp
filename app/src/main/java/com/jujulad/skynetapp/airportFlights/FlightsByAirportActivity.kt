package com.jujulad.skynetapp.airportFlights

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.airportFlights.ui.main.SectionsPagerAdapter
import com.jujulad.skynetapp.dataclasses.FlightData


class FlightsByAirportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flights_by_airport)
        val bundle = intent.extras
        val arrivals = changeData(bundle?.getSerializable("arrivals") as MutableList<FlightData>)
        val departures = changeData(bundle?.getSerializable("departures") as MutableList<FlightData>)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            // This method will be invoked when a new page becomes selected.

            override fun onPageSelected(position: Int) {
                when(position){
                    0->{
                        val list = findViewById<ListView>(R.id.list_arrivals)
                        updateList(arrivals,list)

                    }
                    1->{
                        val list = findViewById<ListView>(R.id.list_departures)
                        updateList(departures,list)

                    }
                }
            }

            // This method will be invoked when the current page is scrolled
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) { // Code goes here
            }

            // Called when the scroll state changes:
            override fun onPageScrollStateChanged(state: Int) { // Code goes here
            }
        })

    }
    private fun updateList(flights: MutableList<String>,list:ListView) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, flights)
        list.adapter = adapter
    }

    private fun changeData(list:MutableList<FlightData>):MutableList<String>{
        var newList= mutableListOf<String>()
        for(flight in list){
            newList.add(" Flight date: ${flight.flight_date}\n Flight status: ${flight.flight_status}\n Departure airport: ${flight.dep_airport}\n Departure time: ${flight.dep_time.replace("T"," ")}\n Arrival airport=${flight.arr_airport}\n Arrival time: ${flight.arr_time.replace("T", " ")}\n Airline: ${flight.airline}")
        }
        return newList
    }

}