package com.example.metar_decoder

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
fun formatTaf(taf: TafResponse): String {
    if (taf.forecast.isNullOrEmpty()) return "Brak szczegółowej prognozy TAF"

    val builder = StringBuilder()
    taf.forecast.forEachIndexed { i, fc ->
        val start = fc.start_time?.repr ?: "?"
        val end = fc.end_time?.repr ?: "?"
        val windDir = fc.wind_direction?.value?.toInt()?.toString() ?: "-"
        val windSpd = fc.wind_speed?.value?.toInt()?.toString() ?: "-"
        val vis = fc.visibility?.value?.toInt()?.toString() ?: "-"
        val clouds = fc.clouds?.joinToString { it.type ?: "chmury" } ?: "-"
        val summary = fc.summary ?: ""

        builder.append("Prognoza #${i + 1} [$start-$end]:\n")
        builder.append("  Zmiana: ${fc.change_indicator ?: "stała"}\n")
        builder.append("  Wiatr: $windDir° $windSpd kt\n")
        builder.append("  Widzialność: $vis m\n")
        builder.append("  Chmury: $clouds\n")
        if (summary.isNotBlank()) builder.append("  Opis: $summary\n")
        builder.append("\n")
    }
    return builder.toString()
}
