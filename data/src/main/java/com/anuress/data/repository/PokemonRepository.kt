package com.anuress.data.repository

import androidx.paging.PagingData
import com.anuress.data.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun getPokemonStream(): Flow<PagingData<Pokemon>>
}
