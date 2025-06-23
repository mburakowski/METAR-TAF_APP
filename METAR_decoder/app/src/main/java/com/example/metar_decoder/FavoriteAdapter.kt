package com.example.metar_decoder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter RecyclerView do wyświetlania listy ulubionych lotnisk ICAO.
 *
 * @constructor Tworzy adapter z pustą listą.
 */
class FavoriteAdapter : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    /** Lista kodów ICAO ulubionych lotnisk. */
    private var items: List<String> = emptyList()

    /**
     * Aktualizuje listę ulubionych lotnisk.
     * @param newItems Nowa lista kodów ICAO.
     */
    fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * ViewHolder klasyczny do wyświetlania pojedynczego kodu ICAO.
     * @property icaoTextView Pole tekstowe z kodem ICAO.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icaoTextView: TextView = view.findViewById(R.id.icaoTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.icaoTextView.text = items[position]
    }
}
