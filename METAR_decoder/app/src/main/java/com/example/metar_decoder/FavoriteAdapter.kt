package com.example.metar_decoder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

/**
 * Adapter RecyclerView do wyświetlania ulubionych lotnisk.
 * Oczekuje listy obiektów [HistoryEntry], aby pokazać kod ICAO, nazwę,
 * państwo, region i współrzędne w layoucie item_history.xml.
 */
class FavoriteAdapter(
    private val onItemClick: ((HistoryEntry) -> Unit)? = null
) : ListAdapter<HistoryEntry, FavoriteAdapter.ViewHolder>(FavoriteDiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textIcaoName      = view.findViewById<TextView>(R.id.textIcaoName)
        private val textCountryRegion = view.findViewById<TextView>(R.id.textCountryRegion)
        private val textCoordinates   = view.findViewById<TextView>(R.id.textCoordinates)

        fun bind(entry: HistoryEntry) {
            textIcaoName.text      = "${entry.icao} – ${entry.airportName.orEmpty()}"
            textCountryRegion.text = "${entry.country} ∙ ${entry.region}"
            textCoordinates.text   = String.format("%.4f, %.4f", entry.latitude, entry.longitude)
            itemView.setOnClickListener { onItemClick?.invoke(entry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * DiffUtil do FavoriteAdapter – porównuje dwa [HistoryEntry].
 */
class FavoriteDiffCallback : DiffUtil.ItemCallback<HistoryEntry>() {
    override fun areItemsTheSame(old: HistoryEntry, new: HistoryEntry) = old.icao == new.icao
    override fun areContentsTheSame(old: HistoryEntry, new: HistoryEntry) = old == new
}