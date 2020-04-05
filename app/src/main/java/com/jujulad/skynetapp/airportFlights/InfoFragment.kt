package com.jujulad.skynetapp.airportFlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jujulad.skynetapp.R

//Klasa reprezentująca zakładkę z informacją (tab[0])
class InfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_disc, container, false)
    }


    companion object {
        private const val TAG = "InfoFragment"
    }

}
