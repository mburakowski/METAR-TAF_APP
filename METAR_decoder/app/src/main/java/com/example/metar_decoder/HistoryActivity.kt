package com.example.metar_decoder

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Aktywność wyświetlająca historię wyszukiwań lotnisk.
 * Dane pobierane są z kolekcji Firestore: "history".
 * Jeśli dokument zawiera tylko ICAO, pozostałe dane (kraj, region, współrzędne)
 * pobierane są z lokalnej bazy AirportDatabase.
 */
class HistoryActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyEntries = mutableListOf<HistoryEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Inicjalizacja RecyclerView
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyAdapter = HistoryAdapter { entry ->
            Toast.makeText(this, "Wybrano: ${entry.icao}", Toast.LENGTH_SHORT).show()
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Załaduj bazę lotnisk
        AirportDatabase.load(applicationContext)

        // Pobranie historii z Firestore
        fetchHistory()

        // Swipe to delete dla historii
        setupSwipeToDelete(
            recyclerView = historyRecyclerView,
            entries = historyEntries,
            adapter = historyAdapter,
            collectionName = "history"
        )
    }

    /**
     * Pobiera listę wpisów historii z Firestore i aktualizuje adapter.
     * Uzupełnia brakujące dane z AirportDatabase.
     */
    private fun fetchHistory() {
        db.collection("history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                historyEntries.clear()
                for (doc in result.documents) {
                    val icao = doc.getString("icao") ?: continue
                    val airportInfo = AirportDatabase.getAirport(icao)
                    val name = airportInfo?.name.orEmpty()
                    //val airportInfo = AirportDatabase.getAirport(icao)
                    val country = doc.getString("country") ?: airportInfo?.country.orEmpty()
                    val region = doc.getString("region") ?: airportInfo?.region.orEmpty()
                    val lat = doc.getDouble("lat") ?: airportInfo?.latitude ?: 0.0
                    val lon = doc.getDouble("lon") ?: airportInfo?.longitude ?: 0.0
                    historyEntries.add(HistoryEntry(icao, name, country, region, lat, lon))
                }
                historyAdapter.submitList(historyEntries.toList())
                if (historyEntries.isEmpty()) {
                    Toast.makeText(this, "Brak historii wyszukiwań.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("HistoryActivity", "Błąd pobierania historii", e)
                Toast.makeText(this, "Błąd historii: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Konfiguruje gest "swipe to delete" dla podanego RecyclerView.
     */
    private fun setupSwipeToDelete(
        recyclerView: RecyclerView,
        entries: MutableList<HistoryEntry>,
        adapter: ListAdapter<HistoryEntry, out RecyclerView.ViewHolder>,
        collectionName: String
    ) {
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.adapterPosition
                val removed = entries.removeAt(pos)
                adapter.submitList(entries.toList())

                db.collection(collectionName)
                    .whereEqualTo("icao", removed.icao)
                    .get()
                    .addOnSuccessListener { snaps -> snaps.forEach { it.reference.delete() } }
            }
        })
        touchHelper.attachToRecyclerView(recyclerView)
    }
}
