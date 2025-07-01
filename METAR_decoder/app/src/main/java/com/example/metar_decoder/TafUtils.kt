package com.example.metar_decoder

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
/**
 * Formatter do daty i czasu w formacie "dd.MM.yy, HH:mm" w języku angielskim.
 */
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy, HH:mm", Locale.ENGLISH)

@RequiresApi(Build.VERSION_CODES.O)
/**
 * Skrócony formatter do czasu w formacie "HH:mm (dd.MM)" w języku angielskim.
 */
private val shortFormatter = DateTimeFormatter.ofPattern("HH:mm (dd.MM)", Locale.ENGLISH)

/**
 * Formatuje kierunek w stopniach na tekstową reprezentację wraz z nazwą kierunku kardynalnego.
 *
 * @param degrees kierunek w stopniach (0.0–360.0), lub null jeśli brak danych
 * @return sformatowany kierunek np. "45.0° (NE)" lub "brak danych" gdy wartość null
 */
private fun formatDirection(degrees: Double?): String {
    if (degrees == null) return "brak danych"
    val dirs = listOf(
        "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
    )
    val index = ((degrees + 11.25) / 22.5).toInt() % 16
    return "${degrees}° (${dirs[index]})"
}

/**
 * Formatuje listę warstw chmur do czytelnego łańcucha tekstowego.
 *
 * @param clouds lista obiektów typu Clouds lub null/ pusta lista
 * @return sformatowany łańcuch z opisem warstw chmur lub "Brak danych"
 */
private fun formatClouds(clouds: List<Clouds>?): String {
    if (clouds.isNullOrEmpty()) return "Brak danych"
    return clouds.joinToString(", ") { cloud ->
        val height = cloud.altitude?.times(100) ?: "?"
        "${cloud.type}${cloud.modifier ?: ""} ${height} ft"
    }
}

/**
 * Tłumaczy i formatuje listę skrótów zjawisk pogodowych na polskie opisy.
 *
 * @param other lista kodów zjawisk pogodowych lub null/pusta lista
 * @return łańcuch z tłumaczeniem zjawisk lub "Brak zjawisk"
 */
private fun formatOther(other: List<String>?): String {
    if (other.isNullOrEmpty()) return "Brak zjawisk"

    val translations = mapOf(
        "RA" to "deszcz",
        "SHRA" to "przelotny deszcz",
        "VCSH" to "przelotny deszcz w pobliżu",
        "TSRA" to "burza z deszczem",
        "TS" to "burza",
        "SN" to "śnieg",
        "BR" to "zamglenie",
        "FG" to "mgła",
        "HZ" to "zamglenie pyłowe",
        "DZ" to "mżawka"
    )

    return other.joinToString(", ") { code ->
        translations[code] ?: code
    }
}

@RequiresApi(Build.VERSION_CODES.O)
        /**
         * Formatuje obiekt TafResponse do czytelnej, wielolinijkowej depeszy tekstowej.
         *
         * @param tafResponse obiekt zawierający dane TAF
         * @return sformatowany raport tekstowy z prognozą pogody lotniczej
         */
fun formatTaf(tafResponse: TafResponse): String {
    val sb = StringBuilder()

    val dataTAF = tafResponse.time.dt?.let { ZonedDateTime.parse(it) }
    dataTAF?.let {
        sb.appendLine("Depesza TAF wygenerowana: ${dateTimeFormatter.format(it)}")
        sb.appendLine()
    }

    for (forecast in tafResponse.forecast) {
        val start = forecast.start_time.dt?.let { ZonedDateTime.parse(it) }
        val end = forecast.end_time.dt?.let { ZonedDateTime.parse(it) }

        val header = when (forecast.type) {
            "FROM" -> "Prognoza od ${start?.let { shortFormatter.format(it) }} do ${end?.let { shortFormatter.format(it) }}"
            "TEMPO" -> "Czasowe zmiany ${start?.let { shortFormatter.format(it) }}–${end?.let { shortFormatter.format(it) }}"
            "BECMG" -> "Zmiana warunków ${start?.let { shortFormatter.format(it) }}–${end?.let { shortFormatter.format(it) }}"
            else -> "Zmiana od ${start?.let { shortFormatter.format(it) }} do ${end?.let { shortFormatter.format(it) }}"
        }

        sb.appendLine(header)

        if (forecast.wind_direction?.value != null && forecast.wind_speed?.value != null) {
            sb.append("Wiatr: ${formatDirection(forecast.wind_direction.value)} ${forecast.wind_speed.value} kt")
        } else {
            sb.append("Wiatr: brak danych")
        }

        forecast.wind_gust?.value?.let {
            sb.append(" (porywy: $it kt)")
        }
        sb.appendLine()

        if (!forecast.wind_variable_direction.isNullOrEmpty()) {
            val variable = forecast.wind_variable_direction
            if (variable.size == 2) {
                sb.appendLine("Zmienny kierunek: od ${variable[0]}° do ${variable[1]}°")
            } else {
                sb.appendLine("Zmienny kierunek: ${variable.joinToString(", ")}°")
            }
        }

        val visibility = forecast.visibility?.repr ?: "brak danych"
        sb.appendLine("Widzialność: $visibility")

        sb.appendLine("Zachmurzenie: ${formatClouds(forecast.clouds)}")

        sb.appendLine("Zjawiska: ${formatOther(forecast.other)}")

        sb.appendLine()
    }

    return sb.toString().trim()
}

