package com.jujulad.skynetapp.httpRequest

import android.os.AsyncTask
import com.jujulad.skynetapp.dataclasses.FlightData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class HttpGetRequest(
    private val afterAction: (r: HttpGetRequest) -> Unit = {}
) : AsyncTask<String, String, String>() {

    var flights = mutableListOf<FlightData>()

    override fun doInBackground(vararg urls: String?): String? {
        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(urls[0])
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 100000

            val inString = streamToString(urlConnection.inputStream)
            publishProgress(inString)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            urlConnection?.disconnect()
        }


        return " "
    }

    override fun onProgressUpdate(vararg result: String?) {
        try {
            flights = getJSONflightData(*result)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onPostExecute(result: String?) {
        afterAction.invoke(this)
    }

    override fun onCancelled() {

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
    } catch (ex: Exception) {
    }
    return result
}

fun getJSONflightData(vararg values: String?): MutableList<FlightData> {

    val flights = mutableListOf<FlightData>()
    var avstack = true
    val array = try {
        val json = JSONObject(values[0])
        json.getJSONArray("data")
    } catch (e: JSONException) {
        avstack = false
        JSONArray(values[0])
    }
    for (i in 0 until array.length()) {
        val row = JSONObject(array.get(i).toString())
        if (!avstack) {
            val f = FlightData(
                dep_airport = row.getJSONObject("departure").getString("iataCode"),
                arr_airport = row.getJSONObject("arrival").getString("iataCode"),
                lat = row.getJSONObject("geography").getDouble("latitude"),
                lon = row.getJSONObject("geography").getDouble("longitude"),
                aircraft = row.getJSONObject("aircraft").getString("icaoCode")
            )
            flights.add(f)
        } else {
            val flight = if (row.getString("live") == "null")
                FlightData(
                    row.getString("flight_date"),
                    row.getString("flight_status"),
                    row.getJSONObject("departure").getString("airport"),
                    row.getJSONObject("departure").getString("iata"),
                    row.getJSONObject("departure").getString("icao"),
                    row.getJSONObject("departure").getString("estimated"),
                    row.getJSONObject("arrival").getString("airport"),
                    row.getJSONObject("arrival").getString("iata"),
                    row.getJSONObject("arrival").getString("icao"),
                    row.getJSONObject("arrival").getString("estimated"),
                    row.getJSONObject("airline").getString("name")
                )
            else FlightData(
                row.getString("flight_date"),
                row.getString("flight_status"),
                row.getJSONObject("departure").getString("airport"),
                row.getJSONObject("departure").getString("iata"),
                row.getJSONObject("departure").getString("icao"),
                row.getJSONObject("departure").getString("estimated"),
                row.getJSONObject("arrival").getString("airport"),
                row.getJSONObject("arrival").getString("iata"),
                row.getJSONObject("arrival").getString("icao"),
                row.getJSONObject("arrival").getString("estimated"),
                row.getJSONObject("airline").getString("name"),
                row.getJSONObject("live").getDouble("latitude"),
                row.getJSONObject("live").getDouble("longitude"),
                row.getJSONObject("live").getBoolean("is_ground"),
                row.getJSONObject("aircraft").getString("iata")
            )
            flights.add(flight)
        }
    }

    return flights
}

