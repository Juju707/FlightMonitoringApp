package com.jujulad.skynetapp.airportFlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jujulad.skynetapp.R

class ArrivalsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tab_arrivals, container, false)
        return root
    }

    companion object {
        private const val TAG = "ArrivalsFragment"
    }
}