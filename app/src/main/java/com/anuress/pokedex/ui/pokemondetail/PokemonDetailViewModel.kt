package com.anuress.pokedex.ui.pokemondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anuress.data.model.PokemonDetail
import com.anuress.data.model.PokemonSpecies
import com.anuress.data.repository.PokemonRepository
import com.anuress.pokedex.PokedexDestinations
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PokemonDetailScreenState(
    val isLoading: Boolean = true,
    val pokemonDetail: PokemonDetail? = null,
    val pokemonSpecies: PokemonSpecies? = null,
    val errorMessage: String? = null,
    val initialColorInt: Int? = null
)

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle[PokedexDestinations.POKEMON_ID_ARG])
    private val initialColorStringFromNav: String? = savedStateHandle[PokedexDestinations.POKEMON_COLOR_ARG]

    private val _uiState = MutableStateFlow(
        PokemonDetailScreenState(
            initialColorInt = initialColorStringFromNav?.toIntOrNull()
        )
    )
    val uiState: StateFlow<PokemonDetailScreenState> = _uiState.asStateFlow()

    init {
        fetchPokemonDetails()
    }

    private fun fetchPokemonDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val detailDeferred = async { pokemonRepository.getPokemonDetail(pokemonId) }
            val speciesDeferred = async { pokemonRepository.getPokemonSpecies(pokemonId) }

            val detailResult = detailDeferred.await()
            val speciesResult = speciesDeferred.await()

            var currentDetail: PokemonDetail? = null
            var currentSpecies: PokemonSpecies? = null
            val errorMessages = mutableListOf<String>()

            detailResult.fold(
                onSuccess = { detail -> currentDetail = detail },
                onFailure = { e -> errorMessages.add("Failed to load details: ${e.message ?: "Unknown error"}") }
            )

            speciesResult.fold(
                onSuccess = { species -> currentSpecies = species },
                onFailure = { e -> errorMessages.add("Failed to load species data: ${e.message ?: "Unknown error"}") }
            )

            if (currentDetail != null && currentSpecies != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pokemonDetail = currentDetail,
                        pokemonSpecies = currentSpecies,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMessages.joinToString("\n").ifEmpty { "Failed to load Pokémon data." }
                    )
                }
            }
        }
    }
}
