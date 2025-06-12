package com.example.metar_decoder

data class TafResponse(
    val station: String?,
    val time: Time?,
    val forecast: List<TafForecast>?
)

data class TafForecast(
    val start_time: Time?,
    val end_time: Time?,
    val wind_direction: ValueField?,
    val wind_speed: ValueField?,
    val visibility: ValueField?,
    val clouds: List<Cloud>?,
    val summary: String?,
    val change_indicator: String? // np. "BECMG", "TEMPO"
)
