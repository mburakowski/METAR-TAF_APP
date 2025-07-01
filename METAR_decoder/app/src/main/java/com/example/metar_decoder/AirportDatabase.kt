package com.example.metar_decoder

import android.content.Context

/**
 * Reprezentuje dane lotniska ładowane z assets/airports.csv.
 */
data class Airport(
    val icao: String,
    val name: String,
    val country: String,
    val region: String,
    val latitude: Double,
    val longitude: Double
)

object AirportDatabase {
    // Mapa ICAO -> Airport
    private val airportMap = mutableMapOf<String, Airport>()

    /**
     * Ładuje plik assets/airports.csv do [airportMap].
     * Format CSV zakłada kolumny:
     * 0: countryCode, 1: regionName, 2: iata, 3: icao, 4: name, 5: latitude, 6: longitude
     */
    fun load(context: Context) {
        if (airportMap.isNotEmpty()) return  // już załadowano

        context.assets.open("airports.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",").map { it.trim('"') }
                if (tokens.size >= 7) {
                    val icao = tokens[3]
                    if (icao.isNotBlank()) {
                        val airport = Airport(
                            icao     = icao,
                            name     = tokens[4].ifBlank { "–" },
                            country  = tokens[0].ifBlank { "–" },
                            region   = tokens[1].ifBlank { "–" },
                            latitude = tokens[5].toDoubleOrNull() ?: 0.0,
                            longitude= tokens[6].toDoubleOrNull() ?: 0.0
                        )
                        airportMap[icao] = airport
                    }
                }
            }
        }
    }

    /** Zwraca pełne dane lotniska lub null, jeśli nie znaleziono wpisu. */
    fun getAirport(icao: String): Airport? = airportMap[icao]

    /** Zwraca nazwę lotniska lub null. */
    fun getNameForIcao(icao: String): String? = airportMap[icao]?.name

    /** Zwraca kraj lotniska lub null. */
    fun getCountry(icao: String): String? = airportMap[icao]?.country

    /** Zwraca region lotniska lub null. */
    fun getRegion(icao: String): String? = airportMap[icao]?.region

    /** Zwraca szerokość geograficzną lotniska lub null. */
    fun getLatitude(icao: String): Double? = airportMap[icao]?.latitude

    /** Zwraca długość geograficzną lotniska lub null. */
    fun getLongitude(icao: String): Double? = airportMap[icao]?.longitude
}
