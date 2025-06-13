package com.example.metar_decoder

data class MetarResponse(
    val station: String?,
    val time: Time?,
    val temperature: ValueField?,
    val dewpoint: ValueField?,
    val altimeter: ValueField?,
    val visibility: ValueField?,
    val wind_direction: ValueField?,
    val wind_speed: ValueField?,
    val wind_gust: ValueField?,
    val clouds: List<Cloud>?,
    val flight_rules: String?,
    val remarks: String?,
    val remarks_info: RemarksInfo?,
    val runway_visibility: List<Any>?, // jeśli chcesz to potem rozszerzyć – zmień typ
    val raw: String?
)

data class Time(
    val repr: String?,
    val dt: String? // np. "2019-10-25T21:53:00+00:00Z"
)

data class ValueField(
    val repr: String?,      // np. "27"
    val spoken: String?,    // np. "two seven"
    val value: Double?      // np. 27.0
)

data class RemarksInfo(
    val temperature_decimal: ValueField?,
    val dewpoint_decimal: ValueField?
)

data class Cloud(
    val type: String?,       // np. SCT, BKN
    val altitude: Int?,      // np. 25
    val modifier: String?,   // np. "+"
    val direction: String?,  // (zawsze null w przykładzie, ale może być string)
    val repr: String?        // np. "SCT025"
)
