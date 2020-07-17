package com.fafadiatech.newscout.searchcomponent

import com.fafadiatech.newscout.BuildConfig
import com.fafadiatech.newscout.model.NewsDataApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GNewsApi {
    @GET("article/search/" + BuildConfig.DOMAIN_NAME)
    fun searchUsers(@Query("q") query: String,
                    @Query("page") page: Int
                    ): Call<NewsDataApi>


    companion object {
        private const val BASE_url = "http://www.newscout.in/api/v1/"

        fun create(): GNewsApi {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(BASE_url)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(GNewsApi::class.java)
        }
    }
}