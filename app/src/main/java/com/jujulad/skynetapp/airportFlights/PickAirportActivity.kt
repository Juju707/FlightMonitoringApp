package com.jujulad.skynetapp.airportFlights


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.jujulad.skynetapp.R
import com.jujulad.skynetapp.adapters.AutoSuggestAdapter
import com.jujulad.skynetapp.dataclasses.AirportData
import com.jujulad.skynetapp.httpRequest.HttpGetRequest
import org.json.JSONObject
import java.io.Serializable


class PickAirportActivity : AppCompatActivity() {
    private var choiceList = mutableListOf<String>()
    private var airporticao = ""
    val bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickairport)
        val atxt = findViewById<AutoCompleteTextView>(R.id.atxt_aiport)
        //Rozpoczyna się wczytywanie listy lotnisk z pliku
        val task = AsyncTaskRunner()
        task.execute()
        //Sprawdzane zostają poprzednie wyszukiwania z shared preferences
        val history =
            (getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("history", "") ?: "").split(";").toMutableList()
        updateList(history)

        val pb = findViewById<ProgressBar>(R.id.pb_progressbar)
        pb.visibility = View.INVISIBLE
        val search = findViewById<Button>(R.id.btn_search)
        val hislist = findViewById<ListView>(R.id.scview_lastairports)

        //Listener dla przycisku wyszukiwania.
        //Jeśli lotnisko zostało poprawnie wybrane, zostaje pokazany prograss bar oraz uruchamiany jest 1 wątek pobierający dane
        //W przypadku niepoprawnego wyboru lotniska(niewybrania z listy) wyświetlony zostaje komunikat
        search.setOnClickListener {
            if (airporticao != "") {
                pb.visibility = View.VISIBLE
                updateHistory(atxt.text.toString(), history)
                val thread = HttpGetRequest { getDepList(it) }
                //Departures
                val url =
                    "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&dep_icao=$airporticao"
                thread.execute(url)
            } else {
                Toast.makeText(applicationContext, "Please pick the airport from the dropdown list", Toast.LENGTH_LONG)
                    .show()
            }
        }
        //Listener dla text view z autosugestią. Dla podpowiadania wyników po zmianie tekstu
        atxt.setOnItemClickListener { parent, view, position, id ->
            separateData(parent.getItemAtPosition(position).toString())

        }
        //Listener dla listy uprzednio wyszukiwanych lotów, po kliknięciu wyboru, jego nazwa pokacuje się w polu tekstowym
        hislist.setOnItemClickListener { _, _, i, _ ->
            atxt.postDelayed(Runnable {
                atxt.setText(history[i])
                atxt.showDropDown()
            }, 10)

        }

    }

    //Funkcja wykonywana po zakończeniu działania 1 wątku.
    //Ustawia listę w pakiecie danych, a następnie rozpoczyna kolejny wątek
    private fun getDepList(r: HttpGetRequest) {
        bundle.putSerializable("departures", r.flights as Serializable)
        val t2 = HttpGetRequest { getArrList(it) }
        val url =
            "http://api.aviationstack.com/v1/flights?access_key=a2130ee26fddacb7c83f29e0f0f33c68&arr_icao=$airporticao"
        t2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url)

    }

    //Funkcja wykonywana dla 2 uruchamianego wątku. Ustawia kolejną listę w pakiecie danych, a następnie wysyła cały pakiet do aktywności,którą następnie otwiera.
    //Aktualna aktywność zostaje zniszczona
    private fun getArrList(r: HttpGetRequest) {
        bundle.putSerializable("arrivals", r.flights as Serializable)
        val intent = Intent(this, FlightsByAirportActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

    //Funkcja dla aktualizacji listy zawierającej dane o wyszukiwanych uprzednio lotniskach
    private fun updateList(flights: MutableList<String>) {
        val list = findViewById<ListView>(R.id.scview_lastairports)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, flights)
        list.adapter = adapter
    }

    //Funkcja dla aktualizowania historii wyszukiwań. Zaktualizowana lista zapisywana jest w Shared Preferences
    private fun updateHistory(newRecord: String, historyList: MutableList<String>) {
        historyList.add(0, newRecord)
        if (historyList.size > 5) {
            historyList.removeAt(historyList.lastIndex)
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
            .putString("history", historyList.joinToString(";")).apply()
    }

    //Funkcja do rzdzielania łańcucha znaków po średnikach oraz wyciąganie odpowiedniego kodu icao
    private fun separateData(choice: String) {
        val ifIs = choiceList.filter { value -> value.contains(choice) }
        val idx = choiceList.indexOf(ifIs[0])
        val parts = choiceList[idx].split(",")

        airporticao = parts[3]
    }

    //Funkcja do ustawiania adaptera na textview (dla autosugestii). Minimum wymaganych znaków ustawione jest na 4
    private fun setAdapter(list: MutableList<String>) {
        val airport = findViewById<AutoCompleteTextView>(R.id.atxt_aiport)
        val adapter = AutoSuggestAdapter(
            this@PickAirportActivity,
            android.R.layout.simple_list_item_1,
            list
        )

        airport.setAdapter(adapter)
        airport.threshold = 4

    }

    //Funkcja do czytania danych z pliku JSON
    private fun readJSON(): MutableList<AirportData> {

        val text = application.assets.open("airports.json").bufferedReader().use { it.readText() }
        val json = JSONObject(text)

        val airports = mutableListOf<AirportData>()

        for (key in json.keys()) {
            val row = JSONObject(json.get(key).toString())
            val airport =
                AirportData(
                    row.getString("icao"),
                    row.getString("iata"),
                    row.getString("name"),
                    row.getString("city"),
                    row.getString("state"),
                    row.getString("country"),
                    row.getString("elevation"),
                    row.getString("lat"),
                    row.getString("lon")
                )
            airports.add(airport)

        }
        return airports
    }

    //Zadanie asynchroniczne do wczytania listy lotnisk na świecie
    inner class AsyncTaskRunner : AsyncTask<Void, Void, MutableList<AirportData>>() {

        //Po zakończeniu pobierania danych z pliku JSON dane są przekazywane do adaptera
        override fun onPostExecute(result: MutableList<AirportData>) {
            val list = mutableListOf<String>()
            for (airport in result) {
                list.add("${airport.country},${airport.city},${airport.name}")
                choiceList.add("${airport.country},${airport.city},${airport.name},${airport.icao}")
            }
            setAdapter(list)
        }

        override fun doInBackground(vararg p0: Void?): MutableList<AirportData> {
            return readJSON()
        }

    }

}