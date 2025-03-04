package com.osman.eczanemnerede

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.osman.eczanemnerede.data.CSVDataModel

class GroupedCSVAdapter(private val groupedData: Map<String, List<CSVDataModel>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val headersAndItems: List<Any> = groupedData.entries.flatMap { (neighborhood, pharmacies) ->
        listOf(neighborhood) + pharmacies
    }

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            val headerView = inflater.inflate(R.layout.header_layout, parent, false)
            HeaderViewHolder(headerView)
        } else {
            val itemView = inflater.inflate(R.layout.item_layout, parent, false)
            ItemViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bindHeader(headersAndItems[position] as String)
        } else if (holder is ItemViewHolder) {
            val data = headersAndItems[position] as CSVDataModel
            holder.bindItem(data)
        }
    }

    override fun getItemCount(): Int = headersAndItems.size

    override fun getItemViewType(position: Int): Int {
        return if (headersAndItems[position] is String) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)

        fun bindHeader(neighborhood: String) {
            val headerText = "$neighborhood Mahallesi Eczaneleri"
            headerTextView.text = headerText
        }
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eczaneAdiTextView: TextView = itemView.findViewById(R.id.eczaneAdiTextView)

        fun bindItem(data: CSVDataModel) {
            eczaneAdiTextView.text = data.eczaneAdi
        }
    }
}
