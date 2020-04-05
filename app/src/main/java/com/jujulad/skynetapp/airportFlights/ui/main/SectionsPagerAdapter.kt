package com.jujulad.skynetapp.airportFlights.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.airportFlights.ArrivalsFragment
import com.jujulad.skynetapp.airportFlights.DeparturesFragment
import com.jujulad.skynetapp.airportFlights.InfoFragment


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

    // FunkcjagetItem wywoływana jest do stworzenia instancji przy zmianie wybranego fragmentu
    // Zwraca wybrany fragment
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> InfoFragment()
            1 -> ArrivalsFragment()
            2 -> DeparturesFragment()
            else -> PlaceholderFragment.newInstance(position + 1)
        }
    }

    // Funkcja getPageTitle wywoływana jest do kontroli nazw wybranych fragmentów
    // Zwraca nazwę zakładki
    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Disclaimer"
            1 -> "Arrivals"
            2 -> "Departures"
            else -> context.resources.getString(TAB_TITLES[position])
        }
    }

    //Funkcja ta zwraca ilość zakładek w TabLayout
    override fun getCount(): Int {
        return 3
    }


}