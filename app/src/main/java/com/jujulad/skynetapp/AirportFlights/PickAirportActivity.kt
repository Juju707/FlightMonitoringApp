package com.jujulad.skynetapp.AirportFlights

import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.R

class PickAirportActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var choiceList= mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickairport)
        val history =
            (getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("history", "") ?: "").split(";").toMutableList()
        val airport = findViewById<AutoCompleteTextView>(R.id.atxt_aiport)
        val search = findViewById<Button>(R.id.btn_search)
        val hislist = findViewById<ScrollView>(R.id.scview_lastairports)
        val adapter = AutoSuggestAdapter(this, R.layout.activity_pickairport, choiceList)
        var choice = ""
        airport.setAdapter(adapter)

        search.setOnClickListener {
            updateHistory(airport.text.toString(), history)

//            val intent = Intent(this, FlightsbyairportActivity::class.java)
//            startActivity(intent)

        }
        hislist.setOnClickListener{
        }
    }

    private fun updateHistory(newRecord: String, historyList: MutableList<String>) {
        historyList.add(0, newRecord)
        if (historyList.size > 5) {
            historyList.removeAt(historyList.lastIndex)
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
            .putString("history", historyList.joinToString(";")).apply()
    }
    private fun separateData(choice:String){

    }
}