package com.example.metar_decoder

//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//
//class HomeActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//    }
//}

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
                        resultTextView.text =
                            "=== METAR ===\n${metar ?: "Brak danych"}\n\n=== TAF ===\n${taf ?: "Brak danych"}"
                    }
                }
            } else {
                Toast.makeText(this, "Podaj poprawny kod ICAO (4 litery)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMetarAndTaf(icao: String, callback: (String?, String?) -> Unit) {
        var metarResponse: String? = null
        var tafResponse: String? = null

        // Licznik odpowiedzi, bo chcemy poczekać na oba zapytania
        var responses = 0

        fun checkAndCallback() {
            responses++
            if (responses == 2) {
                callback(metarResponse, tafResponse)
            }
        }

        // Pobierz METAR
        val metarUrl = "https://avwx.rest/api/metar/$icao"
        val metarRequest = Request.Builder()
            .url(metarUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()
        client.newCall(metarRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                metarResponse = "Błąd: ${e.localizedMessage}"
                checkAndCallback()
            }
            override fun onResponse(call: Call, response: Response) {
                metarResponse = response.body?.string()
                checkAndCallback()
            }
        })

        // Pobierz TAF
        val tafUrl = "https://avwx.rest/api/taf/$icao"
        val tafRequest = Request.Builder()
            .url(tafUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()
        client.newCall(tafRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                tafResponse = "Błąd: ${e.localizedMessage}"
                checkAndCallback()
            }
            override fun onResponse(call: Call, response: Response) {
                tafResponse = response.body?.string()
                checkAndCallback()
            }
        })
    }
}