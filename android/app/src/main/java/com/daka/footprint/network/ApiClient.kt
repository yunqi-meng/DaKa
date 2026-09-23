package com.daka.footprint.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private var token: String? = null

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val request = authHeader()?.let {
                    chain.request().newBuilder().addHeader("Authorization", it).build()
                } ?: chain.request()
                chain.proceed(request)
            }
            .build()
    }

    private var retrofit: Retrofit? = null

    var apiService: ApiService? = null
        private set

    fun init() {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit!!.create(ApiService::class.java)
    }

    fun setToken(token: String?) {
        this.token = token
    }

    fun hasToken(): Boolean = token != null

    fun authHeader(): String? = token?.let {
        if (it.startsWith("Bearer ")) it else "Bearer $it"
    }
}