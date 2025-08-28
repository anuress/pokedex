package com.anuress.data.repository

import org.koin.dsl.module

val repositoryModule = module {
    // Define a singleton for PokemonRepository, providing PokemonRepositoryImpl
    // Koin will automatically inject PokeApiService into PokemonRepositoryImpl
    single<PokemonRepository> { PokemonRepositoryImpl(get()) }
}
