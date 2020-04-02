package com.jujulad.skynetapp.httpRequest

import android.os.AsyncTask
import com.jujulad.skynetapp.dataclasses.airportData
import com.jujulad.skynetapp.dataclasses.flightData
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class HttpGetRequest : AsyncTask<String, String, String>() {
    private var type="flight"
    private var flights= mutableListOf<flightData>()
    private var airports= mutableListOf<airportData>()
    override fun doInBackground(vararg urls: String?): String? {
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(urls[0])

            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 100000

            var inString = streamToString(urlConnection.inputStream)
            publishProgress(inString)
        } catch (ex: Exception) {

        } finally {
            urlConnection?.disconnect()
        }

        return " "
    }

    override fun onProgressUpdate(vararg result: String?) {
        try {
            //jakiś when
            when(type){
                "flight"->flights=getJSONflightData(*result)
                "airport"->airports=getJSONairportData(*result)
            }
        } catch (ex: Exception) {

        }
    }

    override fun onPostExecute(result: String?) {
        // Useless
    }
    //Przemyśl to jeszcze
    fun changeType(){
        type = if(type=="flight") "airport"
        else "flight"
    }
    fun getFlightsList():MutableList<flightData>{
        return flights
    }
    fun getAirportsList():MutableList<airportData>{
        return airports
    }
}

fun streamToString(inputStream: InputStream): String {
    val bufferReader = BufferedReader(InputStreamReader(inputStream))
    var line: String
    var result = ""

    try {
        do {
            line = bufferReader.readLine()
            if (line != null) {
                result += line
            }
        } while (line != null)
        inputStream.close()
    } catch (ex: Exception) { }
    return result
}

fun getJSONflightData(vararg values: String?):MutableList<flightData>{
    var json = JSONObject(values[0])
    var flights= mutableListOf<flightData>()
    val array = json.getJSONArray("data")
    for (i in 0 until array.length()) {
        val row: JSONObject = array.getJSONObject(i)
        var flight=flightData(row.getString("flight_data"),row.getString("flight_status"),row.getJSONArray("departure").getString(0),
            row.getJSONArray("departure").getString(2),row.getJSONArray("departure").getString(3),row.getJSONArray("departure").getString(7),
            row.getJSONArray("arrival").getString(0), row.getJSONArray("arrival").getString(2),row.getJSONArray("arrival").getString(3),
            row.getJSONArray("arrival").getString(7),row.getJSONArray("airline").getString(0))
        flights.add(flight)
    }
    return flights
}
fun getJSONairportData(vararg values: String?):MutableList<airportData>{
    //url=https://raw.githubusercontent.com/jbrooksuk/JSON-Airports/master/airports.json
    var json = JSONArray(values[0])
    var airports= mutableListOf<airportData>()
    for (i in 0 until json.length()) {
        val row: JSONObject = json.getJSONObject(i)
        var airport=airportData(row.getString("icao"),row.getString("iata"),row.getString("name"),row.getString("city"),row.getString("state"),
            row.getString("country"),row.getString("elevation"),row.getString("lat"),row.getString("lon"))
        airports.add(airport)
    }
    return airports
}
