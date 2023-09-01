package com.osman.eczanemnerede

import android.telecom.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface PdfService {

    @GET
    fun downloadPdfFile(@Url fileUrl: String): Call<String>

}