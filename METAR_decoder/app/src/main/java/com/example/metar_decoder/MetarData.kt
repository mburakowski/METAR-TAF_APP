package com.example.metar_decoder

/**
 * Główna struktura danych reprezentująca odpowiedź z API METAR.
 *
 * @property station Kod ICAO stacji meteorologicznej.
 * @property time Czas obserwacji METAR.
 * @property temperature Temperatura powietrza w stopniach Celsjusza.
 * @property dewpoint Punkt rosy w stopniach Celsjusza.
 * @property altimeter Wartość ciśnienia atmosferycznego (QNH).
 * @property visibility Widzialność pozioma.
 * @property wind_direction Kierunek wiatru w stopniach.
 * @property wind_speed Prędkość wiatru w węzłach.
 * @property wind_gust Prędkość porywów wiatru w węzłach.
 * @property clouds Lista warstw zachmurzenia.
 * @property flight_rules Warunki lotu (np. VFR, IFR).
 * @property remarks Dodatkowe uwagi i objaśnienia (pole RMK).
 * @property remarks_info Informacje dodatkowe o temperaturze i punkcie rosy z RMK.
 * @property runway_visibility Widzialność na pasach startowych (zazwyczaj pusta lista).
 * @property raw Surowy tekst depeszy METAR.
 * @property wind_variable_direction Lista zmiennych kierunków wiatru (jeśli występują).
 */
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
    val runway_visibility: List<Any>?,
    val raw: String?,
    val wind_variable_direction: List<ValueField>?
)

/**
 * Reprezentuje informacje o czasie, w tym oryginalny zapis i format ISO.
 *
 * @property repr Skrócony format czasu (np. "252153Z").
 * @property dt Format ISO czasu (np. "2019-10-25T21:53:00+00:00Z").
 */
data class Time(
    val repr: String?,
    val dt: String?
)

/**
 * Uniwersalna struktura dla wartości liczbowych z depeszy.
 *
 * @property repr Oryginalna reprezentacja tekstowa (np. "27").
 * @property spoken Tekst do odczytu (np. "two seven").
 * @property value Wartość liczbowa (np. 27.0).
 */
data class ValueField(
    val repr: String?,
    val spoken: String?,
    val value: Double?
)

/**
 * Szczegółowe informacje wyodrębnione z pola RMK (remarks).
 *
 * @property temperature_decimal Temperatura z RMK z dokładnością dziesiętną.
 * @property dewpoint_decimal Punkt rosy z RMK z dokładnością dziesiętną.
 */
data class RemarksInfo(
    val temperature_decimal: ValueField?,
    val dewpoint_decimal: ValueField?
)

/**
 * Reprezentuje warstwę chmur w depeszy METAR.
 *
 * @property type Typ zachmurzenia (np. SCT, BKN, FEW).
 * @property altitude Wysokość podstawy chmur w setkach stóp (np. 25 → 2500 ft).
 * @property modifier Modyfikator zachmurzenia (np. "CB" – cumulonimbus).
 * @property direction Kierunek (w praktyce często null).
 * @property repr Surowa reprezentacja warstwy (np. "SCT025").
 */
data class Cloud(
    val type: String?,
    val altitude: Int?,
    val modifier: String?,
    val direction: String?,
    val repr: String?
)
