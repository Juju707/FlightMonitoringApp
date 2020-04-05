package com.jujulad.skynetapp.airportFlights.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.airportFlights.ArrivalsFragment
import com.jujulad.skynetapp.airportFlights.DeparturesFragment
import com.jujulad.skynetapp.airportFlights.InfoFragment
import com.jujulad.skynetapp.dataclasses.FlightData


private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        when(position){
            0->{var frag=InfoFragment(); return frag}
            1->{var frag=ArrivalsFragment(); return frag}
            2->{var frag=DeparturesFragment(); return frag}
        }
        return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0->{return "Disclaimer"}
            1->{return "Arrivals"}
            2->{return "Departures"}
        }
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 3
    }


}