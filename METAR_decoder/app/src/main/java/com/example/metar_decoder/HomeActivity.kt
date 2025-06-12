package com.example.metar_decoder

import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private val apiKey = "KBMXYEsCFCdkxdAbAagPDwVgN1GH-jtDwn01Wjif_5Y"
    private val client = OkHttpClient()

    private var lastMetarJson: String? = null
    private var lastTafJson: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val icaoEditText = findViewById<EditText>(R.id.icaoEditText)
        val fetchButton = findViewById<Button>(R.id.fetchButton)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)
        val decodeButton = findViewById<Button>(R.id.decodeButton)
        val decodedTextView = findViewById<TextView>(R.id.decodedTextView)

        decodeButton.isEnabled = false

        fetchButton.setOnClickListener {
            val icao = icaoEditText.text.toString().trim().uppercase()
            if (icao.length == 4) {
                resultTextView.text = "Pobieram dane..."
                decodeButton.isEnabled = false

                // Pobierz METAR
                fetchMetar(icao) { metarJson ->
                    lastMetarJson = metarJson
                    // Pobierz TAF po METAR
                    fetchTaf(icao) { tafJson ->
                        lastTafJson = tafJson
                        runOnUiThread {
                            val metarRaw = extractRaw(metarJson)
                            val tafRaw = extractRaw(tafJson)
                            resultTextView.text =
                                "=== METAR ===\n${metarRaw}\n\n=== TAF ===\n${tafRaw}"
                            decodeButton.isEnabled = true
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Podaj poprawny kod ICAO (4 litery)", Toast.LENGTH_SHORT).show()
            }
        }

        decodeButton.setOnClickListener {
            try {
                val gson = Gson()
                val metarDecoded = lastMetarJson?.let { gson.fromJson(it, MetarResponse::class.java) }
                val tafDecoded = lastTafJson?.let { gson.fromJson(it, TafResponse::class.java) }

                val metarText = metarDecoded?.let { formatMetar(it) } ?: "Brak zdekodowanego METAR"
                val tafText = tafDecoded?.let { formatTaf(it) } ?: "Brak zdekodowanego TAF"

                decodedTextView.text = "=== METAR ===\n$metarText\n\n=== TAF ===\n$tafText"
            } catch (e: Exception) {
                decodedTextView.text = "Błąd dekodowania: ${e.message}"
            }
        }
    }

    private fun fetchMetar(icao: String, callback: (String) -> Unit) {
        val url = "https://avwx.rest/api/metar/$icao"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Błąd: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                callback(body ?: "Brak danych z serwera")
            }
        })
    }

    private fun fetchTaf(icao: String, callback: (String) -> Unit) {
        val url = "https://avwx.rest/api/taf/$icao"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Błąd: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                callback(body ?: "Brak danych z serwera")
            }
        })
    }

    // Funkcja do wyciągania pola "raw" z JSON-a (surowy tekst raportu)
    private fun extractRaw(json: String?): String {
        return try {
            val obj = Gson().fromJson(json, Map::class.java)
            obj?.get("raw")?.toString() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
