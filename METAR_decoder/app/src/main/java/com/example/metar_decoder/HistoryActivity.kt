package com.example.metar_decoder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var favoriteAdapter: FavoriteAdapter

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

        // Pobierz historię
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

        // Pobierz ulubione
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
