package com.anuress.pokedex.ui.pokedex

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Define how to provide PokedexViewModel
    // Koin will automatically inject PokemonRepository into PokedexViewModel
    viewModel { PokedexViewModel(get()) }
}
