package com.example.metar_decoder

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun parseMetarTime(code: String): String {
    if (!Regex("\\d{6}Z").matches(code)) return "Nieznana data"

    val day = code.substring(0, 2).toIntOrNull() ?: return "Nieznana data"
    val hour = code.substring(2, 4).toIntOrNull() ?: return "Nieznana data"
    val minute = code.substring(4, 6).toIntOrNull() ?: return "Nieznana data"

    // Użyj bieżącego miesiąca i roku
    val now = LocalDate.now(ZoneOffset.UTC)
    val dateTime = LocalDateTime.of(now.year, now.month, day, hour, minute)

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")
    return dateTime.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatMetar(metar: MetarResponse): String {
    val cloudsDescription = metar.clouds?.joinToString("\n") {
        val type = when (it.type) {
            "SCT" -> "Rozproszone chmury (SCT)"
            "BKN" -> "Znaczne zachmurzenie (BKN)"
            "OVC" -> "Całkowite zachmurzenie (OVC)"
            else -> it.type ?: "Nieznany typ"
        }
        val altitude = it.altitude?.times(100) ?: 0
        "- $type na wysokości ${altitude} ft"
    } ?: "Brak danych o zachmurzeniu"

    val temperature = metar.remarks_info?.temperature_decimal?.value
        ?: metar.temperature?.value
        ?: 0.0

    val dewpoint = metar.remarks_info?.dewpoint_decimal?.value
        ?: metar.dewpoint?.value
        ?: 0.0

    val windDirection = metar.wind_direction?.value?.toInt()?.toString() ?: "Brak"
    val windSpeed = metar.wind_speed?.value?.toInt()?.toString() ?: "Brak"
    val visibility = metar.visibility?.value?.toInt()?.toString() ?: "Brak"
    val pressure = metar.altimeter?.value?.toString() ?: "Brak"
    val remarks = metar.remarks ?: "Brak"
    val flightRules = metar.flight_rules ?: "Brak"
    val timeFormatted = metar.time.repr?.let { parseMetarTime(it) } ?: "Brak"

    return """
        Lotnisko: ${metar.station}
        Czas obserwacji: $timeFormatted
        Temperatura: ${temperature}°C (punkt rosy: ${dewpoint}°C)
        Wiatr: $windDirection° z prędkością $windSpeed kt
        Widzialność: $visibility NM
        Ciśnienie (QNH): $pressure hPa
        Zachmurzenie:
        $cloudsDescription
        Warunki lotu: $flightRules
        Uwagi: $remarks
    """.trimIndent()
}
