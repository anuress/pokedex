package com.anuress.data.network

// Domain model imports removed:
// import com.anuress.data.model.PokemonDetail
// import com.anuress.data.model.PokemonSpecies

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {

    // Base URL for PokeAPI is https://pokeapi.co/api/v2/
    // This endpoint will be appended to the base URL.
    // e.g., https://pokeapi.co/api/v2/pokemon?limit=20&offset=0

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonListResponse // com.anuress.data.network.PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(
        @Path("id") id: Int
    ): PokemonDetail // Now resolves to com.anuress.data.network.PokemonDetail

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(
        @Path("id") id: Int
    ): PokemonSpecies // Now resolves to com.anuress.data.network.PokemonSpecies
}
