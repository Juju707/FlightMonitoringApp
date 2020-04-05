package com.jujulad.skynetapp.dataclasses

import java.io.Serializable
//Klasa do trzymania danych o szczegółach lotu
data class FlightData(
    val flight_date: String = "",
    val flight_status: String = "",
    val dep_airport: String = "",
    val dep_iata: String = "",
    val dep_icao: String = "",
    val dep_time: String = "",
    val arr_airport: String = "",
    val arr_iata: String = "",
    val arr_icao: String = "",
    val arr_time: String = "",
    val airline: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val is_ground: Boolean? = null,
    val aircraft: String? = null
) : Serializable
//Klasa do trzymania danych o ilości natknięcia się na poszczególny samolot
data class Flight(
    val aircraft: String,
    val seen: MutableList<Map<String, Any>>
) : Serializable {
    //Nadpisana funkcja toString
    override fun toString(): String =
        "$aircraft; ${seen.last()["dep_airport"]} -> ${seen.last()["arr_airport"]}; seen: ${seen.size}"

    //Funkcja zwracająca informacje dla markera na mapie
    fun markerInfo(): String =
        "$aircraft, ${seen.last()["dep_airport"]} -> ${seen.last()["arr_airport"]}"

    //Funkcja zwracająca pełne informacje o położeniu geograficznym samolotu
    fun fullInfo(): String =
        """
                |$aircraft
                |seen: ${seen.size}:
                |${seen.joinToString("") { s ->
            "${s["time"]};\n ${s["dep_airport"]} -> ${s["arr_airport"]}; \n ${s["lat"]}, ${s["lon"]} \n *** \n"
        }}
            """.trimMargin()
}