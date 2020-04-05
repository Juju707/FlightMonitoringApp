package com.jujulad.skynetapp.airportFlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.dataclasses.FlightData
//Klasa reprezentująca zakładkę z informacją (tab[0])
class InfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tab_disc, container, false)
        return root
    }


    companion object {
        private const val TAG = "InfoFragment"
    }

}
