package com.example.metar_decoder

data class TafResponse(
    val station: String,
    val time: TimeInfo,
    val start_time: TimeInfo,
    val end_time: TimeInfo,
    val raw: String,
    val forecast: List<Forecast>,
    val remarks: String?
)

data class TimeInfo(
    val dt: String?,      // ISO format, np. "2019-10-25T21:00:00+00:00Z"
    val repr: String?     // Skrócony format, np. "2521"
)

data class Forecast(
    val type: String?,                    // "FROM", "TEMPO", "BECMG", itd.
    val start_time: TimeInfo,
    val end_time: TimeInfo,
    val wind_direction: WindComponent?,
    val wind_speed: WindComponent?,
    val visibility: Visibility?,
    val clouds: List<Clouds> = emptyList(),
    val other: List<String> = emptyList(),     // np. ["VCSH"]
    val altimeter: String?,
    val flight_rules: String?,
    val probability: String?,
    val raw: String?,
    val sanitized: String?,
    val wind_gust: WindComponent?,
    val wind_shear: Any?,       // może być obiektem, rzadko używane – pozostawione jako Any?
    val icing: List<Any>?,      // szczegóły mogą być dodane jeśli potrzebne
    val turbulance: List<Any>?, // szczegóły mogą być dodane jeśli potrzebne
)

data class WindComponent(
    val value: Double?,
    val repr: String?,
    val spoken: String?
)

data class Visibility(
    val value: Double?,      // może być null, np. dla "P6SM"
    val repr: String?,       // np. "P6"
    val spoken: String?      // np. "greater than six"
)

data class Clouds(
    val type: String?,       // np. "SCT", "BKN", "FEW"
    val altitude: Int?,      // np. 25 (setki stóp, czyli 2500 ft)
    val direction: String?,  // raczej zawsze null
    val modifier: String?,   // np. "CB" lub null
    val repr: String?        // oryginalny zapis np. "SCT025"
)
