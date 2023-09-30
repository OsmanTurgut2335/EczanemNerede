package com.osman.eczanemnerede

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader


var latitude : Double = 0.0
var longitude : Double = 0.0

val nearbyPharmacies = ArrayList<CSVDataForLocation>()
val dummyList = ArrayList<CSVDataForLocation>()

class LocationBasedPharmacies : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_based_pharmacies)
        val inputStream: InputStream = resources.openRawResource(R.raw.eczane)


        latitude =intent.getDoubleExtra("latitude",0.0)
        longitude = intent.getDoubleExtra("longitude",0.0)




        //  val inputStream: InputStream = resources.openRawResource(R.raw.eczane)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val pharmacies = parseCSVData(reader).sortedBy { it.distance }

        //val reader = createCSVReader()
        //val pharmacies = parseCSVData(reader).sortedBy { it.distance }


        println(pharmacies)
        for (pharmacy in pharmacies) {

            val distance =
                calculateDistance(latitude, longitude, pharmacy.enlem, pharmacy.boylam)


            if (distance <= 5.0) { // Check if the distance is within 5 kilometers
                if (distance == 0.0) {
                    continue
                }
                nearbyPharmacies.add(pharmacy)

            }

            val recyclerView: RecyclerView = findViewById(R.id.locationBasedRecycler)
            val adapter = AdapterforLocation(nearbyPharmacies, latitude, longitude)

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this@LocationBasedPharmacies)
        }







    }
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val distance = earthRadius * c // Distance in kilometers



        return distance
    }

    fun parseCSVData(reader: Reader): List<CSVDataForLocation> {
        val pharmacies = ArrayList<CSVDataForLocation>()

        BufferedReader(reader).use { bufferedReader ->
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                //  println("CSV line read: $line")
                val columns = line!!.split(";").map { it.trim() }

                if (columns.size <= 6) {


                    val eczaneAdi = columns[0].trim()
                    val iletisimNo = columns[1].trim()
                    val enlem = columns[2].trim().toDoubleOrNull() ?: 0.0
                    val boylam = columns[3].trim().toDoubleOrNull() ?: 0.0
                    val adres = columns[4].trim()

                    val pharmacy = CSVDataForLocation( eczaneAdi, iletisimNo, enlem, boylam, adres,calculateDistance(
                        latitude, longitude,enlem,boylam))

                    pharmacies.add(pharmacy)
                }
            }
        }

        return pharmacies
    }

    fun directionTextViewClicked(){
        if (nearbyPharmacies.isNotEmpty()) {
            val selectedPharmacy = nearbyPharmacies[0] // Replace with your logic to select a pharmacy
            val uri = Uri.parse("http://maps.google.com/maps?saddr=$latitude,$longitude&daddr=${selectedPharmacy.enlem},${selectedPharmacy.boylam}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)

        }

    }


    private fun createCSVReader(): Reader {
        val inputStream: InputStream = resources.openRawResource(R.raw.eczane)
        return InputStreamReader(inputStream, Charsets.UTF_8)
    }

}