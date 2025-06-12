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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val icaoEditText = findViewById<EditText>(R.id.icaoEditText)
        val fetchButton = findViewById<Button>(R.id.fetchButton)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        fetchButton.setOnClickListener {
            val icao = icaoEditText.text.toString().trim().uppercase()
            if (icao.length == 4) {
                resultTextView.text = "Pobieram dane METAR i TAF..."
                fetchMetarAndTaf(icao) { metar, taf ->
                    runOnUiThread {
                        resultTextView.text = buildString {
                            append("=== METAR ===\n")
                            append(metar ?: "Brak danych")
                            append("\n\n=== TAF ===\n")
                            append(taf ?: "Brak danych")
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Podaj poprawny kod ICAO (4 litery)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMetarAndTaf(icao: String, callback: (String?, String?) -> Unit) {
        var metarResult: String? = null
        var tafResult: String? = null
        var responses = 0

        fun checkAndReturn() {
            responses++
            if (responses == 2) {
                callback(metarResult, tafResult)
            }
        }

        // METAR
        val metarUrl = "https://avwx.rest/api/metar/$icao"
        val metarRequest = Request.Builder()
            .url(metarUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(metarRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                metarResult = "Błąd: ${e.localizedMessage}"
                checkAndReturn()
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                    val gson = Gson()
                    val metarObj = gson.fromJson(body, MetarResponse::class.java)
                    metarResult = formatMetar(metarObj)
                } catch (e: Exception) {
                    metarResult = "Nie udało się przetworzyć danych METAR.\n${e.message}"
                }
                checkAndReturn()
            }
        })

        // TAF
        val tafUrl = "https://avwx.rest/api/taf/$icao"
        val tafRequest = Request.Builder()
            .url(tafUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(tafRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                tafResult = "Błąd: ${e.localizedMessage}"
                checkAndReturn()
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                    val gson = Gson()
                    val tafObj = gson.fromJson(body, TafResponse::class.java)
                    tafResult = formatTAF(tafObj)
                } catch (e: Exception) {
                    tafResult = "Nie udało się przetworzyć danych TAF.\n${e.message}"
                }
                checkAndReturn()
            }
        })
    }
}
