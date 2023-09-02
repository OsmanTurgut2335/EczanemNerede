package com.osman.eczanemnerede

// StringConverterFactory.kt

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object StringConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: java.lang.reflect.Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (type == String::class.java) {
            return ScalarsConverterFactory.create().responseBodyConverter(type, annotations, retrofit)
        }
        return null
    }
}
