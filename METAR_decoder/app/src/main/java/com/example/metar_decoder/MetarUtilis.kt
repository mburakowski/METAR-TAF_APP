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
            "FEW" -> "Małe zachmurzenie (FEW)"
            "NSC" -> "Brak znaczących chmur (NSC)"
            else -> it.type ?: "Nieznany typ"
        }
        val altitude = it.altitude?.times(100) ?: 0
        "- $type na wysokości ${altitude} ft"
    } ?: "Brak danych o zachmurzeniu"

    val temperature = metar.remarks_info?.temperature_decimal?.value
        ?: metar.temperature?.value
    val dewpoint = metar.remarks_info?.dewpoint_decimal?.value
        ?: metar.dewpoint?.value

    val windDirection = metar.wind_direction?.repr ?: "Brak"
    val windSpeed = metar.wind_speed?.repr ?: "Brak"
    val windGust = metar.wind_gust?.repr?.let { " (porywy: $it kt)" } ?: ""
    val visibility = metar.visibility?.repr?.let { "$it" } ?: "Brak"
    val pressure = metar.altimeter?.repr?.let { "$it hPa" } ?: "Brak"
    val remarks = metar.remarks ?: "Brak"
    val flightRules = metar.flight_rules ?: "Brak"
    val timeFormatted = metar.time?.repr?.let { parseMetarTime(it) } ?: "Brak"

    return buildString {
        appendLine("Lotnisko: ${metar.station ?: "Nieznane"}")
        appendLine("Czas obserwacji: $timeFormatted")
        appendLine("Temperatura: ${temperature?.let { "$it°C" } ?: "Brak"} (punkt rosy: ${dewpoint?.let { "$it°C" } ?: "Brak"})")
        appendLine("Wiatr: $windDirection° z prędkością $windSpeed kt$windGust")
        appendLine("Widzialność: $visibility")
        appendLine("Ciśnienie (QNH): $pressure")
        appendLine("Zachmurzenie:")
        appendLine(cloudsDescription)
        appendLine("Warunki lotu: $flightRules")
        appendLine("Uwagi: $remarks")
    }.trim()
}
