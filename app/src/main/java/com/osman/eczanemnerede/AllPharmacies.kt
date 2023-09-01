package com.osman.eczanemnerede
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AllPharmacies : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupedCSVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_pharmacies)

        val layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerViewAllPharmacies)
        recyclerView.layoutManager = layoutManager

        val dataList = parseCSVData()

        val groupedData = groupPharmaciesByNeighborhood(dataList)

        adapter = GroupedCSVAdapter(groupedData)
        recyclerView.adapter = adapter

        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
    }


    private fun parseCSVData(): List<CSVDataModel> {
        val dataList = mutableListOf<CSVDataModel>() // Create a flat list

        val resources: Resources = this.resources
        val inputStream = resources.openRawResource(R.raw.eczane)

        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        try {
            // Skip the header line
            reader.readLine()

            while (reader.readLine().also { line = it } != null) {
                val columns = line?.split(";")
                if (columns != null && columns.size <= 6) {

                    val eczaneAdi = columns[0].trim()
                    val iletisimNo = columns[1].trim()
                    val enlem = columns[2].trim()
                    val boylam = columns[3].trim()
                    val adres = columns[4].trim()

                    val dataModel = CSVDataModel( eczaneAdi, iletisimNo, enlem, boylam, adres)
                    dataList.add(dataModel) // Add data to the flat list
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return dataList
    }

    private fun groupPharmaciesByNeighborhood(dataList: List<CSVDataModel>): Map<String, List<CSVDataModel>> {
        val groupedPharmacies = mutableMapOf<String, MutableList<CSVDataModel>>()

        for (pharmacy in dataList.sortedBy { it.adres }) {
            val neighborhood = pharmacy.adres.split(" ")[0].trim() // Trim leading/trailing spaces

            if (groupedPharmacies.containsKey(neighborhood)) {

                    groupedPharmacies[neighborhood]?.add(pharmacy)




            } else {
                groupedPharmacies[neighborhood] = mutableListOf(pharmacy)
            }
        }

        return groupedPharmacies
    }

}
