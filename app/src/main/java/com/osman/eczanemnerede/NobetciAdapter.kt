package com.osman.eczanemnerede

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


// Create an adapter class
class NobetciAdapter(private val pharmacyNames: List<String>) : RecyclerView.Adapter<NobetciAdapter.PharmacyViewHolder>() {

    inner class PharmacyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pharmacyNameTextView: TextView = itemView.findViewById(R.id.pharmacyNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PharmacyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerrow_nobetci, parent, false)
        return PharmacyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PharmacyViewHolder, position: Int) {
        val pharmacyName = pharmacyNames[position]
        holder.pharmacyNameTextView.text = pharmacyName
    }

    override fun getItemCount(): Int {
        return pharmacyNames.size
    }
}
