package com.osman.eczanemnerede

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.osman.eczanemnerede.core.DistanceUtility
import com.osman.eczanemnerede.data.CSVDataForLocation

class AdapterforLocation(private val dataList: List<CSVDataForLocation>,

                         private val userLatitude: Double,
                         private val userLongitude: Double,) :
    RecyclerView.Adapter<AdapterforLocation.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerrow_location, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.eczaneAdiTextView.text = data.eczaneAdi


        holder.directionTextView.setOnClickListener {
            val uri =
                Uri.parse("http://maps.google.com/maps?saddr=$userLatitude,$userLongitude&daddr=${data.enlem},${data.boylam}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            it.context.startActivity(intent)
        }
        val distance = DistanceUtility.calculateDistance(
            userLatitude,
            userLongitude,
            data.enlem,
            data.boylam
        )
        val formattedDistance = String.format("%.2f km", distance)
        holder.kmTextView.text = formattedDistance


    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eczaneAdiTextView: TextView = itemView.findViewById(R.id.textView2)
        val kmTextView: TextView = itemView.findViewById(R.id.kmTextView)

        val directionTextView: TextView = itemView.findViewById(R.id.directionTextView)
    }

}