package com.anuress.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {

    // Provide a singleton OkHttpClient instance
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Set to Level.BODY for development to see request and response bodies
            // For release, you might want Level.NONE or Level.BASIC
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // You can add other configurations like connectTimeout, readTimeout here
            .build()
    }

    // Provide a singleton Retrofit instance
    single {
        val baseUrl = "https://pokeapi.co/api/v2/"
        // Configure Kotlinx Serialization to ignore unknown keys, which is often helpful
        val json = Json { ignoreUnknownKeys = true }

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(get<OkHttpClient>()) // Koin injects the OkHttpClient defined above
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // Provide a singleton PokeApiService instance
    single {
        get<Retrofit>().create(PokeApiService::class.java) // Koin injects the Retrofit instance
    }
}
