package com.anuress.pokedex.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anuress.data.model.Pokemon
import com.anuress.data.repository.PokemonRepository // Import the repository interface
import kotlinx.coroutines.flow.Flow

class PokedexViewModel(
    private val pokemonRepository: PokemonRepository // Depend on the interface
) : ViewModel() {

    val pokemonPagingFlow: Flow<PagingData<Pokemon>> =
        pokemonRepository.getPokemonStream() // Get the stream from the repository
            .cachedIn(viewModelScope) // Cache the flow in ViewModel's scope
}
