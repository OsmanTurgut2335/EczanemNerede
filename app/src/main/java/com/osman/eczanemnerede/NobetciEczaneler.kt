package com.osman.eczanemnerede

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView



class NobetciEczaneler : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nobetci_eczaneler)

        val pdfView :PDFView = findViewById(R.id.pdfView)
        pdfView.fromAsset("aralık.pdf").load()

    }



}