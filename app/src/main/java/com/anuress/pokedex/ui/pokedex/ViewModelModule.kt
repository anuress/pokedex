package com.anuress.pokedex.ui.pokedex

import com.anuress.pokedex.ui.pokemondetail.PokemonDetailViewModel // Added import
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Define how to provide PokedexViewModel
    // Koin will automatically inject PokemonRepository into PokedexViewModel
    viewModel { PokedexViewModel(get()) }

    // Define how to provide PokemonDetailViewModel
    // Koin injects SavedStateHandle and PokemonRepository automatically
    viewModel { PokemonDetailViewModel(get(), get()) }
}
