package com.jujulad.skynetapp.httpRequest

import android.os.AsyncTask
import com.jujulad.skynetapp.dataclasses.flightData
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class HttpGetRequest : AsyncTask<String, String, String>() {

    private var flights= mutableListOf<flightData>()

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
                flights=getJSONflightData(*result)

        } catch (ex: Exception) {

        }
    }

    override fun onPostExecute(result: String?) {
        // Useless
    }

    fun getFlightsList():MutableList<flightData>{
        return flights
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
        val row = JSONObject(array.get(i).toString())
        var flight=flightData(row.getString("flight_date"),row.getString("flight_status"),row.getJSONObject("departure").getString("airport"),
            row.getJSONObject("departure").getString("iata"),row.getJSONObject("departure").getString("icao"),row.getJSONObject("departure").getString("estimated"),
            row.getJSONObject("arrival").getString("airport"), row.getJSONObject("arrival").getString("iata"),row.getJSONObject("arrival").getString("icao"),
            row.getJSONObject("arrival").getString("estimated"),row.getJSONObject("airline").getString("name"))
        flights.add(flight)

    }
    return flights
}

