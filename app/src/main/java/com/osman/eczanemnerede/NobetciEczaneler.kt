package com.osman.eczanemnerede


import com.osman.eczanemnerede.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osman.eczanemnerede.NobetciAdapter
import com.osman.eczanemnerede.PdfParser
import com.osman.eczanemnerede.PdfService
import com.osman.eczanemnerede.StringConverterFactory
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.Callback // Import the correct Callback class

class NobetciEczaneler : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nobetci_eczaneler)
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com/") // Replace with your base URL
            .addConverterFactory(StringConverterFactory) // Use the custom converter factory
            .build()


        val pdfService = retrofit.create(PdfService::class.java)

        // Replace this with the actual PDF URL
        val pdfUrl = "https://keo.org.tr/dosyalar/files/Eyl%C3%BCl.pdf"

        // Make a network request to download the PDF
        val call = pdfService.downloadPdfFile(pdfUrl)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val pdfContent = response.body()?.string() // Convert ResponseBody to String


                    // Parse the downloaded PDF content using your PdfParser class
                    val pharmacyNames = pdfContent?.let { PdfParser.getPharmacyNames(it) }



                    // Initialize RecyclerView
                    val recyclerView: RecyclerView = findViewById(R.id.recyclerViewNobetci)
                    val layoutManager = LinearLayoutManager(this@NobetciEczaneler)
                    recyclerView.layoutManager = layoutManager

                    // Create and set the adapter
                    val adapter = pharmacyNames?.let { NobetciAdapter(it) }
                    recyclerView.adapter = adapter
                    // Initialize RecyclerView and set the adapter as previously shown
                    // ...
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle network or other errors
            }
        })


    }
}
