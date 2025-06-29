package com.osman.eczanemnerede.screens

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.osman.eczanemnerede.R
import okhttp3.*
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NobetciEczaneler : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nobetci_eczaneler)

        pdfView = findViewById(R.id.pdfView)
        progressBar = findViewById(R.id.progressBar)

        // 1. ProgressBar göster
        progressBar.visibility = View.VISIBLE

        // 2. Güncel PDF bağlantısını al
        fetchLatestPdfUrl { pdfUrl ->
            runOnUiThread {
                if (pdfUrl != null) {
                    downloadAndDisplayPDF(pdfUrl)
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "PDF bağlantısı bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // JSoup ile en güncel PDF linkini bulan fonksiyon
    private fun fetchLatestPdfUrl(callback: (String?) -> Unit) {
        Thread {
            try {
                val doc = Jsoup.connect("https://keo.org.tr/kategori/nobetle-ilgili-462077/")
                    .userAgent("Mozilla/5.0")
                    .get()
                val turkishLocale = Locale("tr", "TR")
                val currentMonth = SimpleDateFormat("MMMM", turkishLocale).format(Date()).lowercase(turkishLocale)

                val selector = "a[href*=-$currentMonth][href*=-nobetci-eczane]"

                val latestAnnouncementElement = doc.select(selector).first()

                println("YARRRRRRRRRRRAK")
                println(latestAnnouncementElement)

                val detailUrl = latestAnnouncementElement?.attr("href")


                if (detailUrl == null) {
                    callback(null)
                    return@Thread
                }

                val rawHref = latestAnnouncementElement?.attr("href")

                val fullDetailUrl = when {
                    rawHref == null -> null
                    rawHref.startsWith("http") -> rawHref
                    rawHref.startsWith("/") -> "https://keo.org.tr$rawHref"
                    else -> "https://keo.org.tr/$rawHref"  // ← işte burası eksik '/' varsa tamamlıyor
                }


                println("YARRRRRRRRRRRAK")
                println(fullDetailUrl)

                // Duyuru detay sayfasını aç
                val detailDoc = Jsoup.connect(fullDetailUrl).get()

                // PDF linkini al
                val pdfElement = detailDoc.selectFirst("a[href$=.pdf]")
                val pdfUrl = pdfElement?.attr("href")

                callback(pdfUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }

    // PDF’i indirip gösteren fonksiyon
    private fun downloadAndDisplayPDF(pdfUrl: String) {
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
                    val file = File(cacheDir, "temp.pdf")
                    file.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }

                    runOnUiThread {
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
                }
            }
        })
    }
}
