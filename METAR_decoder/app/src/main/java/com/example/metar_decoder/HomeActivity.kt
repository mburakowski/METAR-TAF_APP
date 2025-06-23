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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth

/**
 * Główna aktywność aplikacji – pozwala pobierać dane METAR/TAF dla lotnisk ICAO,
 * zarządzać ulubionymi lotniskami oraz historią wyszukiwań.
 * Obsługuje także powiadomienia systemowe po pobraniu danych.
 */
class HomeActivity : AppCompatActivity() {

    /** Klucz API do AVWX */
    private val apiKey = "KBMXYEsCFCdkxdAbAagPDwVgN1GH-jtDwn01Wjif_5Y"
    /** Klient HTTP do pobierania danych z AVWX */
    private val client = OkHttpClient()
    /** Instancja Firestore (baza danych chmurowa Google) */
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    /** Ostatni pobrany surowy JSON METAR */
    private var lastMetarJson: String? = null
    /** Ostatni pobrany surowy JSON TAF */
    private var lastTafJson: String? = null

    /** Zbiór ulubionych lotnisk ICAO */
    private var favoriteAirports: MutableSet<String> = mutableSetOf()
    /** Ostatni poprawny kod ICAO (do ulubionych) */
    private var lastValidIcao: String? = null

    /**
     * Metoda inicjująca aktywność – ładuje widok, obsługuje UI, pobieranie danych i powiadomienia.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Poproś o uprawnienie do powiadomień (Android 13+)
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
        val addFavoriteButton = findViewById<Button>(R.id.addFavoriteButton)
        val favoritesLayout = findViewById<LinearLayout>(R.id.favoritesLayout)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)
        val decodedTextView = findViewById<TextView>(R.id.decodedTextView)
        val historyButton = findViewById<Button>(R.id.historyButton)
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // Załaduj ulubione lotniska i narysuj na ekranie
        loadFavorites { renderFavorites(favoritesLayout, icaoEditText) }

        // Dodawanie do ulubionych
        addFavoriteButton.isEnabled = false
        addFavoriteButton.setOnClickListener {
            val icao = lastValidIcao ?: return@setOnClickListener
            if (icao.length == 4 && !favoriteAirports.contains(icao)) {
                favoriteAirports.add(icao)
                renderFavorites(favoritesLayout, icaoEditText)
                firestore.collection("favorites").document(icao)
                    .set(mapOf("icao" to icao))
                    .addOnSuccessListener {
                        renderFavorites(favoritesLayout, icaoEditText)
                        Toast.makeText(this, "$icao dodano do ulubionych", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "To lotnisko już jest w ulubionych.", Toast.LENGTH_SHORT).show()
            }
        }

        // Pobierz METAR/TAF
        fetchButton.setOnClickListener {
            val icao = icaoEditText.text.toString().trim().uppercase()
            if (icao.length == 4) {
                resultTextView.text = "Pobieram dane..."
                addFavoriteButton.isEnabled = false
                lastValidIcao = null

                // Zapisz ICAO do historii
                val historyData = hashMapOf("icao" to icao, "timestamp" to System.currentTimeMillis())
                firestore.collection("history").add(historyData)

                fetchMetar(icao) { metarJson ->
                    lastMetarJson = metarJson
                    fetchTaf(icao) { tafJson ->
                        lastTafJson = tafJson
                        runOnUiThread {
                            val metarRaw = extractRaw(metarJson)
                            val tafRaw = extractRaw(tafJson)
                            // Obsługa błędnego ICAO
                            if (metarRaw.isNullOrBlank() && tafRaw.isNullOrBlank()) {
                                resultTextView.text = "Nie odnaleziono lotniska"
                                decodedTextView.text = ""
                                addFavoriteButton.isEnabled = false
                                lastValidIcao = null
                            } else {
                                resultTextView.text = "=== METAR ===\n${metarRaw}\n\n=== TAF ===\n${tafRaw}"
                                try {
                                    val gson = Gson()
                                    val metarDecoded = gson.fromJson(metarJson, MetarResponse::class.java)
                                    val tafDecoded = gson.fromJson(tafJson, TafResponse::class.java)
                                    val metarText = formatMetar(metarDecoded)
                                    val tafText = formatTaf(tafDecoded)
                                    decodedTextView.text =
                                        "=== METAR (zdekodowany) ===\n$metarText\n\n=== TAF (zdekodowany) ===\n$tafText"
                                    addFavoriteButton.isEnabled = true
                                    lastValidIcao = icao
                                } catch (e: Exception) {
                                    decodedTextView.text = "Błąd dekodowania: ${e.message}"
                                    addFavoriteButton.isEnabled = false
                                    lastValidIcao = null
                                }
                            }
                            showNotification(icao)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Podaj poprawny kod ICAO (4 litery)", Toast.LENGTH_SHORT).show()
                addFavoriteButton.isEnabled = false
                lastValidIcao = null
            }
        }

        // Odświeżanie danych
        swipeRefreshLayout.setOnRefreshListener {
            val icao = icaoEditText.text.toString().trim().uppercase()
            if (icao.length == 4) {
                fetchMetar(icao) { metarJson ->
                    lastMetarJson = metarJson
                    fetchTaf(icao) { tafJson ->
                        lastTafJson = tafJson
                        runOnUiThread {
                            val metarRaw = extractRaw(metarJson)
                            val tafRaw = extractRaw(tafJson)
                            if (metarRaw.isNullOrBlank() && tafRaw.isNullOrBlank()) {
                                resultTextView.text = "Nie odnaleziono lotniska"
                                decodedTextView.text = ""
                                addFavoriteButton.isEnabled = false
                                lastValidIcao = null
                            } else {
                                resultTextView.text = "=== METAR ===\n${metarRaw}\n\n=== TAF ===\n${tafRaw}"
                                try {
                                    val gson = Gson()
                                    val metarDecoded = gson.fromJson(metarJson, MetarResponse::class.java)
                                    val tafDecoded = gson.fromJson(tafJson, TafResponse::class.java)
                                    val metarText = formatMetar(metarDecoded)
                                    val tafText = formatTaf(tafDecoded)
                                    decodedTextView.text =
                                        "=== METAR (zdekodowany) ===\n$metarText\n\n=== TAF (zdekodowany) ===\n$tafText"
                                    addFavoriteButton.isEnabled = true
                                    lastValidIcao = icao
                                } catch (e: Exception) {
                                    decodedTextView.text = "Błąd dekodowania: ${e.message}"
                                    addFavoriteButton.isEnabled = false
                                    lastValidIcao = null
                                }
                            }
                            swipeRefreshLayout.isRefreshing = false
                            showNotification(icao)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Podaj poprawny kod ICAO", Toast.LENGTH_SHORT).show()
                addFavoriteButton.isEnabled = false
                lastValidIcao = null
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // Wylogowywanie
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Przejdź do historii wyszukiwań
        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    /**
     * Pobiera listę ulubionych lotnisk z Firestore i wywołuje przekazaną funkcję po załadowaniu.
     */
    private fun loadFavorites(onLoaded: () -> Unit) {
        favoriteAirports.clear()
        firestore.collection("favorites").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val icao = doc.getString("icao")
                    if (icao != null) favoriteAirports.add(icao)
                }
                onLoaded()
            }
    }

    /**
     * Renderuje widok ulubionych lotnisk – każdy wiersz zawiera nazwę ICAO i przycisk "Usuń".
     * Kliknięcie ICAO wpisuje kod w pole wyszukiwania.
     */
    private fun renderFavorites(layout: LinearLayout, icaoEditText: EditText) {
        layout.removeAllViews()
        val sorted = favoriteAirports.toList().sorted()
        if (sorted.isEmpty()) {
            val tv = TextView(this)
            tv.text = "Ulubione lotniska: brak"
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            layout.addView(tv)
        } else {
            val header = TextView(this)
            header.text = "Ulubione lotniska:"
            header.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            layout.addView(header)
            for (icao in sorted) {
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL

                val tv = TextView(this)
                tv.text = icao
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                tv.textSize = 16f
                tv.setPadding(0,0,16,0)
                tv.setOnClickListener {
                    icaoEditText.setText(icao)
                }

                val removeBtn = Button(this)
                removeBtn.text = "Usuń"
                removeBtn.textSize = 12f
                removeBtn.setOnClickListener {
                    favoriteAirports.remove(icao)
                    renderFavorites(layout, icaoEditText)
                    firestore.collection("favorites").document(icao).delete()
                        .addOnSuccessListener { renderFavorites(layout, icaoEditText) }
                }

                row.addView(tv)
                row.addView(removeBtn)
                layout.addView(row)
            }
        }
    }

    /**
     * Pobiera dane METAR z AVWX API dla podanego ICAO.
     * @param icao Kod lotniska ICAO
     * @param callback Funkcja przyjmująca surową odpowiedź JSON
     */
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

    /**
     * Pobiera dane TAF z AVWX API dla podanego ICAO.
     * @param icao Kod lotniska ICAO
     * @param callback Funkcja przyjmująca surową odpowiedź JSON
     */
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

    /**
     * Wyciąga surowe pole "raw" (METAR/TAF) z odpowiedzi JSON.
     * @param json Surowy JSON odpowiedzi
     * @return Surowy tekst raportu METAR/TAF lub pusty String
     */
    private fun extractRaw(json: String?): String {
        return try {
            val obj = Gson().fromJson(json, Map::class.java)
            obj?.get("raw")?.toString() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Wyświetla powiadomienie po pobraniu danych.
     * @param icao Kod ICAO lotniska
     */
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
