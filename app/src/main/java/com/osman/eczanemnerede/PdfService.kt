package com.osman.eczanemnerede

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url
import retrofit2.Call
import retrofit2.http.Streaming

interface PdfService {
    @GET
    @Streaming
    fun downloadPdfFile(@Url fileUrl: String): Call<ResponseBody>
}
