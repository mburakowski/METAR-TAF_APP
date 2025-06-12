package com.example.metar_decoder

data class MetarResponse(
    val station: String,
    val time: Time,
    val temperature: ValueField?,
    val dewpoint: ValueField?,
    val remarks: String?,
    val remarks_info: RemarksInfo?, // <- TERAZ MOŻE BYĆ NULL
    val wind_direction: ValueField?,
    val wind_speed: ValueField?,
    val visibility: ValueField?,
    val altimeter: ValueField?,
    val flight_rules: String?,
    val cloudz: List<Cloud>?
)

data class Time(val repr: String?)
data class ValueField(val value: Double?)
data class RemarksInfo(
    val temperature_decimal: ValueField?, // <- też może być null
    val dewpoint_decimal: ValueField?
)
data class Cloud(val type: String?, val altitude: Int?)