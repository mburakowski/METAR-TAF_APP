package com.example.metar_decoder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Reprezentuje wpis w historii wyszukiwania lotniska.
 *
 * @property icao Kod ICAO lotniska.
 * @property airportName Opcjonalna nazwa lotniska.
 * @property country Państwo, w którym znajduje się lotnisko.
 * @property region Region lub województwo lotniska.
 * @property latitude Szerokość geograficzna lotniska.
 * @property longitude Długość geograficzna lotniska.
 */
data class HistoryEntry(
    val icao: String,
    val airportName: String?,
    val country: String,
    val region: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Adapter RecyclerView wyświetlający historię wyszukiwania lotnisk
 * w postaci listy wpisów {@link HistoryEntry}.
 */
class HistoryAdapter(
    private val onItemClick: ((HistoryEntry) -> Unit)? = null
) : ListAdapter<HistoryEntry, HistoryAdapter.ViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("RV-DEBUG", "onCreateViewHolder()")        // ← log tworzenia ViewHoldera
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("RV-DEBUG", "onBindViewHolder($position)")  // ← log wiązania danych
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        Log.d("RV-DEBUG", "onViewRecycled()")             // ← log recyklingu
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textIcaoName      = view.findViewById<TextView>(R.id.textIcaoName)
        private val textCountryRegion = view.findViewById<TextView>(R.id.textCountryRegion)
        private val textCoordinates   = view.findViewById<TextView>(R.id.textCoordinates)

        init {
            Log.d("RV-DEBUG", "ViewHolder.init()")       // ← log utworzenia instancji
        }

        fun bind(entry: HistoryEntry) {
            textIcaoName.text      = "${entry.icao} – ${entry.airportName.orEmpty()}"
            textCountryRegion.text = "${entry.country} ∙ ${entry.region}"
            textCoordinates.text   = String.format("%.4f, %.4f", entry.latitude, entry.longitude)
            itemView.setOnClickListener { onItemClick?.invoke(entry) }
        }
    }
}


/**
 * Callback wykorzystywany przez ListAdapter do porównywania elementów.
 */
class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryEntry>() {
    /**
     * Sprawdza, czy dwa obiekty reprezentują ten sam wpis (na podstawie kodu ICAO).
     */
    override fun areItemsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry): Boolean =
        oldItem.icao == newItem.icao

    /**
     * Sprawdza, czy zawartość dwóch obiektów jest identyczna.
     */
    override fun areContentsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry): Boolean =
        oldItem == newItem
}
