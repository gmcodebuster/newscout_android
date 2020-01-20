package com.fafadiatech.newscout.api

import com.fafadiatech.newscout.UrlEncodeInterceptor
import com.fafadiatech.newscout.appconstants.BASE_URL
import com.fafadiatech.newscout.appconstants.URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {

    companion object {
        var retrofit: Retrofit? = null

        fun getClient(): Retrofit {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            var client = OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS)
                    .readTimeout(360, TimeUnit.SECONDS)
                    .addInterceptor(UrlEncodeInterceptor())
                    .build()

            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            return retrofit!!
        }


        fun getADSClient(): Retrofit {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            var client = OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS)
                    .readTimeout(360, TimeUnit.SECONDS)
                    .addInterceptor(UrlEncodeInterceptor())
                    .build()

            retrofit = Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            return retrofit!!
        }
    }
}