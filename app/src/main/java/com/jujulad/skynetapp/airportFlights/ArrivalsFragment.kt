package com.jujulad.skynetapp.airportFlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jujulad.skynetapp.R

//Klasa reprezentująca zakładkę z listą samolotów przylatujących (tab[1])
class ArrivalsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_arrivals, container, false)
    }

    companion object {
        private const val TAG = "ArrivalsFragment"
    }

}
