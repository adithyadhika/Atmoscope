package com.example.atmoscope.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Model Data Request & Response
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String, val userId: Int?)

// 2. Interface API Retrofit
interface AtmoscopeApiService {
    @POST("login.php")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}

// 3. Retrofit Client Instance
object RetrofitClient {
    // Ganti IP dengan IP Laptop kamu jika pakai Emulator bawaan Android Studio (biasanya 10.0.2.2)
    private const val BASE_URL = "http://10.0.2.2/"

    val instance: AtmoscopeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AtmoscopeApiService::class.java)
    }
}