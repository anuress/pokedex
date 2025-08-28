package com.anuress.data.repository

import androidx.paging.PagingData
import com.anuress.data.model.Pokemon // Assuming this is the list item model
import com.anuress.data.model.PokemonDetail // Added import
import com.anuress.data.model.PokemonSpecies // Added import
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun getPokemonStream(): Flow<PagingData<Pokemon>>

    suspend fun getPokemonDetail(id: Int): Result<PokemonDetail>

    suspend fun getPokemonSpecies(id: Int): Result<PokemonSpecies>
}
