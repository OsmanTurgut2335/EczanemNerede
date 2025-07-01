package com.osman.eczanemnerede.screens

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.osman.eczanemnerede.R
import okhttp3.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NobetciEczaneler : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nobetci_eczaneler)

        pdfView = findViewById(R.id.pdfView)
        progressBar = findViewById(R.id.progressBar)

        val pdfUrl = intent.getStringExtra("pdf_url")
        val errorMessage = intent.getStringExtra("error_message")

        if (!errorMessage.isNullOrEmpty()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (pdfUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Beklenmeyen hata", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val currentMonth = SimpleDateFormat("MMMM", Locale("tr", "TR"))
            .format(Date())
            .lowercase(Locale("tr", "TR"))
        val cachedFile = File(cacheDir, "nobetci_$currentMonth.pdf")

        progressBar.visibility = View.VISIBLE

        if (cachedFile.exists()) {
            showPdfFromFile(cachedFile)
        } else {
            downloadAndDisplayPDF(pdfUrl, cachedFile)
        }
    }

    private fun showPdfFromFile(file: File) {
        pdfView.fromFile(file)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .spacing(10)
            .enableAntialiasing(true)
            .load()

        progressBar.visibility = View.GONE
    }

    private fun downloadAndDisplayPDF(pdfUrl: String, targetFile: File) {
        val client = OkHttpClient()
        val request = Request.Builder().url(pdfUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "PDF yüklenemedi!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.byteStream()?.use { inputStream ->
                    targetFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }

                    runOnUiThread {
                        showPdfFromFile(targetFile)
                    }
                }
            }
        })
    }
}
