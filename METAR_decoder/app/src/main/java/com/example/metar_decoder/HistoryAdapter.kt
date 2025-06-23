package com.example.metar_decoder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter RecyclerView wyświetlający historię wpisywanych kodów ICAO.
 *
 * Każdy element listy to jeden kod ICAO zapisany przez użytkownika.
 */
class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    /** Lista kodów ICAO do wyświetlenia */
    private var items: List<String> = emptyList()

    /**
     * Aktualizuje dane w adapterze i odświeża widok.
     *
     * @param newItems Nowa lista kodów ICAO do wyświetlenia.
     */
    fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * Przechowuje widoki jednego elementu listy (ViewHolder wzorzec ViewHolder).
     *
     * @param view Widok pojedynczego elementu listy.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /** Pole tekstowe wyświetlające kod ICAO */
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

