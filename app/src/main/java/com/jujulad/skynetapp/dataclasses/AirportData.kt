package com.jujulad.skynetapp.dataclasses

//Klasa dla trzymania danych o lotnisku
data class AirportData(
    val icao: String,
    val iata: String,
    val name: String,
    val city: String,
    val state: String,
    val country: String,
    val elevation: String,
    val lat: String,
    val lon: String
)