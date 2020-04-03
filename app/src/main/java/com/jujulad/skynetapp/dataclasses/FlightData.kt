package com.jujulad.skynetapp.dataclasses

data class FlightData(
    val flight_date: String,
    val flight_status: String,
    val dep_airport: String,
    val dep_iata: String,
    val dep_icao: String,
    val dep_time: String,
    val arr_airport: String,
    val arr_time: String,
    val arr_iata: String,
    val arr_icao: String,
    val airline: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val is_ground: Boolean? = null,
    val aircraft: String? = null
)

data class Flight(
    val aircraft: String,
    val dep_airport: String,
    val arr_airport: String,
    val lat: Double,
    val lon: Double,
    val seen: MutableList<String>
) {
    override fun toString(): String =
        "$aircraft; $dep_airport -> $arr_airport; $lat,$lon; seen: ${seen.size}"
}