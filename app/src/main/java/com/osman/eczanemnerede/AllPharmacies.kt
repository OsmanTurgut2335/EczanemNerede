package com.osman.eczanemnerede

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class AllPharmacies : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CSVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_pharmacies)


        recyclerView = findViewById(R.id.recyclerViewAllPharmacies)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val reader = createCSVReader()
        val pharmacies = parseCSVData(reader).sortedBy { it.eczaneAdi }
       // Parse your CSV data into a list of CSVDataModel objects

        adapter = CSVAdapter(pharmacies)
        recyclerView.adapter = adapter
    }

    fun parseCSVData(reader: Reader): List<CSVDataModel> {
        val pharmacies = ArrayList<CSVDataModel>()

        BufferedReader(reader).use { bufferedReader ->
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                //  println("CSV line read: $line")
                val columns = line!!.split(";").map { it.trim() }

                if (columns.size <= 6) {


                    val eczaneAdi = columns[0].trim()
                    val iletisimNo = columns[1].trim()
                    val enlem = columns[2].trim()
                    val boylam = columns[3].trim()
                    val adres = columns[4].trim()

                    val pharmacy = CSVDataModel( eczaneAdi, iletisimNo, enlem, boylam, adres)

                    pharmacies.add(pharmacy)
                }
            }
        }

        return pharmacies
    }

    private fun createCSVReader(): Reader {
        val inputStream: InputStream = resources.openRawResource(R.raw.eczane)
        return InputStreamReader(inputStream, Charsets.UTF_8)
    }

}

