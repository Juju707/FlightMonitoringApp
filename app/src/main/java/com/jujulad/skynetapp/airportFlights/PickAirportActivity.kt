package com.jujulad.skynetapp.airportFlights


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.adapters.AutoSuggestAdapter
import com.jujulad.skynetapp.dataclasses.AirportData
import com.jujulad.skynetapp.httpRequest.HttpGetRequest
import org.json.JSONObject
import java.io.Serializable


class PickAirportActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var choiceList= mutableListOf<String>()
    private var icaoList= mutableListOf<String>()
    private var airporticao=""
    val bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickairport)
        val atxt = findViewById<AutoCompleteTextView>(R.id.atxt_aiport)
        var task=AsyncTaskRunner()
        task.execute()
        val history =
            (getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("history", "") ?: "").split(";").toMutableList()
        updateList(history)

        val pb=findViewById<ProgressBar>(R.id.pb_progressbar)
        pb.visibility= View.INVISIBLE
        val search = findViewById<Button>(R.id.btn_search)
        val hislist = findViewById<ListView>(R.id.scview_lastairports)

        search.setOnClickListener {
            if (airporticao!=""){
                pb.visibility= View.VISIBLE
                updateHistory(atxt.text.toString(), history)
                var thread= HttpGetRequest{getDepList(it)}
                //Departures
                var url = "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&dep_icao=$airporticao"
                thread.execute(url)
            }else{
                Toast.makeText(applicationContext, "Please pick the airport from the dropdown list", Toast.LENGTH_LONG).show()
            }
        }

        atxt.setOnItemClickListener { parent,view,position,id->
            separateData(parent.getItemAtPosition(position).toString())

        }
        hislist.setOnItemClickListener{_, _, i, _ ->
            atxt.postDelayed(Runnable {
                atxt.setText(history[i])
                atxt.showDropDown()
            }, 10)

        }

    }
    private fun getDepList(r: HttpGetRequest) {
        bundle.putSerializable("departures", r.flights as Serializable)
        val t2 = HttpGetRequest{ getArrList(it)}
        var url = "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&arr_icao=$airporticao"
        t2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url)

    }
    private fun getArrList(r: HttpGetRequest) {
        bundle.putSerializable("arrivals", r.flights as Serializable)
        val intent = Intent(this, FlightsByAirportActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

    private fun updateList(flights: MutableList<String>) {
        val list = findViewById<ListView>(R.id.scview_lastairports)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, flights)
        list.adapter = adapter
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
        var ifIs=choiceList.filter { value-> value.contains(choice) }
        var idx=choiceList.indexOf(ifIs[0])
        var parts=choiceList[idx].split(",")

        airporticao=parts[3]
    }
    private fun setAdapter(list:MutableList<String>){
        val airport = findViewById<AutoCompleteTextView>(R.id.atxt_aiport)
        val adapter = AutoSuggestAdapter(
            this@PickAirportActivity,
            android.R.layout.simple_list_item_1,
            list
        )

        airport.setAdapter(adapter)
        airport.threshold = 4

    }
    private fun readJSON():MutableList<AirportData>{

        var text= application.assets.open("airports.json").bufferedReader().use { it.readText() }
        var json = JSONObject(text)

        var airports= mutableListOf<AirportData>()

        for (key in json.keys()) {
            val row=JSONObject(json.get(key).toString())
            var airport=
                AirportData(row.getString("icao"),row.getString("iata"),row.getString("name"),row.getString("city"),row.getString("state"),
                    row.getString("country"),row.getString("elevation"),row.getString("lat"),row.getString("lon"))
            airports.add(airport)

        }
        return airports
    }
    inner class AsyncTaskRunner : AsyncTask<Void,Void,MutableList<AirportData>>() {

        override fun onPostExecute(result: MutableList<AirportData>) {
            val list= mutableListOf<String>()
            for(airport in result){
                list.add("${airport.country},${airport.city},${airport.name}")
                choiceList.add("${airport.country},${airport.city},${airport.name},${airport.icao}")
            }
                setAdapter(list)
        }

        override fun doInBackground(vararg p0: Void?): MutableList<AirportData>{
            return readJSON()
        }

    }

}