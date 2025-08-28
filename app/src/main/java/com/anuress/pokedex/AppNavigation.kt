package com.anuress.pokedex // Or com.anuress.pokedex.ui.navigation

object PokedexDestinations {
    const val POKEDEX_LIST_ROUTE = "pokedex_list"
    const val POKEMON_DETAIL_ROUTE_BASE = "pokemon_detail" // Base for the route
    const val POKEMON_ID_ARG = "pokemonId" // Argument name
    // Complete route with argument: pokemon_detail/{pokemonId}
    val POKEMON_DETAIL_ROUTE_WITH_ARG = "$POKEMON_DETAIL_ROUTE_BASE/{$POKEMON_ID_ARG}"
}
