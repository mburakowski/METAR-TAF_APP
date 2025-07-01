package com.example.metar_decoder

/**
 * Struktura danych reprezentująca odpowiedź TAF z serwera AVWX.
 *
 * @property station Kod ICAO lotniska.
 * @property time Czas wygenerowania depeszy.
 * @property start_time Czas rozpoczęcia ważności prognozy.
 * @property end_time Czas zakończenia ważności prognozy.
 * @property raw Surowy tekst depeszy TAF.
 * @property forecast Lista prognozowanych zmian pogody.
 * @property remarks Dodatkowe uwagi lub adnotacje.
 */
data class TafResponse(
    val station: String,
    val time: TimeInfo,
    val start_time: TimeInfo,
    val end_time: TimeInfo,
    val raw: String,
    val forecast: List<Forecast>,
    val remarks: String?
)

/**
 * Struktura czasu zawierająca zarówno format ISO jak i skrócony.
 *
 * @property dt Pełna data i godzina w formacie ISO, np. "2019-10-25T21:00:00+00:00Z".
 * @property repr Skrócona forma czasu, np. "2521".
 */
data class TimeInfo(
    val dt: String?,
    val repr: String?
)

/**
 * Prognozowany warunek pogodowy w określonym przedziale czasu.
 *
 * @property type Typ sekcji TAF (np. "FROM", "TEMPO", "BECMG").
 * @property start_time Czas rozpoczęcia obowiązywania prognozy.
 * @property end_time Czas zakończenia obowiązywania prognozy.
 * @property wind_direction Kierunek wiatru w stopniach.
 * @property wind_speed Prędkość wiatru w węzłach.
 * @property visibility Widzialność w milach morskich.
 * @property clouds Lista warstw zachmurzenia.
 * @property other Dodatkowe zjawiska pogodowe (np. "VCSH").
 * @property altimeter Wartość ciśnienia atmosferycznego.
 * @property flight_rules Warunki lotu (np. VFR, MVFR).
 * @property probability Prawdopodobieństwo wystąpienia danego warunku.
 * @property raw Surowy tekst tej części depeszy.
 * @property sanitized Wstępnie przetworzony tekst sekcji prognozy.
 * @property wind_gust Prędkość porywów wiatru.
 * @property wind_shear Informacja o uskoku wiatru (jeśli dostępna).
 * @property icing Informacje o oblodzeniu (jeśli dostępne).
 * @property turbulance Informacje o turbulencjach (jeśli dostępne).
 * @property wind_variable_direction Kierunki zmiennego wiatru (np. [230, 290]).
 */
data class Forecast(
    val type: String?,
    val start_time: TimeInfo,
    val end_time: TimeInfo,
    val wind_direction: WindComponent?,
    val wind_speed: WindComponent?,
    val visibility: Visibility?,
    val clouds: List<Clouds> = emptyList(),
    val other: List<String> = emptyList(),
    val altimeter: String?,
    val flight_rules: String?,
    val probability: Any?,
    val raw: String?,
    val sanitized: String?,
    val wind_gust: WindComponent?,
    val wind_shear: Any?,
    val icing: List<Any>?,
    val turbulance: List<Any>?,
    val wind_variable_direction: List<Int>?
)

/**
 * Ogólna reprezentacja komponentu związanego z wartością liczbową.
 *
 * @property value Wartość liczbowa (np. 70.0).
 * @property repr Surowy tekst (np. "070").
 * @property spoken Forma wypowiadana (np. "zero seven zero").
 */
data class WindComponent(
    val value: Double?,
    val repr: String?,
    val spoken: String?
)

/**
 * Struktura opisująca widzialność.
 *
 * @property value Wartość liczbowa widzialności (w milach morskich), może być null.
 * @property repr Surowa forma (np. "P6").
 * @property spoken Tekst do wypowiedzenia (np. "greater than six").
 */
data class Visibility(
    val value: Double?,
    val repr: String?,
    val spoken: String?
)

/**
 * Warstwa chmur w prognozie TAF.
 *
 * @property type Typ chmur (np. SCT, BKN, FEW).
 * @property altitude Wysokość podstawy chmur w setkach stóp (np. 25 = 2500 ft).
 * @property direction Kierunek, jeśli podany (w praktyce rzadko występuje).
 * @property modifier Modyfikator chmur (np. CB dla cumulonimbus).
 * @property repr Surowa reprezentacja (np. "SCT025").
 */
data class Clouds(
    val type: String?,
    val altitude: Int?,
    val direction: String?,
    val modifier: String?,
    val repr: String?
)

