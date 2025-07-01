import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

val client = OkHttpClient()

/**
 * Klucz API do autoryzacji zapytań w usłudze AVWX.
 */
val apiKey = "KBMXYEsCFCdkxdAbAagPDwVgN1GH-jtDwn01Wjif_5Y"

/**
 * Główna funkcja programu. Prosi użytkownika o kod ICAO i pobiera dane METAR oraz TAF
 * z serwisu AVWX. Następnie wypisuje surową odpowiedź JSON na konsolę.
 */
fun main() {
    println("=== METAR Downloader (AVWX API) ===")
    print("Podaj kod ICAO lotniska (np. EPWA): ")
    val stationICAO = readln().trim().uppercase()

    getMetarData(stationICAO)
    getTafData(stationICAO)

    println("Oczekiwanie na odpowiedź...")
    Thread.sleep(3000)
}

/**
 * Wysyła asynchroniczne zapytanie HTTP GET do API AVWX, aby pobrać dane METAR
 * dla danego kodu ICAO lotniska. Odpowiedź (surowy JSON) wypisywana jest na konsolę.
 *
 * @param stationICAO Czteroliterowy kod ICAO lotniska (np. EPWA).
 */
fun getMetarData(stationICAO: String) {
    val url = "https://avwx.rest/api/metar/$stationICAO"

    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { responseBody ->
                println("\n=== Odpowiedź z serwera ===")
                println(responseBody)
            }
        }
    })
}

/**
 * Wysyła asynchroniczne zapytanie HTTP GET do API AVWX, aby pobrać dane TAF
 * dla danego kodu ICAO lotniska. Odpowiedź (surowy JSON) wypisywana jest na konsolę.
 *
 * @param stationICAO Czteroliterowy kod ICAO lotniska (np. EPWA).
 */
fun getTafData(stationICAO: String) {
    val url = "https://avwx.rest/api/taf/$stationICAO"

    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { responseBody ->
                println("\n=== Odpowiedź z serwera ===")
                println(responseBody)
            }
        }
    })
}
