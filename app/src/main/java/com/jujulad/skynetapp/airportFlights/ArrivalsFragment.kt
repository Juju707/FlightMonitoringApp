package com.jujulad.skynetapp.airportFlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.dataclasses.FlightData


class ArrivalsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tab_arrivals, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val a = activity as FlightsByAirportActivity
        val arrivals = a.changeData(a.intent.extras?.getSerializable("arrivals") as MutableList<FlightData>)
        a.findViewById<ListView>(R.id.list_arrivals)
        a.updateList(arrivals, a.findViewById(R.id.list_arrivals))
    }


    companion object {
        private const val TAG = "ArrivalsFragment"
    }

}
