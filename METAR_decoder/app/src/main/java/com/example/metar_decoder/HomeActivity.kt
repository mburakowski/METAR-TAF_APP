package com.example.metar_decoder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private val apiKey = "KBMXYEsCFCdkxdAbAagPDwVgN1GH-jtDwn01Wjif_5Y"
    private val client = OkHttpClient()
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var lastMetarJson: String? = null
    private var lastTafJson: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Poproś o zgodę na powiadomienia (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        val icaoEditText = findViewById<EditText>(R.id.icaoEditText)
        val fetchButton = findViewById<Button>(R.id.fetchButton)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)
        val decodeButton = findViewById<Button>(R.id.decodeButton)
        val decodedTextView = findViewById<TextView>(R.id.decodedTextView)
        val historyButton: Button? = try { findViewById(R.id.historyButton) } catch (e: Exception) { null }

        decodeButton.isEnabled = false

        // (Opcjonalnie) przejście do historii
        historyButton?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        fetchButton.setOnClickListener {
            val icao = icaoEditText.text.toString().trim().uppercase()
            if (icao.length == 4) {
                resultTextView.text = "Pobieram dane..."
                decodeButton.isEnabled = false

                // Firestore: Zapisz ICAO do historii
                val historyData = hashMapOf("icao" to icao, "timestamp" to System.currentTimeMillis())
                firestore.collection("history").add(historyData)
                    .addOnFailureListener {
                        // obsługa błędu (opcjonalnie Toast)
                    }

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
                            // Powiadomienie po pobraniu
                            showNotification(icao)
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

    // ---- Powiadomienia ----
    private fun showNotification(icao: String) {
        val channelId = "metar_channel"
        val notifId = 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "METAR Channel"
            val desc = "Powiadomienia o pobraniu METAR"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = desc
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Pobrano dane METAR/TAF")
            .setContentText("Pobrano dane dla lotniska $icao")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {

            if (ContextCompat.checkSelfPermission(
                    this@HomeActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notifId, builder.build())
            }
        }
    }
}
