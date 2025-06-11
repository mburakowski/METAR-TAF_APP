import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

val client = OkHttpClient()
val apiKey = "KBMXYEsCFCdkxdAbAagPDwVgN1GH-jtDwn01Wjif_5Y"

fun main() {
    println("=== METAR Downloader (AVWX API) ===")
    print("Podaj kod ICAO lotniska (np. EPWA): ")
    val stationICAO = readln().trim().uppercase()

    getMetarData(stationICAO)
    getTafData(stationICAO)

    println("Oczekiwanie na odpowiedź...")
    Thread.sleep(3000)
}

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
