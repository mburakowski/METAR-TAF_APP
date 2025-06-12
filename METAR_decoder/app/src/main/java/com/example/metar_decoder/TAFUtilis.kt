package com.example.metar_decoder

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy, HH:mm", Locale.ENGLISH)
    @RequiresApi(Build.VERSION_CODES.O)
    private val shortFormatter = DateTimeFormatter.ofPattern("HH:mm (dd.MM)", Locale.ENGLISH)

    private fun formatDirection(degrees: Double?): String {
        if (degrees == null) return "brak danych"
        val dirs = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((degrees + 11.25) / 22.5).toInt() % 16
        return "${degrees}° (${dirs[index]})"
    }

    private fun formatClouds(clouds: List<Clouds>?): String {
        if (clouds.isNullOrEmpty()) return "Brak zachmurzenia"
        return clouds.joinToString(", ") { cloud ->
            val height = cloud.altitude?.times(100) ?: "?"
            "${cloud.type}${cloud.modifier ?: ""} ${height} ft"
        }
    }

    private fun formatOther(other: List<String>?): String {
        return if (other.isNullOrEmpty()) "Brak zjawisk" else other.joinToString(", ")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTAF(tafResponse: TafResponse): String {
        val sb = StringBuilder()

        val dataTAF = ZonedDateTime.parse(tafResponse.time.dt)
        sb.append("${dateTimeFormatter.format(dataTAF)}\n\n")

        for (forecast in tafResponse.forecast) {
            val start = ZonedDateTime.parse(forecast.start_time.dt)
            val end = ZonedDateTime.parse(forecast.end_time.dt)

            val naglowek = when (forecast.type) {
                "FROM" -> "Prognoza od ${shortFormatter.format(start)} do ${shortFormatter.format(end)}"
                "TEMPO" -> "Czasowe zmiany ${shortFormatter.format(start)}–${shortFormatter.format(end)}"
                "BECMG" -> "Zmiana warunków ${shortFormatter.format(start)}–${shortFormatter.format(end)}"
                else -> "Zmiana od ${shortFormatter.format(start)} do ${shortFormatter.format(end)}"
            }

            sb.append(naglowek).append("\n")

            // Wiatr
            if (forecast.wind_direction?.value != null && forecast.wind_speed?.value != null) {
                sb.append("Wiatr: ${formatDirection(forecast.wind_direction.value)} ${forecast.wind_speed.value} kt\n")
            } else {
                sb.append("Wiatr: brak danych\n")
            }

            // Widzialność
            sb.append("Widzialność: ${forecast.visibility?.repr ?: "brak danych"}\n")

            // Zachmurzenie
            sb.append("Zachmurzenie: ${formatClouds(forecast.clouds)}\n")

            // Inne zjawiska
            sb.append("Zjawiska: ${formatOther(forecast.other)}\n")

            sb.append("\n")
        }

        return sb.toString().trim()
    }


