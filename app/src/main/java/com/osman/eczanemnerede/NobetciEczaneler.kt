package com.osman.eczanemnerede

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import okhttp3.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NobetciEczaneler : AppCompatActivity() {
    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nobetci_eczaneler)

        pdfView = findViewById(R.id.pdfView)

        // Get the correct month's PDF URL
        val pdfUrl = getCurrentMonthPDFUrl()

        // Download and display the PDF
        downloadAndDisplayPDF(pdfUrl)
    }

    // Function to get the correct month in Turkish and generate the PDF URL
    private fun getCurrentMonthPDFUrl(): String {
        val turkishLocale = Locale("tr", "TR")
        val dateFormat = SimpleDateFormat("MMMM", turkishLocale) // Full month name in Turkish
        val monthName = dateFormat.format(Date()).uppercase() // Convert to uppercase
        return "https://keo.org.tr/dosyalar/files/$monthName.pdf"
    }

    // Function to download and display PDF
    private fun downloadAndDisplayPDF(pdfUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(pdfUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "PDF yüklenemedi!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.byteStream()?.use { inputStream ->
                    val file = File(cacheDir, "temp.pdf")
                    file.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }

                    runOnUiThread {
                        pdfView.fromFile(file)
                            .enableSwipe(true)
                            .swipeHorizontal(false) // Vertical scrolling
                            .enableDoubletap(true) // Zoom with double tap
                            .defaultPage(0) // Start on first page
                            .spacing(10) // Space between pages
                            .enableAntialiasing(true) // Smoother rendering
                            .load()
                    }
                }
            }
        })
    }
}
