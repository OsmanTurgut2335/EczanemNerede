package com.osman.eczanemnerede

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor


class NobetciEczaneler : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nobetci_eczaneler)

        val pdfView :PDFView = findViewById(R.id.pdfView)
        pdfView.fromAsset("ekim.pdf").load()

    }



}