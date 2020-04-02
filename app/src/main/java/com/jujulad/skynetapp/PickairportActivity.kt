package com.jujulad.skynetapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class PickairportActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickairport)
        val history =
            (getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("history", "") ?: "").split(";").toMutableList()
        val airport = findViewById<EditText>(R.id.atxt_aiport)
        val search = findViewById<Button>(R.id.btn_search)
        val hislist = findViewById<ScrollView>(R.id.scview_lastairports)

        search.setOnClickListener {
            updateHistory(airport.text.toString(), history)

//            val intent = Intent(this, FlightsbyairportActivity::class.java)
//            startActivity(intent)

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
}