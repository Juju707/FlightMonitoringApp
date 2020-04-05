package com.jujulad.skynetapp.airportFlights

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.airportFlights.ui.main.SectionsPagerAdapter
import com.jujulad.skynetapp.dataclasses.FlightData


class FlightsByAirportActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    //Pobiearne są dane z poprzedniej aktywności, ustawiany jest odpowiedni adapter i pager dla fragmentów na layoucie z zakładkami
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

            // W zależności od wybranej zakładki, nastąpi zmiana fragmentu oraz aktualizacja odpowiedniej listy
            override fun onPageSelected(position: Int) {
                when(position){
                    1->{
                        val list = findViewById<ListView>(R.id.list_arrivals)
                        updateList(arrivals,list)

                    }
                   2->{
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
    //Funkcja dla aktualizacji listy zawierającej dane o lotach
    private fun updateList(flights: MutableList<String>,list:ListView) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, flights)
        list.adapter = adapter
    }
    //Funkcja dla zmiany FlightData na łańcuchy znaków z informacjami dla użytkownika( pomijane są takie dane jak np. kody icao)
    private fun changeData(list:MutableList<FlightData>):MutableList<String>{
        var newList= mutableListOf<String>()
        for(flight in list){
            newList.add(" Flight date: ${flight.flight_date}\n Flight status: ${flight.flight_status}\n Departure airport: ${flight.dep_airport}\n Departure time: ${flight.dep_time.replace("T"," ")}\n Arrival airport=${flight.arr_airport}\n Arrival time: ${flight.arr_time.replace("T", " ")}\n Airline: ${flight.airline}")
        }
        return newList
    }

}