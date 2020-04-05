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

//Klasa dla pobierania danych asynchronicznie do głównego wątku
class HttpGetRequest(
    private val afterAction: (r: HttpGetRequest) -> Unit = {}
) : AsyncTask<String, String, String>() {

    var flights = mutableListOf<FlightData>()
    //Funkacja wykonująca działanie w tle. Jej zadaniem jest połączenie się, pobranie z REST API odpowiedniego pliku JSON w
    // postaci łancucha znaków a następnie zakończenie połączenia
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

    //Funkcja wykonująca działanie przy postępie, w tym przypadku przy pobraniu informacji z REST API
    override fun onProgressUpdate(vararg result: String?) {
        try {
            flights = getJSONflightData(*result)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    //Funkcja dla wykonania zdefiniowanej, w deklaracji klasy, funkcji, która ma się wykonać od razu
    // po skończeniu przetwarzasnia polecenia (czyli pobierania i przetwarzania danych o locie)
    override fun onPostExecute(result: String?) {
        afterAction.invoke(this)
    }


}

//Funkcja dla przetwarzania strumieni przychodzących danych na jeden łańcuch znaków
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

//Funkcja do zamiany łańcucha znaków na plik JSON, a następnie, w zależności od typu uzyskanych danych,
// dodanie wszystkich ważnych informacji do obiektów danych, które następnie dodawane są do listy
fun getJSONflightData(vararg values: String?): MutableList<FlightData> {

    val flights = mutableListOf<FlightData>()
    var avstack = true //Pozwala na rozróżnienie z którego api korzystano
    //Dane otrzymywane z obu api mają różne struktury po czym można je rozróżnić.
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
            //Pobranie danych o obecnych lotach w promieniu 100km od zadanej lokalizacji.
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

