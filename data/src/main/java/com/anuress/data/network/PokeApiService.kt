package com.anuress.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface PokeApiService {

    // Base URL for PokeAPI is https://pokeapi.co/api/v2/
    // This endpoint will be appended to the base URL.
    // e.g., https://pokeapi.co/api/v2/pokemon?limit=20&offset=0

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonListResponse // This will now refer to the class in PokemonApiModels.kt
}
