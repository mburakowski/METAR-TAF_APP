package com.example.metar_decoder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Aktywność wyświetlająca historię wyszukiwań oraz ulubione lotniska ICAO użytkownika.
 *
 * Pobiera dane z Firestore i prezentuje je w dwóch listach RecyclerView.
 */
class HistoryActivity : AppCompatActivity() {

    /** RecyclerView do historii wyszukiwań */
    private lateinit var historyRecyclerView: RecyclerView

    /** RecyclerView do ulubionych lotnisk */
    private lateinit var favoritesRecyclerView: RecyclerView

    /** Adapter do historii */
    private lateinit var historyAdapter: HistoryAdapter

    /** Adapter do ulubionych */
    private lateinit var favoriteAdapter: FavoriteAdapter

    /**
     * Inicjalizacja widoków i pobranie danych z Firestore po uruchomieniu aktywności.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Historia
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyAdapter = HistoryAdapter()
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Ulubione
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        favoriteAdapter = FavoriteAdapter()
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoriteAdapter

        // Pobierz historię wyszukiwań z Firestore
        FirebaseFirestore.getInstance().collection("history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { it.getString("icao") }
                historyAdapter.updateData(items)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd historii: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

        // Pobierz ulubione lotniska z Firestore
        FirebaseFirestore.getInstance().collection("favorites")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { it.getString("icao") }
                favoriteAdapter.updateData(items)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd ulubionych: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
