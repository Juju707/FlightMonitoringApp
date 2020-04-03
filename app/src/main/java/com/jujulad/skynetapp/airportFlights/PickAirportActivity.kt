package com.jujulad.skynetapp.airportFlights


import android.os.AsyncTask
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.adapters.Test
import com.jujulad.skynetapp.dataclasses.airportData
import com.jujulad.skynetapp.httpRequest.HttpGetRequest
import org.json.JSONObject


class PickAirportActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var choiceList= mutableListOf<String>()
    private var icaoList= mutableListOf<String>()
    private var airporticao="ZYYJ"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickairport)
        val task=AsyncTaskRunner()
        task.execute()
        val thread= HttpGetRequest()
        val history =
            (getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("history", "") ?: "").split(";").toMutableList()

        val search = findViewById<Button>(R.id.btn_search)
        //val hislist = findViewById<ScrollView>(R.id.scview_lastairports)

        /// PIERDOLĘ TO
        var choice = ""

        search.setOnClickListener {
            //updateHistory(airport.text.toString(), history)

            //Departures
            //var url = "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&dep_iata=$airport"

            //thread.execute(url)
           // val departures=thread.getFlightsList()
            //Arrivals
            //url = "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&arr_iata=$airport"
           // thread.execute(url)
           // val arrivals=thread.getFlightsList()

           // val bundle = Bundle()
           // bundle.putSerializable("departures",departures as Serializable)
            //bundle.putSerializable("arrivals",arrivals as Serializable)

            //val intent = Intent(this, FlightsByAirportActivity::class.java)
            //intent.putExtras(bundle)
            //startActivity(intent)
        }
        //hislist.setOnClickListener{
        //}
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
    private fun setAdapter(){
        val airport = findViewById<AutoCompleteTextView>(R.id.atxt_aiport)

        val adapter = Test(
            this@PickAirportActivity,
            android.R.layout.simple_list_item_1,
            choiceList
        )
        airport.setAdapter(adapter)
        airport.threshold = 2

    }
    private fun readJSON():MutableList<airportData>{

        var text= application.assets.open("airports.json").bufferedReader().use { it.readText() }
        var json = JSONObject(text)

        var airports= mutableListOf<airportData>()

        for (key in json.keys()) {
            val row=JSONObject(json.get(key).toString())
            var airport=
                airportData(row.getString("icao"),row.getString("iata"),row.getString("name"),row.getString("city"),row.getString("state"),
                    row.getString("country"),row.getString("elevation"),row.getString("lat"),row.getString("lon"))
            airports.add(airport)

        }
        return airports
    }
    inner class AsyncTaskRunner : AsyncTask<Void,Void,MutableList<airportData>>() {

        override fun onPostExecute(result: MutableList<airportData>) {
            val list= mutableListOf<String>()
            for(airport in result){
                list.add("${airport.country},${airport.city},${airport.name}")
                icaoList.add(airport.icao)
            }
            choiceList=list
            println("done "+choiceList[0])
            setAdapter()
        }

        override fun doInBackground(vararg p0: Void?): MutableList<airportData>{
            return readJSON()
        }

    }

}