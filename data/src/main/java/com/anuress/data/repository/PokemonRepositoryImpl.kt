package com.anuress.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.anuress.data.model.Pokemon
import com.anuress.data.network.PokeApiService
import com.anuress.data.network.POKEMON_PAGE_SIZE
import com.anuress.data.network.PokemonPagingSource
import kotlinx.coroutines.flow.Flow

class PokemonRepositoryImpl(
    private val pokeApiService: PokeApiService
) : PokemonRepository {

    override fun getPokemonStream(): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(
                pageSize = POKEMON_PAGE_SIZE,
                enablePlaceholders = false // Can be adjusted based on needs
            ),
            pagingSourceFactory = { PokemonPagingSource(pokeApiService) }
        ).flow
    }
}
